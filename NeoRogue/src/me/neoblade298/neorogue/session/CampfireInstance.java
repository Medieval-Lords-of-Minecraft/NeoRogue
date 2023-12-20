package me.neoblade298.neorogue.session;

import java.util.HashMap;
import java.util.HashSet;
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

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class CampfireInstance extends EditInventoryInstance {
	static final int REST_X = 6, REST_Z = 84;
	private static final int INIT_STATE = 0, REST_STATE = 1, UPGRADE_STATE = 2;
	private int state = 0;
	private Location center;
	private HashSet<UUID> notUsed = new HashSet<UUID>();
	
	public CampfireInstance() {}
	
	public CampfireInstance(String data, HashMap<UUID, PlayerSessionData> party) {
		state = Integer.parseInt(data.substring(data.length() - 1));
		
		for (PlayerSessionData pd : party.values()) {
			if (pd.getInstanceData().equals("F")) {
				notUsed.add(pd.getPlayer().getUniqueId());
			}
		}
	}

	@Override
	public void start(Session s) {
		this.s = s;
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + REST_X - 0.5), 64, s.getZOff() + REST_Z);
		center = spawn.clone().add(0, 0, 2);
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			notUsed.add(p.getUniqueId());
			p.teleport(spawn);
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
		UUID uuid = p.getUniqueId();
		if (e.getClickedBlock().getType() == Material.STONE_BRICK_STAIRS && state == INIT_STATE) {
			if (!s.getHost().equals(uuid)) {
				Util.displayError(p, "The host must first choose what to do!");
				return;
			}
			
			// open host inventory
			new CampfireChoiceInventory(p, this);
			return;
		}

		if (e.getClickedBlock().getType() == Material.ANVIL && notUsed.contains(uuid)) {
			new CampfireUpgradeInventory(p, this);
		}
	}

	public void chooseState(boolean rest) {
		state = rest ? REST_STATE : UPGRADE_STATE;
		s.broadcast("The host has chosen to <yellow>" + (rest ? "rest" : "upgrade"));
		
		if (rest) {
			center.getBlock().setType(Material.CAMPFIRE);
			notUsed.clear();
			
			for (PlayerSessionData data : s.getParty().values()) {
				data.healPercent(0.25);
			}
			
			returnToNodes();
		}
		else {
			center.getBlock().setType(Material.ANVIL);
			s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
		}
	}
	
	public void useUpgrade(UUID uuid) {
		notUsed.remove(uuid);
		if (notUsed.isEmpty()) {
			returnToNodes();
		}
	}
	
	public void returnToNodes() {
		if (state == UPGRADE_STATE) s.broadcast("Everyone is ready! Returning you to node select.");
		s.setInstance(new NodeSelectInstance());
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		for (Entry<UUID, PlayerSessionData> ent : party.entrySet()) {
			ent.getValue().setInstanceData(notUsed.contains(ent.getKey()) ? "F" : "T");
		}
		return "CAMPFIRE:" + state;
	}

	@Override
	public void teleportPlayer(Player p) {
		p.teleport(spawn);
		returnToNodes();
	}
}
