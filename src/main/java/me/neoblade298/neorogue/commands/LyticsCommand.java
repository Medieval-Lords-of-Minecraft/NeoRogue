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
import me.neoblade298.neorogue.commands.AnalyticsFilters.FilterOption;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.analytics.OfferSnapshot.OfferSource;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.fight.Mob;

// Brigadier command for the analytics reports: /nrlytics <view> [args]. Each subcommand parses its
// arguments here and delegates to AnalyticsReport, which does the querying and printing. Replaces the
// old NeoCore SubcommandManager registration; permission (neorogue.admin) is enforced via requires().
@SuppressWarnings("UnstableApiUsage")
public class LyticsCommand {
	private static final List<String> VIEWS = List.of("equipment");
	private static final List<String> SORTS = List.of("rate", "class");
	// Ordered list of subcommands shown when /nrlytics is run with no arguments.
	private static final List<String> SUBCOMMANDS = List.of("version", "equipment", "view", "pickrate", "chance",
			"mobs", "minibosses", "bosses", "mob");

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

				// equipment <equipment>
				.then(Commands.literal("equipment")
						.executes(LyticsCommand::usage)
						.then(Commands.argument("equipment", StringArgumentType.word())
								.executes(LyticsCommand::runEquipment)))

				// view <view> [key=value ...]
				.then(Commands.literal("view")
						.executes(LyticsCommand::usage)
						.then(Commands.argument("view", StringArgumentType.word())
								.suggests(suggest(() -> VIEWS))
								.executes(ctx -> runView(ctx, ""))
								.then(Commands.argument("filters", StringArgumentType.greedyString())
										.suggests(LyticsCommand::suggestViewFilters)
										.executes(ctx -> runView(ctx, StringArgumentType.getString(ctx, "filters"))))))

				// pickrate [source] [class] [sortBy]
				.then(Commands.literal("pickrate")
						.executes(ctx -> runPickrate(ctx, null, null, "rate"))
						.then(Commands.argument("source", StringArgumentType.word())
								.suggests(suggest(LyticsCommand::offerSources))
								.executes(ctx -> runPickrate(ctx, getStr(ctx, "source"), null, "rate"))
								.then(Commands.argument("class", StringArgumentType.word())
										.suggests(suggest(LyticsCommand::allClasses))
										.executes(ctx -> runPickrate(ctx, getStr(ctx, "source"), getStr(ctx, "class"), "rate"))
										.then(Commands.argument("sortBy", StringArgumentType.word())
												.suggests(suggest(() -> SORTS))
												.executes(ctx -> runPickrate(ctx, getStr(ctx, "source"), getStr(ctx, "class"),
														getStr(ctx, "sortBy")))))))

				// chance [setId] [class]
				.then(Commands.literal("chance")
						.executes(ctx -> runChance(ctx, null, null))
						.then(Commands.argument("setId", StringArgumentType.word())
								.suggests(suggest(ChanceSet::getSets))
								.executes(ctx -> runChance(ctx, getStr(ctx, "setId"), null))
								.then(Commands.argument("class", StringArgumentType.word())
										.suggests(suggest(LyticsCommand::playerClasses))
										.executes(ctx -> runChance(ctx, getStr(ctx, "setId"), getStr(ctx, "class"))))))

				// mobs [regionType] [class]
				.then(Commands.literal("mobs")
						.executes(ctx -> runMobs(ctx, null, null))
						.then(Commands.argument("regionType", StringArgumentType.word())
								.suggests(suggest(LyticsCommand::regionTypes))
								.executes(ctx -> runMobs(ctx, getStr(ctx, "regionType"), null))
								.then(Commands.argument("class", StringArgumentType.word())
										.suggests(suggest(LyticsCommand::playerClasses))
										.executes(ctx -> runMobs(ctx, getStr(ctx, "regionType"), getStr(ctx, "class"))))))

				// minibosses [class]
				.then(Commands.literal("minibosses")
						.executes(ctx -> runMinibosses(ctx, null))
						.then(Commands.argument("class", StringArgumentType.word())
								.suggests(suggest(LyticsCommand::playerClasses))
								.executes(ctx -> runMinibosses(ctx, getStr(ctx, "class")))))

				// bosses [class]
				.then(Commands.literal("bosses")
						.executes(ctx -> runBosses(ctx, null))
						.then(Commands.argument("class", StringArgumentType.word())
								.suggests(suggest(LyticsCommand::playerClasses))
								.executes(ctx -> runBosses(ctx, getStr(ctx, "class")))))

