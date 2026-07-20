package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Shared helpers for the /nrflag subcommands (player resolution + prefix-filtered tab suggestions).
class FlagCommandUtil {
	private FlagCommandUtil() {
	}

	// Resolves the target player: the named player if provided, else the sender when they're a player.
	// Returns null if no valid target could be determined (the caller reports the error).
	static Player resolveTarget(CommandSender s, String name) {
		if (name != null) return Bukkit.getPlayer(name);
		return s instanceof Player ? (Player) s : null;
	}

	// Names of every online player whose name starts with the given (case-insensitive) prefix.
	static List<String> onlinePlayerNames(String prefix) {
		String lower = prefix.toLowerCase();
		ArrayList<String> names = new ArrayList<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().toLowerCase().startsWith(lower)) names.add(p.getName());
		}
		return names;
	}

	// Options starting with the given (case-insensitive) prefix.
	static List<String> filter(Collection<String> options, String prefix) {
		String lower = prefix.toLowerCase();
		ArrayList<String> out = new ArrayList<String>();
		for (String opt : options) {
			if (opt.toLowerCase().startsWith(lower)) out.add(opt);
		}
		return out;
	}
}
