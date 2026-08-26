package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardLocation;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardPeriod;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardRunMode;
import me.neoblade298.neorogue.leaderboard.LeaderboardConfig.LeaderboardType;
import me.neoblade298.neorogue.leaderboard.LeaderboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CmdAdminLeaderboard extends Subcommand {
	public static final String PERMISSION = "neorogue.leaderboards.manage";

	public CmdAdminLeaderboard(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		args.add(new Arg("action").setTabOptions(new ArrayList<>(List.of("add", "list", "teleport", "remove", "reload", "refresh"))),
				new Arg("leaderboard/id", false).setTabOptions(new ArrayList<>(List.of("winrate", "fastest_clear"))),
				new Arg("period", false).setTabOptions(new ArrayList<>(List.of("alltime", "monthly"))),
				new Arg("notoriety", false).setTabOptions(notorietyOptions()),
				new Arg("run-mode", false).setTabOptions(new ArrayList<>(List.of("all", "competitive", "casual"))));
		enableTabComplete();
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (args.length == 0) {
			usage(sender);
			return;
		}
		switch (args[0].toLowerCase(Locale.ROOT)) {
		case "add" -> add(sender, args);
		case "list" -> list(sender, args);
		case "teleport", "tp" -> teleport(sender, args);
		case "remove" -> remove(sender, args);
		case "reload" -> {
			LeaderboardManager.reload();
			Util.msgRaw(sender, "<green>Reloaded leaderboard locations.");
		}
		case "refresh" -> {
			LeaderboardManager.refresh();
			Util.msgRaw(sender, "<green>Refreshing leaderboard results.");
		}
		default -> usage(sender);
		}
	}

	private void add(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			Util.msgRaw(sender, "<red>Only players can add leaderboard locations.");
			return;
		}
		if (args.length < 2) {
			Util.msgRaw(sender, "<red>Usage: /nradmin leaderboard add <winrate|fastest_clear> [alltime|monthly] [all|0-10] [all|competitive|casual]");
			return;
		}
		try {
			LeaderboardType type = parseType(args[1]);
			LeaderboardPeriod period = args.length >= 3
					? LeaderboardPeriod.valueOf(args[2].toUpperCase(Locale.ROOT)) : LeaderboardPeriod.ALLTIME;
			Integer notoriety = args.length >= 4 && !args[3].equalsIgnoreCase("all")
					? Integer.valueOf(args[3]) : null;
			if (notoriety != null && (notoriety < 0 || notoriety > 10)) throw new IllegalArgumentException();
			LeaderboardRunMode runMode = args.length >= 5
					? LeaderboardRunMode.valueOf(args[4].toUpperCase(Locale.ROOT)) : LeaderboardRunMode.ALL;
			LeaderboardLocation added = LeaderboardManager.add(type, period, notoriety, runMode, player.getLocation());
			Util.msgRaw(sender, "<green>Added " + type.display() + " leaderboard <white>" + added.id()
					+ "</white> at your location.");
		} catch (IllegalArgumentException ex) {
			Util.msgRaw(sender, "<red>Invalid leaderboard options. Use: <white>/nradmin leaderboard add <winrate|fastest_clear> [alltime|monthly] [all|0-10] [all|competitive|casual]");
		}
	}

	private void list(CommandSender sender, String[] args) {
		LeaderboardType filter = null;
		if (args.length >= 2) {
			try {
				filter = parseType(args[1]);
			} catch (IllegalArgumentException ex) {
				Util.msgRaw(sender, "<red>Unknown leaderboard: " + args[1]);
				return;
			}
		}
		List<LeaderboardLocation> locations = LeaderboardManager.getLocations();
		Util.msgRaw(sender, "<gold>Leaderboard Locations <gray>(" + locations.size() + ")");
		for (LeaderboardLocation location : locations) {
			if (filter != null && location.type() != filter) continue;
			String notoriety = location.notoriety() == null ? "all" : location.notoriety().toString();
			Component line = Component.text(location.id() + " ", NamedTextColor.YELLOW)
					.append(Component.text(location.type().name().toLowerCase(Locale.ROOT) + " ", NamedTextColor.WHITE))
					.append(Component.text(location.period().name().toLowerCase(Locale.ROOT) + " n=" + notoriety
							+ " " + location.runMode().name().toLowerCase(Locale.ROOT) + " ", NamedTextColor.GRAY))
					.append(Component.text("[TP]", NamedTextColor.AQUA)
							.clickEvent(ClickEvent.runCommand("/nradmin leaderboard teleport " + location.id()))
							.hoverEvent(HoverEvent.showText(Component.text("Teleport to " + location.world() + " "
									+ formatCoordinates(location)))))
					.append(Component.space())
					.append(Component.text("[REMOVE]", NamedTextColor.RED)
							.clickEvent(ClickEvent.runCommand("/nradmin leaderboard remove " + location.id()))
							.hoverEvent(HoverEvent.showText(Component.text("Remove this display"))));
			Util.msgRaw(sender, line);
		}
	}

	private void teleport(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			Util.msgRaw(sender, "<red>Only players can teleport to leaderboard locations.");
			return;
		}
		LeaderboardLocation location = find(args);
		if (location == null) {
			Util.msgRaw(sender, "<red>Unknown leaderboard location.");
			return;
		}
		player.teleport(location.toLocation());
		Util.msgRaw(sender, "<green>Teleported to leaderboard " + location.id() + ".");
	}

	private void remove(CommandSender sender, String[] args) {
		if (args.length < 2 || !LeaderboardManager.remove(args[1])) {
			Util.msgRaw(sender, "<red>Unknown leaderboard location.");
			return;
		}
		Util.msgRaw(sender, "<green>Removed leaderboard location " + args[1] + ".");
	}

	private LeaderboardLocation find(String[] args) {
		if (args.length < 2) return null;
		return LeaderboardManager.getLocations().stream()
				.filter(location -> location.id().equalsIgnoreCase(args[1]))
				.findFirst().orElse(null);
	}

	private LeaderboardType parseType(String value) {
		return switch (value.toLowerCase(Locale.ROOT)) {
		case "winrate", "win_rate" -> LeaderboardType.WINRATE;
		case "fastest", "fastest_clear" -> LeaderboardType.FASTEST_CLEAR;
		default -> throw new IllegalArgumentException();
		};
	}

	private String formatCoordinates(LeaderboardLocation location) {
		return String.format(Locale.US, "%.1f, %.1f, %.1f", location.x(), location.y(), location.z());
	}

	private static List<String> notorietyOptions() {
		List<String> options = new ArrayList<>();
		options.add("all");
		for (int i = 0; i <= 10; i++) options.add(String.valueOf(i));
		return options;
	}

	private void usage(CommandSender sender) {
		Util.msgRaw(sender, "<red>Usage: /nradmin leaderboard <add|list|teleport|remove|reload|refresh>");
	}
}