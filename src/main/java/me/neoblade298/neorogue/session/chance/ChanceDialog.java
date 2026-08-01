package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.ChanceGlossaryInventory;
import me.neoblade298.neorogue.player.inventory.FightInfoInventory;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.analytics.ChanceChoiceSnapshot;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class ChanceDialog {
	private ChanceDialog() {
	}

	public static void show(Player viewer, ChanceInstance inst, ChanceSet set, ChanceStage stage) {
		show(viewer, inst.getSession().getData(viewer.getUniqueId()), inst, set, stage, false);
	}

	public static void show(Player viewer, PlayerSessionData data, ChanceInstance inst, ChanceSet set,
			ChanceStage stage, boolean spectator) {
		if (stage == null) return;

		Component choiceType = Component.text(set.isIndividual() ? "Individual Choice" : "Host Choice",
				NamedTextColor.YELLOW);
		List<DialogBody> body = List.of(DialogBody.plainMessage(choiceType),
				DialogBody.plainMessage(stage.description));

		List<ActionButton> buttons = new ArrayList<ActionButton>();
		UUID viewerId = viewer.getUniqueId();
		for (int i = 0; i < stage.choices.size(); i++) {
			ChanceChoice choice = stage.choices.get(i);
			ItemStack item = choice.getItem(inst.getSession(), inst, data);
			ItemMeta meta = item.getItemMeta();
			Component label = meta.displayName() != null ? meta.displayName() : Component.text(choice.getPlainTitle());
			ActionButton.Builder button = ActionButton.builder(label).width(200).tooltip(buildTooltip(meta));
			if (!spectator) {
				int choiceIndex = i;
				button.action(DialogAction.customClick((response, audience) -> {
					Player currentViewer = Bukkit.getPlayer(viewerId);
					if (currentViewer != null) choose(currentViewer, inst, set, stage, choiceIndex);
				},
						ClickCallback.Options.builder().uses(1).build()));
			}
			buttons.add(button.build());
		}
		if (inst.getNextInstance() instanceof FightInstance fight) {
			buttons.add(ActionButton.builder(Component.text("Fight Info", NamedTextColor.BLUE))
					.width(200)
					.tooltip(Component.text("Preview the enemies in the upcoming fight."))
					.action(DialogAction.customClick((response, audience) -> {
						Player currentViewer = Bukkit.getPlayer(viewerId);
						if (currentViewer == null) return;
						currentViewer.closeDialog();
						new FightInfoInventory(currentViewer, inst.getSession(), data, fight, fight.getMap().getMobs(), true);
					}, ClickCallback.Options.builder().uses(1).build()))
					.build());
		}

		// Combine glossary tags across every choice in this stage into a single glossary inventory.
		TreeSet<GlossaryIcon> tags = new TreeSet<GlossaryIcon>(GlossaryIcon.comparator);
		for (ChanceChoice choice : stage.choices) {
			tags.addAll(choice.getTags(inst, data));
		}
		if (!tags.isEmpty()) {
			buttons.add(ActionButton.builder(Component.text("Glossary", NamedTextColor.LIGHT_PURPLE))
					.width(200)
					.tooltip(Component.text("View definitions for the terms used in these options."))
					.action(DialogAction.customClick((response, audience) -> {
						Player currentViewer = Bukkit.getPlayer(viewerId);
						if (currentViewer == null) return;
						currentViewer.closeDialog();
						new ChanceGlossaryInventory(currentViewer, tags, set.getDisplay(),
								() -> show(currentViewer, data, inst, set, stage, spectator));
					}, ClickCallback.Options.builder().uses(1).build()))
					.build());
		}
		buttons.add(ActionButton.builder(Component.text("Close", NamedTextColor.WHITE))
				.width(200)
				.action(DialogAction.customClick((response, audience) -> {
					Player currentViewer = Bukkit.getPlayer(viewerId);
					if (currentViewer != null) currentViewer.closeDialog();
				}, ClickCallback.Options.builder().uses(1).build()))
				.build());

		Component title = spectator
				? Component.text(data.getData().getDisplay() + "'s Chance Event", NamedTextColor.BLUE)
				: set.getDisplay();
		Dialog dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(title)
						.canCloseWithEscape(true)
						.body(body)
						.build())
				.type(DialogType.multiAction(buttons).columns(Math.max(1, Math.min(2, buttons.size()))).build()));
		viewer.showDialog(dialog);
	}

	private static Component buildTooltip(ItemMeta meta) {
		List<Component> lore = meta.lore();
		if (lore == null || lore.isEmpty()) return Component.empty();
		Component tooltip = Component.empty();
		boolean hasLine = false;
		for (Component line : lore) {
			if (PlainTextComponentSerializer.plainText().serialize(line).equals("Right click for glossary")) continue;
			if (hasLine) tooltip = tooltip.append(Component.newline());
			tooltip = tooltip.append(line);
			hasLine = true;
		}
		return tooltip;
	}

	private static void choose(Player player, ChanceInstance inst, ChanceSet set, ChanceStage shownStage,
			int choiceIndex) {
		UUID uuid = player.getUniqueId();
		Session session = inst.getSession();
		PlayerSessionData data = session.getData(uuid);
		if (data == null || inst.getStage(uuid) != shownStage || choiceIndex >= shownStage.choices.size()) {
			player.closeDialog();
			return;
		}

		ChanceChoice choice = shownStage.choices.get(choiceIndex);
		if (!set.isIndividual() && !uuid.equals(session.getHost())) {
			if (!session.canSuggest()) return;
			player.closeDialog();
			session.setSuggestCooldown();
			session.broadcast(player.name().color(NamedTextColor.YELLOW)
					.append(Component.text(" suggests the choice ", NamedTextColor.GRAY))
					.append(choice.getItemWithoutConditions().displayName()));
			session.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
			return;
		}
		if (!choice.canChoose(session, inst, data)) {
			player.closeDialog();
			Util.displayError(player, "You aren't eligible for this option!");
			show(player, inst, set, shownStage);
			return;
		}

		player.closeDialog();
		recordPendingPick(inst, data, shownStage, choiceIndex);
		if (choice.getInteractiveAction() != null) {
			choice.getInteractiveAction().open(inst, data, () -> show(data.getPlayer(), inst, set, shownStage));
			return;
		}

		ChanceStage next = set.getStage(choice.choose(session, inst, data));
		inst.advanceStage(uuid, next);
		session.getInstance().updateBoardLines();
	}

	private static void recordPendingPick(ChanceInstance inst, PlayerSessionData data, ChanceStage stage,
			int pickedIndex) {
		if (!AnalyticsManager.ENABLED) return;
		Session session = inst.getSession();
		UUID uuid = data.getUniqueId();
		ChanceSet set = inst.getSet();
		ChanceChoiceSnapshot snap = new ChanceChoiceSnapshot(UUID.randomUUID().toString(),
				System.currentTimeMillis(), AnalyticsManager.BALANCE_VERSION, uuid.toString(),
				data.getPlayerClass() != null ? data.getPlayerClass().name() : "UNKNOWN",
				session.getHost().toString(), session.getSaveSlot(), session.getRunId(), set.getId(), stage.getId(),
				session.getRegion().getType().name(), session.getNode().getType().name(), session.getLevel(),
				set.isIndividual());
		for (int i = 0; i < stage.choices.size(); i++) {
			ChanceChoice choice = stage.choices.get(i);
			snap.addChoice(i, choice.getPlainTitle(), choice.canChoose(session, inst, data), i == pickedIndex);
		}
		inst.setPendingPick(uuid, snap);
	}
}