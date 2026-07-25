package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdAdminReset extends Subcommand {
	// Each scope maps a keyword to the reset it performs on a PlayerData. Enum order controls the
	// sequence applied when several scopes are requested.
	private enum ResetScope {
		ALL("all", PlayerData::resetAll),
		PROGRESS("progress", PlayerData::resetProgress),
		HISTORY("history", PlayerData::resetRunHistory),
		SAVES("saves", PlayerData::resetSavedRuns),
		BOOSTS("boosts", PlayerData::resetExpBoosts),
		CARGO("cargo", PlayerData::resetCargo),
		FLEET("fleet", PlayerData::resetFleet),
		CARAVAN("caravan", PlayerData::resetCaravan);

		private final String key;
		private final Consumer<PlayerData> action;

		ResetScope(String key, Consumer<PlayerData> action) {
			this.key = key;
			this.action = action;
		}

		private static ResetScope match(String input) {
			for (ResetScope scope : values()) {
				if (scope.key.equalsIgnoreCase(input)) return scope;
			}
			return null;
		}

		private static String keyList() {
			ArrayList<String> keys = new ArrayList<>();
			for (ResetScope scope : values()) keys.add(scope.key);
			return String.join(", ", keys);
		}
	}

	public CmdAdminReset(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("player", false), new Arg("scope...", false));
		args.setMax(-1);
		this.overrideTabHandler();
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		// args[0] is the subcommand key ("reset"); args[1] = player, args[2+] = scopes.
		if (args.length == 2) {
			// First token can be a player name or (when resetting yourself) a scope keyword.
			String prefix = args[1].toLowerCase();
			ArrayList<String> opts = new ArrayList<>(FlagCommandUtil.onlinePlayerNames(args[1]));
			for (ResetScope scope : ResetScope.values()) {
				if (scope.key.startsWith(prefix)) opts.add(scope.key);
			}
			return opts;
		}
		if (args.length >= 3) {
			// Later tokens are always scopes; hide ones already typed.
			LinkedHashSet<String> used = new LinkedHashSet<>();
			for (int i = 2; i < args.length - 1; i++) used.add(args[i].toLowerCase());
			String prefix = args[args.length - 1].toLowerCase();
			ArrayList<String> opts = new ArrayList<>();
			for (ResetScope scope : ResetScope.values()) {
				if (used.contains(scope.key)) continue;
				if (scope.key.startsWith(prefix)) opts.add(scope.key);
			}
			return opts;
		}
		return List.of();
	}

	@Override
	public void run(CommandSender s, String[] args) {
		// Resolve the target and where scope arguments begin. If the first arg names an online player it's
		// the target and scopes follow; otherwise the sender targets themselves and all args are scopes.
		Player target;
		int scopeStart;
		if (args.length >= 1 && ResetScope.match(args[0]) == null) {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				Util.msgRaw(s, "<red>That player is not online.");
				return;
			}
			scopeStart = 1;
		} else if (s instanceof Player) {
			target = (Player) s;
			scopeStart = 0;
		} else {
			Util.msgRaw(s, "<red>Console must specify a player.");
			return;
		}

		PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
		if (data == null) {
			Util.msgRaw(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		// Parse the requested scopes (deduplicated, order preserved). No scopes = full reset.
		LinkedHashSet<ResetScope> scopes = new LinkedHashSet<>();
		for (int i = scopeStart; i < args.length; i++) {
			ResetScope scope = ResetScope.match(args[i]);
			if (scope == null) {
				Util.msgRaw(s, "<red>Unknown reset scope '<yellow>" + args[i] + "</yellow>'. Options: " + ResetScope.keyList());
				return;
			}
			scopes.add(scope);
		}
		if (scopes.isEmpty() || scopes.contains(ResetScope.ALL)) {
			data.resetAll();
			Util.msgRaw(s, "<gray>Reset <yellow>all progress</yellow> for <yellow>" + target.getName() + "</yellow>.");
			return;
		}

		ArrayList<String> applied = new ArrayList<>();
		for (ResetScope scope : scopes) {
			scope.action.accept(data);
			applied.add(scope.key);
		}
		Util.msgRaw(s, "<gray>Reset <yellow>" + String.join(", ", applied)
				+ "</yellow> for <yellow>" + target.getName() + "</yellow>.");
	}
}
