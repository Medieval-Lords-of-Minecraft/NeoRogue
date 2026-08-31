package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;

public class CmdAdminReset extends Subcommand {
	// The playable classes that a class-aware scope can be limited to via the "scope:class" syntax.
	private static final EquipmentClass[] CLASS_OPTIONS = {
			EquipmentClass.WARRIOR, EquipmentClass.THIEF, EquipmentClass.ARCHER, EquipmentClass.MAGE
	};

	// Each scope maps a keyword to the reset it performs on a PlayerData. Enum order controls the
	// sequence applied when several scopes are requested. classAction != null marks a scope as
	// "class-aware": it also supports a "scope:class" form that resets only that class's data.
	private enum ResetScope {
		ALL("all", PlayerData::resetAll, null),
		PROGRESS("progress", PlayerData::resetProgress, PlayerData::resetClassProgress),
		HISTORY("history", PlayerData::resetRunHistory, PlayerData::resetClassRunHistory),
		SAVES("saves", PlayerData::resetSavedRuns, null),
		BOOSTS("boosts", PlayerData::resetBoosts, null),
		CARGO("cargo", PlayerData::resetCargo, null),
		FLEET("fleet", PlayerData::resetFleet, null),
		CARAVAN("caravan", PlayerData::resetCaravan, null);

		private final String key;
		private final Consumer<PlayerData> action;
		private final BiConsumer<PlayerData, EquipmentClass> classAction;

		ResetScope(String key, Consumer<PlayerData> action, BiConsumer<PlayerData, EquipmentClass> classAction) {
			this.key = key;
			this.action = action;
			this.classAction = classAction;
		}

		private boolean isClassAware() {
			return classAction != null;
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

		private static String classAwareList() {
			ArrayList<String> keys = new ArrayList<>();
			for (ResetScope scope : values()) if (scope.isClassAware()) keys.add(scope.key);
			return String.join(", ", keys);
		}
	}

	// A single parsed scope token: a scope plus an optional class limit (null = the account-wide reset).
	private record ScopeRequest(ResetScope scope, EquipmentClass ec) {
		private String label() {
			return ec == null ? scope.key : scope.key + ":" + ec.name().toLowerCase();
		}

		private void apply(PlayerData data) {
			if (ec == null) scope.action.accept(data);
			else scope.classAction.accept(data, ec);
		}
	}

	private static EquipmentClass matchClass(String input) {
		for (EquipmentClass ec : CLASS_OPTIONS) {
			if (ec.name().equalsIgnoreCase(input)) return ec;
		}
		return null;
	}

	private static String classList() {
		ArrayList<String> names = new ArrayList<>();
		for (EquipmentClass ec : CLASS_OPTIONS) names.add(ec.name().toLowerCase());
		return String.join(", ", names);
	}

	public CmdAdminReset(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("scope[:class]...", true), new Arg("player", false));
		args.setMax(-1);
		this.overrideTabHandler();
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		// args[0] is the subcommand key ("reset"); args[1+] are scopes, with an optional trailing player name.
		if (args.length < 2) return List.of();
		String cur = args[args.length - 1].toLowerCase();

		// Completing the class half of a "scope:class" token.
		int colon = cur.indexOf(':');
		if (colon >= 0) {
			ResetScope scope = ResetScope.match(cur.substring(0, colon));
			ArrayList<String> opts = new ArrayList<>();
			if (scope != null && scope.isClassAware()) {
				String classPrefix = cur.substring(colon + 1);
				for (EquipmentClass ec : CLASS_OPTIONS) {
					String name = ec.name().toLowerCase();
					if (name.startsWith(classPrefix)) opts.add(scope.key + ":" + name);
				}
			}
			return opts;
		}

