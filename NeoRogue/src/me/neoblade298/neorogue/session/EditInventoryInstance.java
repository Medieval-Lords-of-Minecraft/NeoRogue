package me.neoblade298.neorogue.session;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class EditInventoryInstance extends Instance {
	public EditInventoryInstance(Session s, double spawnX, double spawnZ) {
		super(s, spawnX, spawnZ);
	}

	public static boolean isValid(Session s) {
		ArrayList<Player> online = s.getOnlinePlayers();
		
		for (Player on : online) {
			on.closeInventory(); // Force inventories to close which puts items from cursor into inventory
		}
		
		for (PlayerSessionData data : s.getParty().values()) {
			if (!data.saveStorage()) {
				for (Player on : online) {
					if (on == data.getPlayer()) continue;
					Util.displayError(on, data.getData().getDisplay() + " has too many items in their inventory! They must drop or equip some "
							+ "to satisfy their storage limit!");
				}
				return false;
			}
		}
		return true;
	}
}
