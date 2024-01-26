package me.neoblade298.neorogue.session.chance;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.*;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChanceInstance extends EditInventoryInstance {
	private static final double SPAWN_X = Session.CHANCE_X + 6.5, SPAWN_Z = Session.CHANCE_Z + 3.5;

	private ChanceSet set;
	private HashMap<UUID, ChanceStage> stage = new HashMap<UUID, ChanceStage>();
	private Instance nextInstance; // For taking you directly from this instance to another
	private boolean returning;

	public ChanceInstance(Session s) {
		super(s, SPAWN_X, SPAWN_Z);
	}
	
	public ChanceInstance(Session s, String setId) {
		this(s);
		this.set = ChanceSet.get(setId);
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
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + Session.CHANCE_X - 0.5), 64,
				s.getZOff() + Session.CHANCE_Z);

		// Pick a random chance set if not already picked
		if (set == null) {
			set = ChanceSet.getSet(s.getArea().getType());
		}
		set.initialize(s, this);

		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(spawn);
			stage.put(data.getPlayer().getUniqueId(), set.getInitialStage());
		}
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		e.setCancelled(true);

		Player p = e.getPlayer();
		if (e.getClickedBlock().getType() == Material.LECTERN) {
			// If we're stuck in chance event due to someone's inventory full or cursed
			if (stage.isEmpty()) {
				returnPlayers();
				return;
			}
			
			if (!stage.containsKey(p.getUniqueId())) {
				Util.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, false);
				Util.msg(p, "You've already completed the chance event! You're waiting for:");
				for (UUID uuid : stage.keySet()) {
					Player waiting = Bukkit.getPlayer(uuid);
					Component c = waiting != null ? waiting.displayName() : Component.text("(Offline) " + uuid);
					Util.msg(p, SharedUtil.color("- ").append(c.color(NamedTextColor.RED)));
				}
				return;
			}
			new ChanceInventory(p, this, set, stage.get(p.getUniqueId()));
		}
	}

	public Session getSession() {
		return s;
	}

	public void advanceStage(UUID uuid, ChanceStage stage) {
		// Only runs if we're out of stages
		if (stage == null) {
			if (!set.isIndividual()) {
				returnPlayers();
			}
			else {
				this.stage.remove(uuid);
				if (this.stage.size() == 0) {
					returnPlayers();
				}
			}
		}
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
	
	private void returnPlayers() {
		if (returning) return;
		s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
		returning = true;
		new BukkitRunnable() {
			public void run() {
				if (nextInstance != null) {
					String instDisplay = null;
					if (nextInstance instanceof FightInstance) {
						instDisplay = "fight";
					}
					else if (nextInstance instanceof CampfireInstance) {
						instDisplay = "campfire";
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
