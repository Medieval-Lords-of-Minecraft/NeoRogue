package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.commands.AnalyticsFilters.FilterOption;
import me.neoblade298.neorogue.commands.AnalyticsReport.EquipmentMetric;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.fight.Mob;

// Brigadier command for the analytics reports: /nrlytics <view> [args]. Each subcommand parses its
// arguments here and delegates to AnalyticsReport, which does the querying and printing. Replaces the
// old NeoCore SubcommandManager registration; permission (neorogue.admin) is enforced via requires().
@SuppressWarnings("UnstableApiUsage")
public class LyticsCommand {
	// Ordered list of subcommands shown when /nrlytics is run with no arguments.
	private static final List<String> SUBCOMMANDS = List.of("version", "equipment", "classes", "losses", "chance",
			"mobs", "minibosses", "bosses", "mob", "achievement");

	private LyticsCommand() {
	}

	public static LiteralCommandNode<CommandSourceStack> build() {
		return Commands.literal("nrlytics")
				.requires(src -> src.getSender().hasPermission("neorogue.admin"))
				.executes(LyticsCommand::usage)

				// version [n]
				.then(Commands.literal("version")
						.executes(LyticsCommand::showVersion)
						.then(Commands.argument("version", IntegerArgumentType.integer())
								.executes(LyticsCommand::setVersion)))

				// achievement <achievementId> <class> <mastery>
				.then(Commands.literal("achievement")
						.executes(LyticsCommand::usage)
						.then(Commands.argument("achievementId", StringArgumentType.word())
								.suggests(suggest(LyticsCommand::achievementIds))
								.then(Commands.argument("class", StringArgumentType.word())
										.suggests(suggest(LyticsCommand::playerClasses))
										.then(Commands.argument("mastery", IntegerArgumentType.integer(1))
												.executes(LyticsCommand::runAchievement)))))

				// equipment [id=<equipmentId>] [key=value ...]
				.then(Commands.literal("equipment")
						.executes(ctx -> runEquipment(ctx, ""))
						.then(Commands.argument("filters", StringArgumentType.greedyString())
								.suggests(LyticsCommand::suggestEquipmentFilters)
								.executes(ctx -> runEquipment(ctx, StringArgumentType.getString(ctx, "filters")))))

				// classes [key=value ...]
				.then(Commands.literal("classes")
						.executes(ctx -> runClasses(ctx, ""))
						.then(Commands.argument("options", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestFilters(builder,
										AnalyticsReport.CLASS_FILTER_OPTIONS))
								.executes(ctx -> runClasses(ctx, getStr(ctx, "options")))))

				// losses [key=value ...]
				.then(Commands.literal("losses")
						.executes(ctx -> runLosses(ctx, ""))
						.then(Commands.argument("options", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestFilters(builder,
										AnalyticsReport.LOSS_FILTER_OPTIONS))
								.executes(ctx -> runLosses(ctx, getStr(ctx, "options")))))

				// chance [setId] [class] [page=n] [filterlow=true|false]
				.then(Commands.literal("chance")
						.executes(ctx -> runChance(ctx, ""))
						.then(Commands.argument("args", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestFilters(builder,
										AnalyticsReport.CHANCE_FILTER_OPTIONS))
								.executes(ctx -> runChance(ctx, getStr(ctx, "args")))))

				// mobs [regionType] [class] [page=n] [filterlow=true|false]
				.then(Commands.literal("mobs")
						.executes(ctx -> runMobs(ctx, ""))
						.then(Commands.argument("args", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestFilters(builder,
										AnalyticsReport.MOB_FILTER_OPTIONS))
								.executes(ctx -> runMobs(ctx, getStr(ctx, "args")))))

				// minibosses [class]
				.then(Commands.literal("minibosses")
						.executes(ctx -> runMinibosses(ctx, ""))
						.then(Commands.argument("args", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestFilters(builder,
										AnalyticsReport.MOB_FILTER_OPTIONS))
								.executes(ctx -> runMinibosses(ctx, getStr(ctx, "args")))))

				// bosses [class]
				.then(Commands.literal("bosses")
						.executes(ctx -> runBosses(ctx, ""))
						.then(Commands.argument("args", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> suggestFilters(builder,
										AnalyticsReport.MOB_FILTER_OPTIONS))
								.executes(ctx -> runBosses(ctx, getStr(ctx, "args")))))

				// mob <mobId>
				.then(Commands.literal("mob")
						.executes(LyticsCommand::usage)
						.then(Commands.argument("mobId", StringArgumentType.word())
								.suggests(suggest(Mob::getStatIds))
								.executes(ctx -> runMob(ctx, ""))
								.then(Commands.argument("options", StringArgumentType.greedyString())
										.suggests((ctx, builder) -> suggestFilters(builder,
												AnalyticsReport.MOB_FILTER_OPTIONS))
										.executes(ctx -> runMob(ctx, getStr(ctx, "options"))))))
				.build();
	}

