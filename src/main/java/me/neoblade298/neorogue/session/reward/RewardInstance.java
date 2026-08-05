package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.instances.EditInventoryInstance;
import me.neoblade298.neorogue.session.instances.InstanceType;
import me.neoblade298.neorogue.session.instances.NodeSelectInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.text.Component;

public class RewardInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.REWARDS_X + 7.5, SPAWN_Z = Session.REWARDS_Z + 3.5,
			HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 6;
	private static final int DISPLAY_TICKS = 60, POP_TICKS = 12, DISMISS_TICKS = 10;
	private static final double CHOICE_SPACING = 1.9;
	private static final float NAME_SCALE = 0.7F;
	private HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
	private final Map<UUID, BukkitTask> rewardDisplayTasks = new HashMap<UUID, BukkitTask>();
	private final Map<UUID, List<RewardDisplay>> rewardDisplays = new HashMap<UUID, List<RewardDisplay>>();
	private TextDisplay holo;
	private NodeType previous;

	private record RewardDisplay(ItemDisplay item, TextDisplay name) {}
	
	public RewardInstance(Session s, HashMap<UUID, ArrayList<Reward>> rewards, NodeType previous) {
		super(s, SPAWN_X, SPAWN_Z);
		this.rewards = rewards;
		this.previous = previous;
		spectatorLines = playerLines;
	}
	
	// Explicitly used for deserialization
	public RewardInstance(Session s, HashMap<UUID, PlayerSessionData> party, NodeType previous, boolean useless) {
		super(s, SPAWN_X, SPAWN_Z);
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			rewards.put(ent.getKey(), Reward.deserializeArray(ent.getValue().getInstanceData()));
		}
		this.previous = previous;
		spectatorLines = playerLines;
	}

	// Builds the reward instance for a TREASURE node: every party member gets 50 coins and one randomly
	// generated artifact. Rewards are built directly (no REWARD_FIGHT trigger) so it stays a flat treasure.
	public static RewardInstance createTreasure(Session s) {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			list.add(new CurrencyReward(50));
			ArrayList<Artifact> arts = Equipment.getArtifact(data.getArtifactDroptable(), s.getBaseDropValue(), 1,
					data.getPlayerClass(), EquipmentClass.CLASSLESS);
			if (!arts.isEmpty()) {
				list.add(new EquipmentReward(new SessionEquipment(arts.get(0))));
			}
			rewards.put(uuid, list);
		}
		return new RewardInstance(s, rewards, NodeType.TREASURE);
	}

	@Override
	public void setup() {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
			teleportRandomly(p);
		}
		
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			teleportRandomly(p);
		}
		super.setup();
		
		// Setup hologram
		Component text = Component.text("Open the enderchest and").appendNewline().append(Component.text("collect your reward!"));
		holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), text);
		startRewardDisplays();
	}

	private void startRewardDisplays() {
		Location chest = findRewardChest();
		for (Entry<UUID, PlayerSessionData> entry : s.getParty().entrySet()) {
			UUID uuid = entry.getKey();
			PlayerSessionData data = entry.getValue();
			Player player = data.getPlayer();
			if (player == null || rewardsFor(uuid).isEmpty()) continue;

			List<List<ItemStack>> displayRewards = rewardsFor(uuid).stream()
					.map(reward -> reward.getDisplayItems(data).stream().map(ItemStack::clone).toList())
					.toList();
			BukkitTask task = new BukkitRunnable() {
				private int rewardIndex;
				private int displayTick;
				private List<Location> targets = List.of();

				@Override
				public void run() {
					Player viewer = Bukkit.getPlayer(uuid);
					if (viewer == null || !viewer.isOnline() || rewardIndex >= displayRewards.size()) {
						removeRewardDisplays(uuid);
						rewardDisplayTasks.remove(uuid);
						cancel();
						return;
					}

					if (displayTick == 0) {
						removeRewardDisplays(uuid);
						targets = spawnRewardDisplays(viewer, chest, displayRewards.get(rewardIndex));
					}
					animateRewardDisplays(uuid, chest, targets, displayTick);

					displayTick++;
					if (displayTick >= DISPLAY_TICKS) {
						removeRewardDisplays(uuid);
						displayTick = 0;
						rewardIndex++;
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 10L, 1L);
			rewardDisplayTasks.put(uuid, task);
		}
	}

	private Location findRewardChest() {
		Location expected = spawn.clone().add(HOLO_X, 0, HOLO_Z);
		Block closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (int x = -4; x <= 4; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -4; z <= 4; z++) {
					Block block = expected.clone().add(x, y, z).getBlock();
					if (block.getType() != Material.ENDER_CHEST) continue;
					double distance = block.getLocation().add(0.5, 0.5, 0.5).distanceSquared(expected);
					if (distance < closestDistance) {
						closest = block;
						closestDistance = distance;
					}
				}
			}
		}
		Location anchor = closest == null ? expected.getBlock().getLocation() : closest.getLocation();
		return anchor.add(0.5, 0.8, 0.5);
	}

	private List<Location> spawnRewardDisplays(Player viewer, Location chest, List<ItemStack> items) {
		ArrayList<Location> targets = new ArrayList<Location>();
		List<RewardDisplay> displays = rewardDisplays.computeIfAbsent(viewer.getUniqueId(), key -> new ArrayList<RewardDisplay>());
		Vector towardViewer = viewer.getLocation().toVector().subtract(chest.toVector()).setY(0);
		if (towardViewer.lengthSquared() == 0) towardViewer.setZ(-1);
		towardViewer.normalize();
		Vector right = new Vector(-towardViewer.getZ(), 0, towardViewer.getX());
		double spacing = items.size() > 1 ? CHOICE_SPACING : 0;

		for (int index = 0; index < items.size(); index++) {
			double offset = (index - (items.size() - 1) / 2.0) * spacing;
			Location target = chest.clone().add(right.clone().multiply(offset)).add(0, 1.8, 0);
			targets.add(target);
			ItemStack item = items.get(index);
			ItemDisplay itemDisplay = chest.getWorld().spawn(chest, ItemDisplay.class, entity -> {
				entity.setItemStack(item);
				entity.setItemDisplayTransform(ItemDisplayTransform.FIXED);
				entity.setBillboard(Billboard.CENTER);
				entity.setBrightness(new Display.Brightness(15, 15));
				entity.setGlowColorOverride(Color.AQUA);
				entity.setGlowing(true);
				entity.setTeleportDuration(2);
				entity.setVisibleByDefault(false);
				Transformation transformation = entity.getTransformation();
				transformation.getScale().set(0.15F);
				entity.setTransformation(transformation);
			});
			ItemMeta meta = item.getItemMeta();
			Component name = meta.hasDisplayName() ? meta.displayName()
					: Component.translatable(item.getType().translationKey());
			TextDisplay nameDisplay = chest.getWorld().spawn(chest, TextDisplay.class, entity -> {
				entity.text(NeoRogue.withTextDisplayShadow(name));
				NeoRogue.configureHologram(entity);
				entity.setBillboard(Billboard.CENTER);
				entity.setBrightness(new Display.Brightness(15, 15));
				entity.setTeleportDuration(2);
				entity.setVisibleByDefault(false);
				entity.setLineWidth(100);
				Transformation transformation = entity.getTransformation();
				transformation.getScale().set(0.15F * NAME_SCALE);
				entity.setTransformation(transformation);
			});
			displays.add(new RewardDisplay(itemDisplay, nameDisplay));
			viewer.showEntity(NeoRogue.inst(), itemDisplay);
			viewer.showEntity(NeoRogue.inst(), nameDisplay);
		}
		viewer.spawnParticle(Particle.END_ROD, chest.clone().add(0, 0.5, 0), 20, 0.35, 0.3, 0.35, 0.03);
		viewer.playSound(chest, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9F, 1.4F);
		return targets;
	}

	private void animateRewardDisplays(UUID uuid, Location chest, List<Location> targets, int tick) {
		List<RewardDisplay> displays = rewardDisplays.get(uuid);
		if (displays == null || displays.size() != targets.size()) return;
		int index = 0;
		for (RewardDisplay display : displays) {
			Location target = targets.get(index);
			double nameOffset = displays.size() > 1 && index % 2 == 1 ? 0.75 : -0.55;
			index++;
			float scale = 1F;
			Location location;
			if (tick < POP_TICKS) {
				double progress = (tick + 1D) / POP_TICKS;
				double eased = 1 - Math.pow(1 - progress, 3);
				location = chest.clone().add(target.toVector().subtract(chest.toVector()).multiply(eased));
				location.add(0, Math.sin(Math.PI * progress) * 0.45, 0);
				scale = (float) (0.15 + 0.85 * eased);
			}
			else {
				location = target.clone().add(0, Math.sin((tick - POP_TICKS) * 0.18) * 0.08, 0);
				if (tick >= DISPLAY_TICKS - DISMISS_TICKS) {
					scale = (DISPLAY_TICKS - tick) / (float) DISMISS_TICKS;
				}
			}
			display.item().teleport(location);
			display.name().teleport(location.clone().add(0, nameOffset, 0));
			Transformation transformation = display.item().getTransformation();
			transformation.getScale().set(scale);
			transformation.getLeftRotation().rotateY(0.12F);
			display.item().setTransformation(transformation);
			Transformation nameTransformation = display.name().getTransformation();
			nameTransformation.getScale().set(scale * NAME_SCALE);
			display.name().setTransformation(nameTransformation);
		}
	}

	private void removeRewardDisplays(UUID uuid) {
		List<RewardDisplay> displays = rewardDisplays.remove(uuid);
		if (displays == null) return;
		for (RewardDisplay display : displays) {
			if (display.item().isValid()) display.item().remove();
			if (display.name().isValid()) display.name().remove();
		}
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost().equals(data.getUniqueId())) continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		UUID uuid = data.getUniqueId();
		String line = rewardsFor(uuid).isEmpty() ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "★ ";
		}
		line += data.getData().getDisplay() + " (" + Math.round(data.getHealth()) + " / " + (int) data.getMaxHealth()
				+ "§c♥§f)";
		return line;
	}

	public HashMap<UUID, ArrayList<Reward>> getRewards() {
		return rewards;
	}

	// Null-safe access to a player's reward list. Missing entries (e.g. a fight that generated no rewards
	// for someone, or an entirely empty reward map) are treated as an empty, already-claimed list, so
	// lookups never NPE and the player can pass through the reward screen as if they had claimed all.
	private ArrayList<Reward> rewardsFor(UUID uuid) {
		return rewards.computeIfAbsent(uuid, k -> new ArrayList<Reward>());
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		for (BukkitTask task : rewardDisplayTasks.values()) {
			task.cancel();
		}
		rewardDisplayTasks.clear();
		for (UUID uuid : new HashSet<UUID>(rewardDisplays.keySet())) {
			removeRewardDisplays(uuid);
		}
		super.cleanup(pluginDisable);
		if (holo != null) holo.remove();
	}

	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getHand() != EquipmentSlot.HAND) return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
			new SpectateSelectInventory(s, e.getPlayer(), null, true);
		}
		else {
			super.handleSpectatorInteractEvent(e);
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);
		
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
			Player p = e.getPlayer();
			UUID uuid = p.getUniqueId();
			if (rewardsFor(uuid).isEmpty()) {
				if (!onRewardClaim()) {
					new SpectateSelectInventory(s, e.getPlayer(), s.getParty().get(uuid), true);
				}
				return;
			}
			p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
			new RewardInventory(s.getParty().get(uuid), rewardsFor(uuid));
		}
		else {
			super.handleInteractEvent(e);
		}
	}
	
	public void spectateRewards(Player spectator, UUID viewed) {
		new RewardInventory(s.getParty().get(viewed), rewardsFor(viewed), spectator);
	}
	
	public boolean onRewardClaim() {
		updateBoardLines();
		for (ArrayList<Reward> rewards : this.rewards.values()) {
			if (!rewards.isEmpty()) return false;
		}

		NodeSelectInstance next = NodeSelectInstance.create(s);
		new BukkitRunnable() {
			public void run() {
				if (!s.isBusy() && s.canSetInstance(next)) {
					s.broadcast("Returning to node select...");
					s.setBusy(true);
					new BukkitRunnable() {
						public void run() {
							s.setInstance(next);
							s.setBusy(false);

							// Boss killed, region completed
							if (previous == NodeType.BOSS) {
								double healMult = NotorietySetting.REDUCED_BOSS_HEAL.isActive(s)
										? NotorietySetting.BOSS_HEAL_MULTIPLIER : 1.0;
								s.getParty().values().forEach(data -> {
									Player p = data.getPlayer();
									double missing = data.getMaxHealth() - data.getHealth();
									data.setHealth(data.getHealth() + (missing * healMult));
									PlayerSessionData.heal.play(p, p);
									Sounds.levelup.play(p, p);
								});
								s.updateAllBoards();
								s.incrementRegionsCompleted();
								// Pay the caravan region-completion reward now that the party has reached the
								// next region (its title just showed via setInstance above). Runs once here
								// rather than in NodeSelectInstance.setup so it isn't re-paid on relog.
								RegionType completed = RegionType.getPreviousRegion(s.getRegion().getType());
								if (completed != null) RunReward.awardRegionCompletion(s, completed);
							}
						}
					}.runTaskLater(NeoRogue.inst(), 40L);
				}
			}
		}.runTaskLater(NeoRogue.inst(), 1);
		return true;
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, ArrayList<Reward>> ent : rewards.entrySet()) {
			String serialized = "";
			for (Reward r : ent.getValue()) {
				serialized += r.serialize() + ",";
			}
			
			PlayerSessionData data = party.get(ent.getKey());
			data.setInstanceData(serialized);
		}
		return InstanceType.REWARD.prefix() + previous.name();
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {
		BukkitTask task = rewardDisplayTasks.remove(p.getUniqueId());
		if (task != null) task.cancel();
		removeRewardDisplays(p.getUniqueId());
		rewards.remove(p.getUniqueId());
		onRewardClaim();
	}
}
