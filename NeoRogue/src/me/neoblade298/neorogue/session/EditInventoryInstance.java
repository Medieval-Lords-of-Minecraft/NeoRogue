package me.neoblade298.neorogue.session;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class EditInventoryInstance extends Instance {
	public static boolean isValid(Session s) {
		for (PlayerSessionData data : s.getParty().values()) {
			if (!data.saveStorage()) {
				for (Player online : s.getOnlinePlayers()) {
					Util.displayError(online, data.getData().getDisplay() + " has too many items in their inventory! They must drop some "
							+ "to satisfy their storage limit of " + data.getMaxStorage() + "!");
				}
				return false;
			}
		}
		return true;
	}
}
