package me.neoblade298.neorogue.session;

import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class EditInventoryInstance extends Instance {
	public EditInventoryInstance(Session s, double spawnX, double spawnZ) {
		super(s, spawnX, spawnZ);
	}

	public static boolean isValid(Session s) {
		for (PlayerSessionData data : s.getParty().values()) {
			if (!data.saveStorage()) {
				s.broadcastError(data.getData().getDisplay() + " has too many items in their inventory! They must drop or equip some "
							+ "to satisfy their storage limit!");
				return false;
			}
			
			if (data.getPlayer() != null && InventoryListener.getCoreInventory(data.getPlayer()) != null) {
				s.broadcastError(data.getData().getDisplay() + " is editing their inventory! Close the inventory to move on.");
				return false;
			}
		}
		return true;
	}
}