	// ---- Executors ---------------------------------------------------------

	private static int usage(CommandContext<CommandSourceStack> ctx) {
		CommandSender s = ctx.getSource().getSender();
		Util.msgRaw(s, "<red>Usage: /nrlytics <subcommand> [args]");
		Util.msgRaw(s, "<gray>Subcommands: <white>" + String.join(", ", SUBCOMMANDS));
		Util.msgRaw(s, "<gray>Leaderboard options: <white>page=1 filterlow=true");
		Util.msgRaw(s, "<gray>Equipment search: <white>/nrlytics equipment id=<equipmentId>");
		Util.msgRaw(s, "<gray>Equipment metrics: <white>" + String.join(", ", AnalyticsReport.EQUIPMENT_METRIC_KEYS).toLowerCase());
		return Command.SINGLE_SUCCESS;
	}

	private static int showVersion(CommandContext<CommandSourceStack> ctx) {
		CommandSender s = ctx.getSource().getSender();
		Util.msgRaw(s, "<gray>Analytics balance version: <yellow>" + AnalyticsManager.getQueryBalanceVersion()
				+ "</yellow> <gray>(latest: <yellow>" + AnalyticsManager.BALANCE_VERSION + "</yellow><gray>)");
		return Command.SINGLE_SUCCESS;
	}

	private static int setVersion(CommandContext<CommandSourceStack> ctx) {
		CommandSender s = ctx.getSource().getSender();
		int version = IntegerArgumentType.getInteger(ctx, "version");
		AnalyticsManager.setQueryBalanceVersion(version);
		Util.msgRaw(s, "<gray>Analytics balance version set to <yellow>" + version);
		return Command.SINGLE_SUCCESS;
	}

