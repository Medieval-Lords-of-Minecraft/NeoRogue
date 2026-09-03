package me.neoblade298.neorogue.session.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;

public class TutorialInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.CHANCE_X + 6.5, SPAWN_Z = Session.CHANCE_Z + 1.5;
	private static final double HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 6;
	private static final String TRIGGER_ID = "TutorialInstance";

	private final List<TutorialStage> stages;
	private final HashMap<UUID, Integer> playerStages = new HashMap<UUID, Integer>();
	private TextDisplay holo;
	private Block candleBlock;
	private boolean returning;

	public TutorialInstance(Session s) {
		this(s, List.of(
				new TutorialStage(
						Component.text("Equipment Details", NamedTextColor.GOLD),
						Component.text("Open your inventory and right click an equipment to proceed!"),
						Component.text("Success!", NamedTextColor.GREEN),
						SessionTrigger.OPEN_GLOSSARY,
						input -> true),
				new TutorialStage(
						Component.text("Tutorial Book", NamedTextColor.GOLD),
						Component.text("Find and open the tutorial (book & quill) in your inventory to proceed!"),
						Component.text("Success!", NamedTextColor.GREEN),
						SessionTrigger.OPEN_TUTORIAL_BOOK,
						input -> true)));
	}

	public TutorialInstance(Session s, List<TutorialStage> stages) {
		super(s, SPAWN_X, SPAWN_Z);
		if (stages.isEmpty()) throw new IllegalArgumentException("A tutorial instance must have at least one stage");
		this.stages = List.copyOf(stages);
		spectatorLines = playerLines;
	}

	@Override
	public void setup() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player player = data.getPlayer();
			if (player != null) player.teleport(spawn);
			playerStages.put(data.getUniqueId(), 0);
			registerStageTrigger(data);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) player.teleport(spawn);
		}
		super.setup();
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z),
				Component.text("Tutorial", NamedTextColor.GOLD).appendNewline()
						.append(Component.text("Right click the pillar below!", NamedTextColor.WHITE)));
		candleBlock = spawn.clone().add(0, 1, 3).getBlock();
	}

	private void registerStageTrigger(PlayerSessionData data) {
		int stageIndex = playerStages.getOrDefault(data.getUniqueId(), stages.size());
		if (stageIndex >= stages.size()) return;
		TutorialStage stage = stages.get(stageIndex);
		data.addTrigger(TRIGGER_ID, stage.getCompletionTrigger(), (playerData, input) -> {
			if (s.getInstance() != this || !stage.matches(input)) return TriggerResult.keep();
			advanceStage(playerData);
			return TriggerResult.remove();
		});
	}

	private void advanceStage(PlayerSessionData data) {
		UUID uuid = data.getUniqueId();
		int currentStage = playerStages.getOrDefault(uuid, 0);
		if (currentStage >= stages.size()) return;
		Player player = data.getPlayer();
		if (player != null) player.sendMessage(stages.get(currentStage).getSuccessMessage());
		int nextStage = currentStage + 1;
		playerStages.put(uuid, nextStage);
		if (nextStage < stages.size()) {
			registerStageTrigger(data);
			Bukkit.getScheduler().runTask(NeoRogue.inst(), () -> {
				if (s.getInstance() != this || playerStages.getOrDefault(uuid, stages.size()) != nextStage) return;
				Player currentPlayer = Bukkit.getPlayer(uuid);
				if (currentPlayer != null) showDialog(currentPlayer, stages.get(nextStage));
			});
		}
		s.launchFireworks();
		updateBoardLines();
		updateActionBar();
		if (allPlayersComplete()) completeTutorial();
	}

	private void completeTutorial() {
		if (returning) return;

		Instance next = s.getNode().getDestinations().isEmpty()
				? new TutorialWinInstance(s)
				: NodeSelectInstance.create(s);
		if (!s.canSetInstance(next)) return;

		Candle candle = (Candle) candleBlock.getBlockData();
		candle.setLit(true);
		candleBlock.setBlockData(candle);
		returning = true;
		s.setBusy(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (s.getInstance() == TutorialInstance.this) s.setInstance(next);
				s.setBusy(false);
			}
		}.runTaskLater(NeoRogue.inst(), 40L);
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null
				|| (e.getClickedBlock().getType() != Material.QUARTZ_PILLAR
						&& e.getClickedBlock().getType() != Material.LIGHT_GRAY_CANDLE)) {
			super.handleInteractEvent(e);
			return;
		}

		PlayerSessionData data = s.getData(e.getPlayer().getUniqueId());
		if (data == null) return;
		int stageIndex = playerStages.getOrDefault(data.getUniqueId(), stages.size());
		if (stageIndex < stages.size()) {
			showDialog(e.getPlayer(), stages.get(stageIndex));
		} else {
			if (!allPlayersComplete()) {
				e.getPlayer().sendMessage(Component.text("Waiting for the rest of your party to finish.", NamedTextColor.YELLOW));
			}
		}
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK
				|| e.getClickedBlock() == null) return;
		Material type = e.getClickedBlock().getType();
		if (type != Material.QUARTZ_PILLAR && type != Material.LIGHT_GRAY_CANDLE) return;
		e.setCancelled(true);
		PlayerSessionData hostData = s.getData(s.getHost());
		int stageIndex = playerStages.getOrDefault(s.getHost(), stages.size());
		if (hostData != null && stageIndex < stages.size()) showDialog(e.getPlayer(), stages.get(stageIndex));
	}

	private void showDialog(Player player, TutorialStage stage) {
		UUID viewerId = player.getUniqueId();
		ActionButton close = ActionButton.builder(Component.text("Okay", NamedTextColor.WHITE))
				.width(200)
				.action(DialogAction.customClick((response, audience) -> {
					Player viewer = Bukkit.getPlayer(viewerId);
					if (viewer != null) viewer.closeDialog();
				}, ClickCallback.Options.builder().uses(1).build()))
				.build();
		Dialog dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(stage.getTitle())
						.canCloseWithEscape(true)
						.body(List.of(DialogBody.plainMessage(stage.getText())))
						.build())
				.type(DialogType.multiAction(List.of(close)).columns(1).build()));
		player.showDialog(dialog);
	}

	private boolean allPlayersComplete() {
		for (UUID uuid : s.getParty().keySet()) {
			if (playerStages.getOrDefault(uuid, 0) < stages.size()) return false;
		}
		return true;
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		for (PlayerSessionData data : new ArrayList<PlayerSessionData>(s.getParty().values())) {
			boolean complete = playerStages.getOrDefault(data.getUniqueId(), 0) >= stages.size();
			playerLines.add((complete ? "§a✓ §f" : "§c✗ §f") + data.getData().getDisplay());
		}
	}

	@Override
	public Component getActionBar(PlayerSessionData data) {
		boolean complete = playerStages.getOrDefault(data.getUniqueId(), 0) >= stages.size();
		return getActionBar(data, complete ? "Tutorial complete" : "Tutorial in progress",
				complete ? NamedTextColor.GREEN : NamedTextColor.YELLOW);
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		for (PlayerSessionData data : s.getParty().values()) data.removeTrigger(TRIGGER_ID);
		super.cleanup(pluginDisable);
		if (candleBlock != null && candleBlock.getBlockData() instanceof Candle candle) {
			candle.setLit(false);
			candleBlock.setBlockData(candle);
		}
		if (holo != null) holo.remove();
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer player) {
		PlayerSessionData data = s.getData(player.getUniqueId());
		if (data != null) data.removeTrigger(TRIGGER_ID);
		playerStages.remove(player.getUniqueId());
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return null;
	}

	public static class TutorialStage {
		private final Component title;
		private final Component text, successMessage;
		private final SessionTrigger completionTrigger;
		private final Predicate<Object> completionPredicate;

		public TutorialStage(Component title, Component text, SessionTrigger completionTrigger,
				Predicate<Object> completionPredicate) {
			this(title, text, Component.text("Success! Stage complete.", NamedTextColor.GREEN), completionTrigger,
					completionPredicate);
		}

		public TutorialStage(Component title, Component text, Component successMessage,
				SessionTrigger completionTrigger, Predicate<Object> completionPredicate) {
			this.title = title;
			this.text = text;
			this.successMessage = successMessage;
			this.completionTrigger = completionTrigger;
			this.completionPredicate = completionPredicate;
		}

		public Component getTitle() {
			return title;
		}

		public Component getText() {
			return text;
		}

		public Component getSuccessMessage() {
			return successMessage;
		}

		public SessionTrigger getCompletionTrigger() {
			return completionTrigger;
		}

		public boolean matches(Object input) {
			return completionPredicate.test(input);
		}
	}
}