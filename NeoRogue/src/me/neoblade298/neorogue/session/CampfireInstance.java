package me.neoblade298.neorogue.session;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class CampfireInstance implements EditInventoryInstance {
	private static final int REST_X = 6, REST_Z = 84;
	private static final int INIT_STATE = 0, REST_STATE = 1, UPGRADE_STATE = 2;
	private int state = 0;
	private Location center;
	private HashSet<UUID> notUsed = new HashSet<UUID>();
	
	private Session s;

	@Override
	public void start(Session s) {
		this.s = s;
		Location loc = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + REST_X - 0.5), 64, s.getZOff() + REST_Z);
		center = loc.clone().add(0, 0, 2);
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			notUsed.add(p.getUniqueId());
			p.teleport(loc);
		}
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		e.setCancelled(true);

		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (e.getClickedBlock().getType() == Material.STONE_BRICK_STAIRS && state == INIT_STATE) {
			if (!s.getHost().equals(uuid)) {
				Util.displayError(p, "&cThe host must first choose what to do!");
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
		s.broadcast("&7The host has chosen to &e" + (rest ? "rest" : "upgrade"));
		
		if (rest) {
			center.getBlock().setType(Material.CAMPFIRE);
			s.broadcastSound(Sound.ENTITY_PLAYER_LEVELUP);
			notUsed.clear();
			
			for (PlayerSessionData data : s.getParty().values()) {
				data.setHealth(data.getHealth() + (data.getMaxHealth() * 0.25));
			}
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
		if (state == UPGRADE_STATE) s.broadcast("&7Everyone is ready! Returning you to node select...");
		new BukkitRunnable() {
			public void run() {
				s.setInstance(new NodeSelectInstance());
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
	}
}