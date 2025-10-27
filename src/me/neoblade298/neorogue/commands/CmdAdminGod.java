package me.neoblade298.neorogue.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class CmdAdminGod extends Subcommand {
	public CmdAdminGod(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
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
		PlayerFightData data = FightInstance.getUserData(p.getUniqueId());
		data.addMaxMana(1000);
		data.addMaxStamina(1000);
		data.addManaRegen(100);
		data.addStaminaRegen(100);
		data.addHealth(10000);
		data.addMana(10000);
		data.addStamina(10000);
		data.ignoreCooldowns(true);
		for (EquipmentInstance ei : data.getActiveEquipment().values()) {
			ei.setCooldown(0);
		}
		
		Util.msg(s, "All stats maxed and cooldowns ignored.");
	}
}
