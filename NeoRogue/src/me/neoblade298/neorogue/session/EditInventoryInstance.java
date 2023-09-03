package me.neoblade298.neorogue.session;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class EditInventoryInstance extends Instance {
	public static boolean isValid(Session s) {
		for (PlayerSessionData data : s.getParty().values()) {
			if (!data.saveStorage()) {
				for (Player online : s.getOnlinePlayers()) {
					Util.displayError(online, "&&4" + data.getData().getDisplay() + "&c has too many items in their inventory! They must drop some "
							+ "to satisfy their storage limit of &e" + data.getMaxStorage() + "&c!");
				}
				return false;
			}
		}
		return true;
	}
}
