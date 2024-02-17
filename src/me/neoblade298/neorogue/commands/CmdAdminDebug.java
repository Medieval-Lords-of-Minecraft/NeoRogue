package me.neoblade298.neorogue.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class CmdAdminDebug extends Subcommand {
	HashMap<String, HashMap<String, Integer>> results = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> failedResults = new HashMap<String, HashMap<String, Integer>>();
	HashSet<String> resultKeys = new HashSet<String>();

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("damage", false));
	}

	@Override
	public void run(CommandSender s, String[] args) {
		UUID uuid = Bukkit.getPlayerUniqueId("Ascheladd");
		if (args.length == 1) {
			FightInstance.getFightData(uuid).applyStatus(StatusType.POISON, uuid, 10, 5);
		}
		else {
			new DamageMeta(FightInstance.getFightData(uuid), 5, DamageType.SLASHING).dealDamage(Bukkit.getPlayer("Ascheladd"));
		}
	}
}