		// Account-wide scope keywords already typed (so repeating them is pointless); class-limited tokens
		// don't hide their base scope, since it can be reused for other classes.
		LinkedHashSet<String> usedFull = new LinkedHashSet<>();
		for (int i = 1; i < args.length - 1; i++) {
			String t = args[i].toLowerCase();
			if (t.indexOf(':') < 0) usedFull.add(t);
		}
		ArrayList<String> opts = new ArrayList<>();
		for (ResetScope scope : ResetScope.values()) {
			if (usedFull.contains(scope.key)) continue;
			if (scope.key.startsWith(cur)) opts.add(scope.key);
		}
		// The final token may instead name the target player.
		opts.addAll(FlagCommandUtil.onlinePlayerNames(args[args.length - 1]));
		return opts;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		// Scopes come first (at least one required); an optional trailing token names the target player.
		// If the final arg isn't a scope keyword, treat it as the player and the rest as scopes.
		Player target;
		int scopeEnd = args.length; // exclusive end of the scope tokens
		if (args.length >= 1 && ResetScope.match(baseScope(args[args.length - 1])) == null) {
			target = Bukkit.getPlayer(args[args.length - 1]);
			if (target == null) {
				Util.msgRaw(s, "<red>That player is not online.");
				return;
			}
			scopeEnd = args.length - 1;
		} else if (s instanceof Player) {
			target = (Player) s;
		} else {
			Util.msgRaw(s, "<red>Console must specify a player.");
			return;
		}

		if (scopeEnd == 0) {
			Util.msgRaw(s, "<red>You must specify at least one reset scope. Options: " + ResetScope.keyList());
			return;
		}

		PlayerData data = PlayerManager.getPlayerData(target.getUniqueId());
		if (data == null) {
			Util.msgRaw(s, "<red>No loaded player data found for " + target.getName() + ".");
			return;
		}

		// Parse the requested scopes (deduplicated by label, order preserved). A "scope:class" token limits
		// a class-aware scope to one class; a bare scope resets it account-wide.
		LinkedHashSet<String> seen = new LinkedHashSet<>();
		ArrayList<ScopeRequest> requests = new ArrayList<>();
		for (int i = 0; i < scopeEnd; i++) {
			String token = args[i];
			String scopePart = token;
			EquipmentClass ec = null;
			int colon = token.indexOf(':');
			if (colon >= 0) {
				scopePart = token.substring(0, colon);
				ec = matchClass(token.substring(colon + 1));
				if (ec == null) {
					Util.msgRaw(s, "<red>Unknown class '<yellow>" + token.substring(colon + 1) + "</yellow>'. Options: " + classList());
					return;
				}
			}
			ResetScope scope = ResetScope.match(scopePart);
			if (scope == null) {
				Util.msgRaw(s, "<red>Unknown reset scope '<yellow>" + scopePart + "</yellow>'. Options: " + ResetScope.keyList());
				return;
			}
			if (ec != null && !scope.isClassAware()) {
				Util.msgRaw(s, "<red>Scope '<yellow>" + scope.key + "</yellow>' can't be limited to a class. Class-aware scopes: " + ResetScope.classAwareList());
				return;
			}
			ScopeRequest req = new ScopeRequest(scope, ec);
			if (seen.add(req.label())) requests.add(req);
		}

		// A bare "all" wipes everything; no point applying anything else.
		for (ScopeRequest req : requests) {
			if (req.scope() == ResetScope.ALL) {
				data.resetAll();
				Util.msgRaw(s, "<gray>Reset <yellow>all progress</yellow> for <yellow>" + target.getName() + "</yellow>.");
				return;
			}
		}

		ArrayList<String> applied = new ArrayList<>();
		for (ScopeRequest req : requests) {
			req.apply(data);
			applied.add(req.label());
		}
		Util.msgRaw(s, "<gray>Reset <yellow>" + String.join(", ", applied)
				+ "</yellow> for <yellow>" + target.getName() + "</yellow>.");
	}

	// Returns the scope keyword portion of a token, dropping any ":class" suffix.
	private static String baseScope(String token) {
		int colon = token.indexOf(':');
		return colon < 0 ? token : token.substring(0, colon);
	}
}
