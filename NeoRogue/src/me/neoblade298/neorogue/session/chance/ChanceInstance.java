package me.neoblade298.neorogue.session.chance;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.EditInventoryInstance;
import me.neoblade298.neorogue.session.Session;

public class ChanceInstance implements EditInventoryInstance {
	private static final int REST_X = 6, REST_Z = 92;
	
	private Session s;

	@Override
	public void start(Session s) {
		this.s = s;
		Location loc = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + REST_X - 0.5), 64, s.getZOff() + REST_Z);
		for (PlayerSessionData data : s.getParty().values()) {
			data.getPlayer().teleport(loc);
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
		UUID uuid = p.getUniqueId();
		if (e.getClickedBlock().getType() == Material.LECTERN) {
			
			// open inventory
			return;
		}
	}
}
