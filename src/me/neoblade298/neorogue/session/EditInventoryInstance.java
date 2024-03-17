package me.neoblade298.neorogue.session;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class EditInventoryInstance extends Instance {
	public EditInventoryInstance(Session s, double spawnX, double spawnZ) {
		super(s, spawnX, spawnZ);
	}
	
	@Override
	public void start() {
		for (PlayerSessionData data : s.getParty().values()) {
			data.setupInventory();
			data.updateBoardLines();
		}
		
		for (UUID uuid : s.getSpectators()) {
			Player p = Bukkit.getPlayer(uuid);
			s.setupSpectatorInventory(p);
		}
	}

	public static boolean isValid(Session s) {
		for (PlayerSessionData data : s.getParty().values()) {
			Player p = data.getPlayer();
			if (data.hasUnequippedCurses()) {
				s.broadcastError(data.getData().getDisplay() + " must equip all their curses before continuing!");
				return false;
			}
			if (data.exceedsStorageLimit()) {
				s.broadcastError(data.getData().getDisplay() + " must remove some equipment before continuing!");
				return false;
			}
			
			if (data.getPlayer() != null && InventoryListener.hasOpenCoreInventory(p)) {
				s.broadcastError(data.getData().getDisplay() + " must close their inventory before continuing!");
				return false;
			}
		}
		return true;
	}
}