	private static int runAchievement(CommandContext<CommandSourceStack> ctx) {
		CommandSender s = ctx.getSource().getSender();
		String achievementId = getStr(ctx, "achievementId");
		Achievement achievement = AchievementManager.get(achievementId);
		if (achievement == null) {
			Util.msgRaw(s, "<red>Unknown achievement: <white>" + achievementId);
			return Command.SINGLE_SUCCESS;
		}

		EquipmentClass equipmentClass;
		try {
			equipmentClass = EquipmentClass.valueOf(getStr(ctx, "class").toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			Util.msgRaw(s, "<red>Invalid class. Expected: <white>" + String.join(", ", playerClasses()));
			return Command.SINGLE_SUCCESS;
		}
		if (!AchievementManager.getForScope(equipmentClass).contains(achievement)) {
			Util.msgRaw(s, "<red>That achievement is not tracked for <white>" + equipmentClass.name());
			return Command.SINGLE_SUCCESS;
		}

		int mastery = IntegerArgumentType.getInteger(ctx, "mastery");
		if (mastery > achievement.getMasteryThresholds().length) {
			Util.msgRaw(s, "<red>Invalid mastery. Maximum for this achievement: <white>"
					+ achievement.getMasteryThresholds().length);
			return Command.SINGLE_SUCCESS;
		}

		AnalyticsReport.achievementOwners(s, achievement, equipmentClass, mastery);
		return Command.SINGLE_SUCCESS;
	}

	private static int runEquipment(CommandContext<CommandSourceStack> ctx, String filterStr) {
		CommandSender s = ctx.getSource().getSender();
		String[] tokens = filterStr.isBlank() ? new String[0] : filterStr.trim().split("\\s+");
		ArrayList<String> filterTokens = new ArrayList<String>();
		EquipmentMetric metric = EquipmentMetric.DAMAGE;
		String sortBy = "rate";
		for (String token : tokens) {
			if (token.toLowerCase().startsWith("sort=")) {
				sortBy = token.substring("sort=".length()).toLowerCase();
				if (!sortBy.equals("rate") && !sortBy.equals("class")) {
					Util.msgRaw(s, "<red>Invalid pickrate sort. Expected: <white>rate, class");
					return Command.SINGLE_SUCCESS;
				}
			}
			else if (!token.toLowerCase().startsWith("metric=")) {
				filterTokens.add(token);
			}
			else {
				EquipmentMetric parsedMetric = EquipmentMetric.fromKey(token.substring("metric=".length()));
				if (parsedMetric == null) {
					Util.msgRaw(s, "<red>Invalid equipment metric. Expected: <white>"
							+ String.join(", ", AnalyticsReport.EQUIPMENT_METRIC_KEYS).toLowerCase());
					return Command.SINGLE_SUCCESS;
				}
				metric = parsedMetric;
			}
		}
		List<FilterOption> filterOptions = metric == EquipmentMetric.PICKRATE
				? AnalyticsReport.PICKRATE_FILTER_OPTIONS : AnalyticsReport.EQUIPMENT_FILTER_OPTIONS;
		AnalyticsFilters filters = AnalyticsFilters.parse(filterTokens.toArray(String[]::new), 0,
				filterOptions);
		if (metric == EquipmentMetric.PICKRATE) AnalyticsReport.equipmentPickrate(s, version(), sortBy, filters);
		else AnalyticsReport.equipmentLeaderboard(s, version(), metric, filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runClasses(CommandContext<CommandSourceStack> ctx, String args) {
		AnalyticsReport.classWinrates(ctx.getSource().getSender(), version(),
				parseArgs(args, 0, AnalyticsReport.CLASS_FILTER_OPTIONS).filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runLosses(CommandContext<CommandSourceStack> ctx, String args) {
		AnalyticsReport.losses(ctx.getSource().getSender(), version(),
				parseArgs(args, 0, AnalyticsReport.LOSS_FILTER_OPTIONS).filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runChance(CommandContext<CommandSourceStack> ctx, String args) {
		ParsedArgs parsed = parseArgs(args, 2, AnalyticsReport.CHANCE_FILTER_OPTIONS);
		AnalyticsReport.chance(ctx.getSource().getSender(), version(), parsed.get(0), upper(parsed.get(1)), parsed.filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runMobs(CommandContext<CommandSourceStack> ctx, String args) {
		ParsedArgs parsed = parseArgs(args, 2, AnalyticsReport.MOB_FILTER_OPTIONS);
		AnalyticsReport.mobs(ctx.getSource().getSender(), version(), upper(parsed.get(0)), upper(parsed.get(1)), parsed.filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runMinibosses(CommandContext<CommandSourceStack> ctx, String args) {
		ParsedArgs parsed = parseArgs(args, 1, AnalyticsReport.MOB_FILTER_OPTIONS);
		AnalyticsReport.minibosses(ctx.getSource().getSender(), version(), upper(parsed.get(0)), parsed.filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runBosses(CommandContext<CommandSourceStack> ctx, String args) {
		ParsedArgs parsed = parseArgs(args, 1, AnalyticsReport.MOB_FILTER_OPTIONS);
		AnalyticsReport.bosses(ctx.getSource().getSender(), version(), upper(parsed.get(0)), parsed.filters);
		return Command.SINGLE_SUCCESS;
	}

	private static int runMob(CommandContext<CommandSourceStack> ctx, String args) {
		AnalyticsReport.mob(ctx.getSource().getSender(), getStr(ctx, "mobId"), version(),
				parseArgs(args, 0, AnalyticsReport.MOB_FILTER_OPTIONS).filters);
		return Command.SINGLE_SUCCESS;
	}

	// ---- Suggestions -------------------------------------------------------

	// Context-aware completion for the view's trailing key=value filters. The greedy argument covers
	// the whole remaining tail, so we isolate the current (last) token and offset the builder to it,
	// then suggest filter keys or a key's allowed values, mirroring the old getTabOptions behavior.
	private static CompletableFuture<Suggestions> suggestEquipmentFilters(CommandContext<CommandSourceStack> ctx,
			SuggestionsBuilder builder) {
		ArrayList<FilterOption> options = new ArrayList<FilterOption>(AnalyticsReport.EQUIPMENT_FILTER_OPTIONS);
		for (FilterOption option : AnalyticsReport.PICKRATE_FILTER_OPTIONS) {
			if (options.stream().noneMatch(existing -> existing.key.equalsIgnoreCase(option.key))) options.add(option);
		}
		options.add(new FilterOption("metric", "", false, AnalyticsReport.EQUIPMENT_METRIC_KEYS));
		options.add(new FilterOption("sort", "", false, List.of("rate", "class")));
		return suggestFilters(builder, options);
	}

	private static CompletableFuture<Suggestions> suggestFilters(SuggestionsBuilder builder,
			List<FilterOption> options) {
		String remaining = builder.getRemaining();
		int lastSpace = remaining.lastIndexOf(' ');
		String token = remaining.substring(lastSpace + 1);
		SuggestionsBuilder b = builder.createOffset(builder.getStart() + lastSpace + 1);

		int eq = token.indexOf('=');
		String lower = token.toLowerCase();

		// No '=' yet: suggest the available filter keys.
		if (eq < 0) {
			for (FilterOption o : options) {
				if ((o.key + "=").toLowerCase().startsWith(lower)) b.suggest(o.key + "=");
			}
			suggestSharedOptions(b, lower);
			return b.buildFuture();
		}

		// "key=" typed: suggest that key's allowed values (null allowed = free-form, no suggestions).
		String key = token.substring(0, eq).toLowerCase();
		for (FilterOption o : options) {
			if (!o.key.equalsIgnoreCase(key)) continue;
			if (o.allowed == null) return b.buildFuture();
			for (String value : o.allowed) {
				if ((o.key + "=" + value).toLowerCase().startsWith(lower)) b.suggest(o.key + "=" + value);
			}
			break;
		}
		return b.buildFuture();
	}

	private static void suggestSharedOptions(SuggestionsBuilder builder, String token) {
		if ("page=".startsWith(token)) builder.suggest("page=");
		if ("filterlow=true".startsWith(token)) builder.suggest("filterlow=true");
		if ("filterlow=false".startsWith(token)) builder.suggest("filterlow=false");
	}

	private static SuggestionProvider<CommandSourceStack> suggest(Supplier<? extends Collection<String>> supplier) {
		return (ctx, builder) -> {
			String rem = builder.getRemainingLowerCase();
			for (String v : supplier.get()) {
				if (v.toLowerCase().startsWith(rem)) builder.suggest(v);
			}
			return builder.buildFuture();
		};
	}

	private static Collection<String> achievementIds() {
		return AchievementManager.getAll().stream().map(Achievement::getId).toList();
	}

	private static Collection<String> playerClasses() {
		return java.util.Arrays.stream(EquipmentClass.values())
				.filter(equipmentClass -> equipmentClass != EquipmentClass.SHOP
						&& equipmentClass != EquipmentClass.CLASSLESS)
				.map(Enum::name).toList();
	}

	// ---- Helpers -----------------------------------------------------------

	private static int version() {
		return AnalyticsManager.getQueryBalanceVersion();
	}

	private static String getStr(CommandContext<CommandSourceStack> ctx, String name) {
		return StringArgumentType.getString(ctx, name);
	}

	private static String upper(String s) {
		return s == null ? null : s.toUpperCase();
	}

	private static ParsedArgs parseArgs(String args, int positionalLimit, List<FilterOption> filterOptions) {
		String[] tokens = args == null || args.isBlank() ? new String[0] : args.trim().split("\\s+");
		ArrayList<String> positional = new ArrayList<String>();
		ArrayList<String> options = new ArrayList<String>();
		for (String token : tokens) {
			if (token.contains("=") || positional.size() >= positionalLimit) options.add(token);
			else positional.add(token);
		}
		AnalyticsFilters filters = AnalyticsFilters.parse(options.toArray(String[]::new), 0, filterOptions);
		return new ParsedArgs(positional, filters);
	}

	private record ParsedArgs(List<String> positional, AnalyticsFilters filters) {
		private String get(int index) {
			return index < positional.size() ? positional.get(index) : null;
		}
	}

}
