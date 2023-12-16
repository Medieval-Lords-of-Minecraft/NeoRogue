package me.neoblade298.neorogue.session.chance;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.*;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;

public class ChanceInstance extends EditInventoryInstance {
	private static final int CHANCE_X = 6, CHANCE_Z = 101;

	private ChanceSet set;
	private HashMap<UUID, ChanceStage> stage = new HashMap<UUID, ChanceStage>();
	private Instance nextInstance; // For taking you directly from this instance to another

	public ChanceInstance() {
	}

	public ChanceInstance(String data, HashMap<UUID, PlayerSessionData> party) {
		data = data.substring("CHANCE:".length());
		String[] split = data.split("-");
		set = ChanceSet.get(split[0]);
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			stage.put(ent.getKey(), ChanceStage.get(ent.getValue().getInstanceData()));
		}
	}

	@Override
	public void start(Session s) {
		this.s = s;
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + CHANCE_X - 0.5), 64,
				s.getZOff() + CHANCE_Z);

		// Pick a random chance set
		set = ChanceSet.getSet(s.getArea().getType());

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
		e.setCancelled(true);

		Player p = e.getPlayer();
		if (e.getClickedBlock().getType() == Material.LECTERN) {
			new ChanceInventory(p, this, set, stage.get(p.getUniqueId()));
			return;
		}
	}

	public Session getSession() {
		return s;
	}

	public void advanceStage(UUID uuid, ChanceStage stage) {
		// Only runs if we're out of stages
		if (stage == null) {
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
					s.setInstance(nextInstance == null ? new NodeSelectInstance() : nextInstance);
				}
			}.runTaskLater(NeoRogue.inst(), 60L);
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

	public void setNextInstance(Instance inst) {
		this.nextInstance = inst;
	}

	public Instance getNextInstance() {
		return nextInstance;
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (PlayerSessionData data : party.values()) {
			data.setInstanceData(this.stage.get(data.getPlayer().getUniqueId()).getId());
		}
		return "CHANCE:" + set.getId();
	}
}
