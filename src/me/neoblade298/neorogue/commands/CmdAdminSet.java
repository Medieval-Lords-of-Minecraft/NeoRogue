package me.neoblade298.neorogue.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class CmdAdminSet extends Subcommand {
	public CmdAdminSet(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		ArrayList<String> tab = new ArrayList<String>(SetType.values().length);
		for (SetType type : SetType.values()) {
			tab.add(type.toString());
		}
		args.add(new Arg("type").setTabOptions(tab), new Arg("value"));
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
		PlayerFightData data = FightInstance.getUserData(p.getUniqueId());
		SetType type = SetType.valueOf(args[0].toUpperCase());
		int value = Integer.parseInt(args[1]);
		switch (type) {
		case MANA:
			data.setMana(value);
			break;
		case STAMINA:
			data.setStamina(value);
			break;
		case MANA_REGEN:
			data.setManaRegen(value);
			break;
		case STAMINA_REGEN:
			data.setStaminaRegen(value);
			break;
		}
		Util.msg(s, "Set " + type + " to " + value + ".");
	}

	private static enum SetType {
		MANA, STAMINA, MANA_REGEN, STAMINA_REGEN;
	}
}
