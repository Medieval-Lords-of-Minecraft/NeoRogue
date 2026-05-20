package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import net.kyori.adventure.text.Component;

public class CmdAdminDamage extends Subcommand {
	private static TargetProperties tp = TargetProperties.line(10, 2, TargetType.ENEMY);
	public CmdAdminDamage(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> tab = new ArrayList<String>(DamageType.values().length);
		for (DamageType type : DamageType.values()) {
			tab.add(type.toString());
		}
		args.add(new Arg("damage type").setTabOptions(tab), new Arg("amount"), new Arg("delay ticks", false));
		this.enableTabComplete();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p = (Player) s;
		Session sess = SessionManager.getSession(p);

		if (sess == null) {
			Util.displayError(p, "You're not currently in a session!");
			return;
		}

		Instance inst = sess.getInstance();
		if (!(inst instanceof FightInstance)) {
			Util.displayError(p, "You're not currently in a fight!");
			return;
		}
		DamageType type = DamageType.valueOf(args[0].toUpperCase());
		int damage = Integer.parseInt(args[1]);
		LivingEntity trg = TargetHelper.getNearestInSight(p, tp);
		FightData src;
		LivingEntity temp = null;
		if (trg == null) {
			trg = p;
			Location above = p.getLocation().add(0, 5, 0);
			// Player can't damage themselves if their shield is raised for some reason, spawn a temporary mob to act as damage source
			temp = (LivingEntity) p.getWorld().spawnEntity(above, EntityType.ZOMBIE);
			temp.setAI(false);
			temp.setInvulnerable(true);
			temp.setSilent(true);
			temp.customName(Component.text("Damage Source"));
			temp.setCustomNameVisible(false);
			FightInstance fi = (FightInstance) sess.getInstance();
			src = fi.createFightData(temp);
		}
		else {
			src = FightInstance.getUserData(p.getUniqueId());
		}

		final LivingEntity ftrg = trg;
		final LivingEntity ftemp = temp;
		DamageMeta dm = new DamageMeta(src, damage, type, DamageStatTracker.ignored("Command"));
		if (args.length > 2) {
			int delay = Integer.parseInt(args[2]);
			new BukkitRunnable() {
				public void run() {
					dealDamage(s, dm, damage, type, ftrg);
					if (ftemp != null) {
						ftemp.remove();
					}
				}
			}.runTaskLater(NeoRogue.inst(), delay);
			return;
		}
		else {
			dealDamage(s, dm, damage, type, trg);
		}
	}

	private void dealDamage(CommandSender s, DamageMeta dm, int damage, DamageType type, LivingEntity trg) {
		dm.dealDamage(trg);
		Util.msg(s, "Dealt " + damage + " " + type + " damage");
	}
}
