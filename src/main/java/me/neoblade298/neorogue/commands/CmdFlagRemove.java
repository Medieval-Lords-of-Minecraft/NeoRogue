package me.neoblade298.neorogue.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.FlagRegistry;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdFlagRemove extends Subcommand {
	public CmdFlagRemove(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("flag"), new Arg("player", false));
		this.overrideTabHandler();
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		// args[0] is the subcommand key ("remove"); args[1] = flag, args[2] = player.
		if (args.length == 2) {
			// The flag is typed before the player, so scope suggestions to the target if already known,
			// otherwise to the sender's own flags.
			Player target = FlagCommandUtil.resolveTarget(s, args.length > 2 ? args[2] : null);
			if (target != null) {
				PlayerData pdata = PlayerManager.getPlayerData(target.getUniqueId());
				if (pdata != null) return FlagCommandUtil.filter(pdata.getFlags(), args[1]);
			}
			return FlagCommandUtil.filter(FlagRegistry.getKnownFlags(), args[1]);
		}
		if (args.length == 3) return FlagCommandUtil.onlinePlayerNames(args[2]);
		return List.of();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		String flag = args[0];
		Player target = FlagCommandUtil.resolveTarget(s, args.length > 1 ? args[1] : null);
		if (target == null) {
			Util.msgRaw(s, "<red>That player isn't online! (or provide a player from console)");
			return;
		}

		PlayerData pdata = PlayerManager.getPlayerData(target.getUniqueId());
		if (pdata == null) {
			Util.msgRaw(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		if (!pdata.hasFlag(flag)) {
			Util.msgRaw(s, "<red>" + target.getName() + " doesn't have flag '" + flag + "'.");
			return;
		}

		pdata.removeFlag(flag);
		Util.msgRaw(s, "<green>Removed flag '<white>" + flag + "<green>' from " + target.getName() + ".");
	}
}