				// mob <mobId>
				.then(Commands.literal("mob")
						.executes(LyticsCommand::usage)
						.then(Commands.argument("mobId", StringArgumentType.word())
								.suggests(suggest(Mob::getStatIds))
								.executes(LyticsCommand::runMob)))
				.build();
	}

	// ---- Executors ---------------------------------------------------------

	private static int usage(CommandContext<CommandSourceStack> ctx) {
		CommandSender s = ctx.getSource().getSender();
		Util.msgRaw(s, "<red>Usage: /nrlytics <subcommand> [args]");
		Util.msgRaw(s, "<gray>Subcommands: <white>" + String.join(", ", SUBCOMMANDS));
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

	private static int runEquipment(CommandContext<CommandSourceStack> ctx) {
		AnalyticsReport.equipment(ctx.getSource().getSender(), getStr(ctx, "equipment"), version());
		return Command.SINGLE_SUCCESS;
	}

	private static int runView(CommandContext<CommandSourceStack> ctx, String filterStr) {
		CommandSender s = ctx.getSource().getSender();
		String view = getStr(ctx, "view").toLowerCase();
		List<FilterOption> options = filterOptionsFor(view);
		if (options == null) {
			Util.msgRaw(s, "<red>Unknown view '" + view + "'. Available: " + String.join(", ", VIEWS));
			return Command.SINGLE_SUCCESS;
		}
		String[] tokens = filterStr.isBlank() ? new String[0] : filterStr.trim().split("\\s+");
		AnalyticsFilters filters = AnalyticsFilters.parse(tokens, 0, options);
		if (view.equals("equipment")) {
			AnalyticsReport.equipmentDamage(s, version(), filters);
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int runPickrate(CommandContext<CommandSourceStack> ctx, String source, String eqClass, String sortBy) {
		AnalyticsReport.pickrate(ctx.getSource().getSender(), version(), upper(source), upper(eqClass),
				sortBy.toLowerCase());
		return Command.SINGLE_SUCCESS;
	}

	private static int runChance(CommandContext<CommandSourceStack> ctx, String setId, String playerClass) {
		AnalyticsReport.chance(ctx.getSource().getSender(), version(), setId, upper(playerClass));
		return Command.SINGLE_SUCCESS;
	}

	private static int runMobs(CommandContext<CommandSourceStack> ctx, String regionType, String playerClass) {
		AnalyticsReport.mobs(ctx.getSource().getSender(), version(), upper(regionType), upper(playerClass));
		return Command.SINGLE_SUCCESS;
	}

	private static int runMinibosses(CommandContext<CommandSourceStack> ctx, String playerClass) {
		AnalyticsReport.minibosses(ctx.getSource().getSender(), version(), upper(playerClass));
		return Command.SINGLE_SUCCESS;
	}

	private static int runBosses(CommandContext<CommandSourceStack> ctx, String playerClass) {
		AnalyticsReport.bosses(ctx.getSource().getSender(), version(), upper(playerClass));
		return Command.SINGLE_SUCCESS;
	}

	private static int runMob(CommandContext<CommandSourceStack> ctx) {
		AnalyticsReport.mob(ctx.getSource().getSender(), getStr(ctx, "mobId"), version());
		return Command.SINGLE_SUCCESS;
	}

	// ---- Suggestions -------------------------------------------------------

	// Context-aware completion for the view's trailing key=value filters. The greedy argument covers
	// the whole remaining tail, so we isolate the current (last) token and offset the builder to it,
	// then suggest filter keys or a key's allowed values, mirroring the old getTabOptions behavior.
	private static CompletableFuture<Suggestions> suggestViewFilters(CommandContext<CommandSourceStack> ctx,
			SuggestionsBuilder builder) {
		List<FilterOption> options = filterOptionsFor(StringArgumentType.getString(ctx, "view").toLowerCase());
		if (options == null) return builder.buildFuture();

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

	private static SuggestionProvider<CommandSourceStack> suggest(Supplier<? extends Collection<String>> supplier) {
		return (ctx, builder) -> {
			String rem = builder.getRemainingLowerCase();
			for (String v : supplier.get()) {
				if (v.toLowerCase().startsWith(rem)) builder.suggest(v);
			}
			return builder.buildFuture();
		};
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

	// Maps a view name to its filter options (currently only the equipment view is filterable).
	private static List<FilterOption> filterOptionsFor(String view) {
		if (view.equalsIgnoreCase("equipment")) return AnalyticsReport.EQUIPMENT_FILTER_OPTIONS;
		return null;
	}

	// The player-selectable classes, used as class filters across most lytics reports.
	private static List<String> playerClasses() {
		ArrayList<String> classes = new ArrayList<String>();
		for (EquipmentClass ec : EquipmentClass.values()) {
			if (ec == EquipmentClass.SHOP || ec == EquipmentClass.CLASSLESS) continue;
			classes.add(ec.name());
		}
		return classes;
	}

	// All equipment classes (including SHOP/CLASSLESS), used by the pickrate class filter.
	private static List<String> allClasses() {
		ArrayList<String> classes = new ArrayList<String>();
		for (EquipmentClass ec : EquipmentClass.values()) classes.add(ec.name());
		return classes;
	}

	private static List<String> offerSources() {
		ArrayList<String> sources = new ArrayList<String>();
		for (OfferSource src : OfferSource.values()) sources.add(src.name());
		return sources;
	}

	private static List<String> regionTypes() {
		ArrayList<String> regions = new ArrayList<String>();
		for (RegionType rt : RegionType.values()) {
			if (rt.name().contains("DEBUG")) continue;
			regions.add(rt.name());
		}
		return regions;
	}
}
