package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.session.*;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChanceInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.CHANCE_X + 6.5, SPAWN_Z = Session.CHANCE_Z + 1.5,
			HOLO_X = 0, HOLO_Y = 2, HOLO_Z = 3;
	private static ParticleContainer part = new ParticleContainer(Particle.FLAME).count(25).speed(0.1).spread(0.2, 0.2);

	private ChanceSet set;
	private HashMap<UUID, ChanceStage> stage = new HashMap<UUID, ChanceStage>();
	private Instance nextInstance; // For taking you directly from this instance to another
	private boolean returning;
	private Hologram holo;
	private Block candleBlock;

	public ChanceInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}
	
	public ChanceInstance(Session s, String setId) {
		this(s);
		this.set = ChanceSet.get(setId);
	}
	
	public ChanceInstance(Session s, ChanceSet set) {
		this(s);
		this.set = set;
	}

	public ChanceInstance(Session s, String data, HashMap<UUID, PlayerSessionData> party) {
		this(s);
		data = data.substring("CHANCE:".length());
		String[] split = data.split("-");
		set = ChanceSet.get(split[0]);
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			String id = ent.getValue().getInstanceData();
			if (id == null) continue;
			stage.put(ent.getKey(), set.getStage(id));
		}
		
		if (split.length > 1) {
			nextInstance = FightInstance.deserializeInstanceData(s, party, split[1]);
		}
	}
	
	public ChanceSet getSet() {
		return set;
	}

	@Override
	public void start() {
		// Pick a random chance set if not already picked
		if (set == null) {
			set = ChanceSet.getSet(s.getArea().getType());
		}
		set.initialize(s, this);

		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(spawn);
			stage.put(data.getPlayer().getUniqueId(), set.getInitialStage());
		}
		for (UUID uuid : s.getSpectators()) {
			Player p = Bukkit.getPlayer(uuid);
			p.teleport(spawn);
		}

		// Setup hologram
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("Right click the pillar below!");
		Plot plot = s.getPlot();
		holo = DHAPI.createHologram(plot.getXOffset() + "-" + plot.getZOffset() + "-shop", spawn.clone().add(HOLO_X, HOLO_Y, HOLO_Z), lines);
		candleBlock = holo.getLocation().add(0, -1, 0).getBlock();
	}

	@Override
	public void cleanup() {
		Candle candle = (Candle) candleBlock.getBlockData();
		candle.setLit(false);
		candleBlock.setBlockData(candle);
		holo.delete();
	}
	
	@Override
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;

		Player p = e.getPlayer();
		if (e.getClickedBlock().getType() == Material.QUARTZ_PILLAR || e.getClickedBlock().getType() == Material.LIGHT_GRAY_CANDLE) {
			if (set.isIndividual()) {
				new SpectateSelectInventory(s, p, true);
			}
			else {
				spectatePlayer(p, s.getHost());
			}
		}
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);

		Player p = e.getPlayer();
		if (e.getClickedBlock().getType() == Material.QUARTZ_PILLAR || e.getClickedBlock().getType() == Material.LIGHT_GRAY_CANDLE) {
			// If we're stuck in chance event due to someone's inventory full or cursed
			if (stage.isEmpty()) {
				returnPlayers();
				return;
			}
			
			if (!stage.containsKey(p.getUniqueId())) {
				Util.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, false);
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
	}

	public Session getSession() {
		return s;
	}
	
	public void spectatePlayer(Player spectator, UUID uuid) {
		new ChanceInventory(s.getParty().get(uuid), this, set, stage.get(uuid), spectator);
	}

	public void advanceStage(UUID uuid, ChanceStage stage) {
		// Finished with chance
		if (stage == null) {
			if (!set.isIndividual()) {
				this.stage.clear();
				returnPlayers();
			}
			else {
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
					this.stage.put(id, stage);
				}
			}
			else {
				this.stage.put(uuid, stage);
			}
		}
	}
	
	public PlayerSessionData chooseRandomPartyMember() {
		Collection<PlayerSessionData> party = s.getParty().values();
		return (PlayerSessionData) party.toArray()[NeoRogue.gen.nextInt(party.size())];
	}
	
	private void returnPlayers() {
		if (returning) return;
		s.broadcastSound(Sound.ENTITY_BLAZE_SHOOT);
		part.spawn(holo.getLocation());
		Candle candle = (Candle) candleBlock.getBlockData();
		candle.setLit(true);
		candleBlock.setBlockData(candle);
		returning = true;
		new BukkitRunnable() {
			public void run() {
				if (nextInstance != null) {
					String instDisplay = null;
					if (nextInstance instanceof FightInstance) {
						instDisplay = "fight";
					}
					else if (nextInstance instanceof ShrineInstance) {
						instDisplay = "shrine";
					}
					else if (nextInstance instanceof RewardInstance) {
						instDisplay = "claim rewards";
					}
					else if (nextInstance instanceof ShopInstance) {
						instDisplay = "shop";
					}
					s.broadcast(Component.text("Sending you to " + instDisplay + "..."));
				}
				else {
					s.broadcast(Component.text("Sending you back to node select..."));
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20L);

		new BukkitRunnable() {
			public void run() {
				returning = false;
				s.setInstance(nextInstance == null ? new NodeSelectInstance(s) : nextInstance);
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
	}

	public void setNextInstance(Instance inst) {
		this.nextInstance = inst;
	}

	public Instance getNextInstance() {
		return nextInstance;
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (PlayerSessionData data : party.values()) {
			UUID uuid = data.getPlayer().getUniqueId();
			if (!this.stage.containsKey(uuid)) {
				data.setInstanceData(null);
				continue;
			}
			data.setInstanceData(this.stage.get(data.getPlayer().getUniqueId()).getId());
		}
		String next = nextInstance instanceof FightInstance ? "-" + ((FightInstance) nextInstance).serializeInstanceData() : "";
		return "CHANCE:" + set.getId() + next;
	}
}
