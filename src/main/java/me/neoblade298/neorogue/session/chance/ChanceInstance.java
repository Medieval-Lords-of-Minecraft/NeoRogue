package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.region.RegionType.Layout;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.analytics.ChanceChoiceSnapshot;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.instances.EditInventoryInstance;
import me.neoblade298.neorogue.session.instances.Instance;
import me.neoblade298.neorogue.session.instances.InstanceType;
import me.neoblade298.neorogue.session.instances.NodeSelectInstance;
import me.neoblade298.neorogue.session.instances.ShopInstance;
import me.neoblade298.neorogue.session.instances.ShrineInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.CHANCE_X + 6.5, SPAWN_Z = Session.CHANCE_Z + 1.5, HOLO_X = 0, HOLO_Y = 3, HOLO_Z = 6;
	private static final String INSTANCE_DATA_SEPARATOR = "::";
	private static final ParticleContainer part = new ParticleContainer(Particle.FLAME).count(25).speed(0.1).spread(0.2, 0.2);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_NOTE_BLOCK_PLING);

	private ChanceSet set;
	private HashMap<UUID, ChanceStage> stage = new HashMap<UUID, ChanceStage>();
	private HashMap<String, String> eventData = new HashMap<String, String>();
	// Choice snapshots captured at click time, keyed by the picking player. Flushed to analytics
	// when the commit lands in advanceStage; a cancelled interactive UI never reaches that point.
	private HashMap<UUID, ChanceChoiceSnapshot> pendingPicks = new HashMap<UUID, ChanceChoiceSnapshot>();
	private Instance nextInstance; // For taking you directly from this instance to another
	private boolean returning;
	private TextDisplay holo;
	private Block candleBlock;
	private boolean deserialized = false;

	public ChanceInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
		spectatorLines = playerLines;
	}
	
	public ChanceInstance(Session s, String setId) {
		this(s);
		this.set = ChanceSet.get(setId);
	}
	
	public ChanceInstance(Session s, ChanceSet set) {
		this(s);
		this.set = set;
	}

	public static ChanceInstance create(Session s) {
		if (s.getRegion().getType().getLayout() == Layout.TUTORIAL) {
			return new TutorialChanceInstance(s);
		}
		return new ChanceInstance(s);
	}

	public static ChanceInstance create(Session s, String setId) {
		if (s.getRegion().getType().getLayout() == Layout.TUTORIAL) {
			return new TutorialChanceInstance(s, setId);
		}
		return new ChanceInstance(s, setId);
	}

	public static ChanceInstance create(Session s, ChanceSet set) {
		if (s.getRegion().getType().getLayout() == Layout.TUTORIAL) {
			return new TutorialChanceInstance(s, set);
		}
		return new ChanceInstance(s, set);
	}

	// Deserialization
	public ChanceInstance(Session s, String data, HashMap<UUID, PlayerSessionData> party) {
		this(s);
		data = data.substring("CHANCE:".length());
		
		// Parse event data section (between | and optional -)
		String setAndData, fightData = null;
		int dashIdx = data.indexOf('-');
		if (dashIdx != -1) {
			setAndData = data.substring(0, dashIdx);
			fightData = data.substring(dashIdx + 1);
		} else {
			setAndData = data;
		}
		
		int pipeIdx = setAndData.indexOf('|');
		if (pipeIdx != -1) {
			set = ChanceSet.get(setAndData.substring(0, pipeIdx));
			String eventDataStr = setAndData.substring(pipeIdx + 1);
			if (!eventDataStr.isEmpty()) {
				for (String entry : eventDataStr.split(",")) {
					String[] kv = entry.split("=", 2);
					if (kv.length == 2) eventData.put(kv[0], kv[1]);
				}
			}
		} else {
			set = ChanceSet.get(setAndData);
		}
		
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			String id = getStageIdFromInstanceData(ent.getValue().getInstanceData());
			if (id == null)
				continue;
			ChanceStage stg = set.getStage(id);
			stage.put(ent.getKey(), stg);
		}
		
		if (fightData != null) {
			nextInstance = FightInstance.deserializeInstanceData(s, party, fightData);
		}
		deserialized = true;
	}
	
	public ChanceSet getSet() {
		return set;
	}
	
	public void setEventData(String key, String value) {
		eventData.put(key, value);
	}
	
	public String getEventData(String key) {
		return eventData.get(key);
	}

	@Override
	public void setup() {
		// Pick a random chance set if not already picked
		if (set == null) {
			set = ChanceSet.getSet(s.getRegion().getType());
		}
		set.initialize(s, this);

		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			p.teleport(spawn);
			if (!deserialized) stage.put(p.getUniqueId(), set.getInitialStage());
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			p.teleport(spawn);
		}
		super.setup();

		// Setup hologram
		if (!set.isIndividual() || set.hasSingleStage()) {
			holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), buildHologramText(set.getInitialStage()));
		} else {
			holo = NeoRogue.createHologram(spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), Component.text("Right click the pillar below!"));
		}
		candleBlock = spawn.clone().add(0, 1, 3).getBlock();
	}

	@Override
	public void updateBoardLines() {
		playerLines.clear();
		playerLines.add(createBoardLine(s.getParty().get(s.getHost()), true));

		ArrayList<PlayerSessionData> sorted = new ArrayList<PlayerSessionData>();
		for (PlayerSessionData data : s.getParty().values()) {
			if (s.getHost().equals(data.getUniqueId()))
				continue;
			sorted.add(data);
		}
		Collections.sort(sorted);
		for (PlayerSessionData data : sorted) {
			playerLines.add(createBoardLine(data, false));
		}
	}

	private String createBoardLine(PlayerSessionData data, boolean isHost) {
		UUID uuid = data.getUniqueId();
		String line = !stage.containsKey(uuid) ? "§a✓ §f" : "§c✗ §f";
		if (isHost) {
			line += "★ ";
		}
		line += data.getData().getDisplay() + " (" + Math.round(data.getHealth()) + " / " + (int) data.getMaxHealth()
				+ "§c♥§f)";
		return line;
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
		Candle candle = (Candle) candleBlock.getBlockData();
		candle.setLit(false);
		candleBlock.setBlockData(candle);
		holo.remove();
	}
	
	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getHand() != EquipmentSlot.HAND)
			return;

		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (e.getClickedBlock().getType() == Material.QUARTZ_PILLAR || e.getClickedBlock().getType() == Material.LIGHT_GRAY_CANDLE)) {
			if (set.isIndividual()) {
				new SpectateSelectInventory(s, p, null, true);
			} else {
				spectatePlayer(p, s.getHost());
			}
		}
		else {
			super.handleSpectatorInteractEvent(e);
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		e.setCancelled(true);

		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && 
				(e.getClickedBlock().getType() == Material.QUARTZ_PILLAR || e.getClickedBlock().getType() == Material.LIGHT_GRAY_CANDLE)) {
			// If we're stuck in chance event due to someone's inventory full or cursed
			if (stage.isEmpty()) {
				returnPlayers();
				return;
			}
			
			if (!stage.containsKey(p.getUniqueId())) {
				sc.play(p, p, Audience.ORIGIN);
				Util.msgRaw(p, "You've already completed the chance event! You're waiting for:");
				for (UUID uuid : stage.keySet()) {
					Player waiting = Bukkit.getPlayer(uuid);
					Component c = waiting != null ? waiting.displayName() : Component.text("(Offline) " + uuid);
					Util.msgRaw(p, SharedUtil.color("- ").append(c.color(NamedTextColor.RED)));
				}
				return;
			}
			new ChanceInventory(p, this, set, stage.get(p.getUniqueId()));
		}
		else {
			super.handleInteractEvent(e);
		}
	}

	public Session getSession() {
		return s;
	}
	
	public void spectatePlayer(Player spectator, UUID uuid) {
		new ChanceInventory(s.getParty().get(uuid), this, set, stage.get(uuid), spectator);
	}

	// Stashes a choice snapshot captured at click time; flushed to analytics on the next
	// advanceStage for this player (i.e. when the pick actually commits).
	public void setPendingPick(UUID uuid, ChanceChoiceSnapshot snap) {
		pendingPicks.put(uuid, snap);
	}

	public void advanceStage(UUID uuid, ChanceStage next) {
		// Flush the pick captured when this player clicked the choice. Reaching advanceStage means
		// the choice actually committed (interactive cancels reopen the inventory and never get here).
		ChanceChoiceSnapshot snap = pendingPicks.remove(uuid);
		if (snap != null) AnalyticsManager.recordChanceChoice(snap);

		// Finished with chance
		if (next == null) {
			if (!set.isIndividual()) {
				this.stage.clear();
				returnPlayers();
			} else {
				this.stage.remove(uuid);
				if (this.stage.isEmpty()) {
					returnPlayers();
				}
			}
		}
		
		// Normal behavior
		else {
			if (!set.isIndividual()) {
				for (UUID id : this.stage.keySet()) {
					this.stage.put(id, next);
				}
				holo.text(buildHologramText(next));
			} else {
				this.stage.put(uuid, next);
			}
		}
	}
	
	private Component buildHologramText(ChanceStage stg) {
		Component text = set.getDisplay().append(Component.newline());
		for (TextComponent line : stg.description) {
			text = text.append(((TextComponent) line.colorIfAbsent(NamedTextColor.GRAY)
					.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE))).append(Component.newline());
		}
		text = text.append(Component.text("Right click the pillar below!", NamedTextColor.GOLD));
		return text;
	}
	
	public PlayerSessionData chooseRandomPartyMember() {
		Collection<PlayerSessionData> party = s.getParty().values();
		return (PlayerSessionData) party.toArray()[NeoRogue.gen.nextInt(party.size())];
	}

	// Must be run later to avoid player open inventory error
	private void returnPlayers() {
		new BukkitRunnable() {
			@Override
			public void run() {
				returnPlayersTask();
			}
		}.runTaskLater(NeoRogue.inst(), 1);
	}
	
	private void returnPlayersTask() {
		if (returning)
			return;

		Instance next = nextInstance == null ? NodeSelectInstance.create(s) : nextInstance;
		if (!s.canSetInstance(next)) {
			return;
		}

		s.broadcastSound(Sound.ENTITY_BLAZE_SHOOT);
		part.play(holo.getLocation());
		Candle candle = (Candle) candleBlock.getBlockData();
		candle.setLit(true);
		candleBlock.setBlockData(candle);
		returning = true;
		s.setBusy(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (nextInstance != null) {
					String instDisplay = null;
					if (nextInstance instanceof FightInstance) {
						instDisplay = "fight";
					} else if (nextInstance instanceof ShrineInstance) {
						instDisplay = "shrine";
					} else if (nextInstance instanceof RewardInstance) {
						instDisplay = "claim rewards";
					} else if (nextInstance instanceof ShopInstance) {
						instDisplay = "shop";
					}
					else if (nextInstance instanceof NodeSelectInstance) {
						instDisplay = "node select";
					}
					s.broadcast(Component.text("Sending you to " + instDisplay + "..."));
				} else {
					s.broadcast(Component.text("Sending you back to node select..."));
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20L);

		new BukkitRunnable() {
			@Override
			public void run() {
				returning = false;
				s.setInstance(next);
				s.setBusy(false);
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
	}

	public void setNextInstance(Instance inst) {
		this.nextInstance = inst;
	}

	public Instance getNextInstance() {
		return nextInstance;
	}

	public HashMap<UUID, ChanceStage> getStages() {
		return stage;
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (PlayerSessionData data : party.values()) {
			UUID uuid = data.getPlayer().getUniqueId();
			if (!this.stage.containsKey(uuid)) {
				data.setInstanceData(null);
				continue;
			}
			ChanceStage stg = this.stage.get(uuid);
			String payload = getInstanceDataPayload(data);
			if (payload != null && !payload.isEmpty()) {
				data.setInstanceData(stg.getId() + INSTANCE_DATA_SEPARATOR + payload);
			}
			else {
				data.setInstanceData(stg.getId());
			}
		}
		
		// Build event data section
		String eventDataStr = "";
		if (!eventData.isEmpty()) {
			StringBuilder sb = new StringBuilder("|");
			boolean first = true;
			for (var entry : eventData.entrySet()) {
				if (!first) sb.append(",");
				first = false;
				sb.append(entry.getKey()).append("=").append(entry.getValue());
			}
			eventDataStr = sb.toString();
		}
		
		String next = nextInstance instanceof FightInstance ? "-" + ((FightInstance) nextInstance).serializeInstanceData() : "";
		return InstanceType.CHANCE.prefix() + set.getId() + eventDataStr + next;
	}

	private static String getStageIdFromInstanceData(String raw) {
		if (raw == null || raw.equals("null")) return null;
		int sep = raw.indexOf(INSTANCE_DATA_SEPARATOR);
		return sep == -1 ? raw : raw.substring(0, sep);
	}

	public static String getInstanceDataPayload(PlayerSessionData data) {
		String raw = data.getInstanceData();
		if (raw == null || raw.equals("null")) return null;
		int sep = raw.indexOf(INSTANCE_DATA_SEPARATOR);
		if (sep == -1 || sep + INSTANCE_DATA_SEPARATOR.length() >= raw.length()) return null;
		return raw.substring(sep + INSTANCE_DATA_SEPARATOR.length());
	}
	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {
		stage.remove(p.getUniqueId());
	}
}
