package me.neoblade298.neorogue.session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;

public abstract class Instance {
	protected Session s;
	protected Location spawn;
	protected ArrayList<String> playerLines = new ArrayList<String>(), spectatorLines = new ArrayList<String>();
	public abstract void start();
	public abstract void cleanup();
	public abstract void handleInteractEvent(PlayerInteractEvent e);
	public abstract void handlePlayerKickEvent(Player kicked);
	public abstract String serialize(HashMap<UUID, PlayerSessionData> party);
	public Instance(Session s) {
		this.s = s;
	}
	public Instance(Session s, double spawnX, double spawnZ) {
		this.s = s;
		spawn = new Location(Bukkit.getWorld(Area.WORLD_NAME), -(s.getXOff() + spawnX), 64, s.getZOff() + spawnZ);
	}
	
	public void handleSpectatorInteractEvent(PlayerInteractEvent e) {
		
	}
	
	public void handlePlayerRejoin(Player p) {
		p.teleport(spawn);
		p.setMaximumNoDamageTicks(0);
		p.setHealthScaled(true);
	}
	
	public void handlePlayerLeave(Player p) {
		
	}
	
	public Location getSpawn() {
		return spawn;
	}

	// Done so that players don't push each other around
	public void teleportRandomly(Player p) {
		double dx = NeoRogue.gen.nextDouble(-1, 1);
		double dz = NeoRogue.gen.nextDouble(-1, 1);
		p.teleport(spawn.clone().add(dx, 0, dz));
	}
	
	public static Instance deserialize(Session s, ResultSet row, HashMap<UUID, PlayerSessionData> party) throws SQLException {
		String data = row.getString("instanceData");
		
		if (data.startsWith("SHRINE")) {
			return new ShrineInstance(s, data, party);
		}
		else if (data.startsWith("NODESELECT")) {
			return new NodeSelectInstance(s);
		}
		else if (data.startsWith("REWARD")) {
			return new RewardInstance(s, party, false); // boolean is literally just to differentiate constructors
		}
		else if (data.startsWith("SHOP")) {
			return new ShopInstance(s, party);
		}
		else if (data.startsWith("CHANCE")) {
			return new ChanceInstance(s, data, party);
		}
		return null;
	}

	public ArrayList<String> getPlayerLines() {
		return playerLines;
	}

	public ArrayList<String> getSpectatorLines() {
		return spectatorLines;
	}

	public abstract void updateBoardLines();
}
