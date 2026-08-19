package me.neoblade298.neorogue.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.inventory.CaravanMenuInventory;
import me.neoblade298.neorogue.player.inventory.CaravanUpgradesInventory;
import me.neoblade298.neorogue.player.inventory.CargoInventory;
import me.neoblade298.neorogue.session.SessionManager;

public final class NeoRogueAPI {
	private NeoRogueAPI() {
	}

	public static void openCaravanUpgradeMenu(Player player) {
		runSync(player, () -> {
			PlayerData data = getAvailableData(player, "manage your caravan");
			if (data != null) new CaravanUpgradesInventory(player, data);
		});
	}

	public static void openCaravanMenu(Player player) {
		runSync(player, () -> {
			PlayerData data = getAvailableData(player, "manage your caravan");
			if (data != null) new CaravanMenuInventory(player);
		});
	}

	public static void openCargoMenu(Player player) {
		runSync(player, () -> {
			PlayerData data = getAvailableData(player, "manage cargo");
			if (data == null) return;
			if (!data.hasFlag(PlayerData.FLAG_CARGO_ACCESS)) {
				Util.displayError(player, "You haven't unlocked cargo access yet! Buy it from the Caravan Upgrades menu.");
				return;
			}
			new CargoInventory(player, data, true);
		});
	}

	private static PlayerData getAvailableData(Player player, String action) {
		if (!SessionManager.requireGeneralPermission(player)) return null;
		if (SessionManager.getSession(player) != null) {
			Util.displayError(player, "You can't " + action + " during a run!");
			return null;
		}
		PlayerData data = PlayerManager.getPlayerData(player.getUniqueId());
		if (data == null) Util.displayError(player, "Your player data isn't loaded yet!");
		return data;
	}

	private static void runSync(Player player, Runnable action) {
		if (player == null || !player.isOnline()) return;
		if (Bukkit.isPrimaryThread()) {
			action.run();
		} else {
			Bukkit.getScheduler().runTask(NeoRogue.inst(), () -> {
				if (player.isOnline()) action.run();
			});
		}
	}
}