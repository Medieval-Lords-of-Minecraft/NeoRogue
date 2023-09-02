package me.neoblade298.neorogue.session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.player.PlayerInteractEvent;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.chance.ChanceInstance;

public interface Instance {
	public void start(Session s);
	public void cleanup();
	public void handleInteractEvent(PlayerInteractEvent e);
	public String serialize(HashMap<UUID, PlayerSessionData> party);
	
	public static Instance deserialize(ResultSet row, HashMap<UUID, PlayerSessionData> party) throws SQLException {
		String data = row.getString("instanceData");
		
		if (data.startsWith("CAMPFIRE")) {
			return new CampfireInstance(data, party);
		}
		else if (data.startsWith("NODESELECT")) {
			return new NodeSelectInstance();
		}
		else if (data.startsWith("REWARD")) {
			return new RewardInstance(party, false); // boolean is literally just to differentiate constructors
		}
		else if (data.startsWith("SHOP")) {
			return new ShopInstance(party);
		}
		else if (data.startsWith("CHANCE")) {
			return new ChanceInstance(data);
		}
		return null;
	}
}
