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
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class LoseInstance extends EditInventoryInstance {
	private static final int LOSE_X = 6, LOSE_Z = 84;
	
	public LoseInstance() {}

	@Override
	public void start(Session s) {
		this.s = s;
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + LOSE_X - 0.5), 64, s.getZOff() + LOSE_Z);
		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(spawn);
		}
		
		s.get
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		return null;
	}

	@Override
	public void teleportPlayer(Player p) {
		p.teleport(spawn);
	}

	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		e.setCancelled(true);
	}
}
