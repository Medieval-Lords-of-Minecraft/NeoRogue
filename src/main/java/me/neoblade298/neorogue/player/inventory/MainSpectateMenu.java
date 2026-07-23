package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Hub menu reached by clicking a spectated player's stats icon. Lets the spectator view that
// player's global achievements, unlocks, and statistics.
public class MainSpectateMenu extends CoreInventory {
	private static final int STATS = 11, ACHIEVEMENTS = 13, UNLOCKS = 15, BACK = 22;
	private PlayerSessionData data;
	private Player spectator;

	public MainSpectateMenu(PlayerSessionData data, Player spectator) {
		super(spectator, Bukkit.createInventory(spectator, 27, title(data)));
		this.data = data;
		this.spectator = spectator;
		spectator.playSound(spectator, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		inv.setItem(STATS, CoreInventory.createButton(Material.EXPERIENCE_BOTTLE,
				Component.text("Stats", NamedTextColor.GREEN)));
		inv.setItem(ACHIEVEMENTS, CoreInventory.createButton(Material.DIAMOND,
				Component.text("Achievements", NamedTextColor.AQUA)));
		inv.setItem(UNLOCKS, CoreInventory.createButton(Material.ENDER_EYE,
				Component.text("Unlocks", NamedTextColor.LIGHT_PURPLE)));
		inv.setItem(BACK, CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED)));
	}

	private static Component title(PlayerSessionData data) {
		PlayerData pd = PlayerManager.getPlayerData(data.getUniqueId());
		String name = pd != null ? pd.getDisplay() : "Player";
		return Component.text(name + "'s Menu", NamedTextColor.DARK_RED);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		PlayerData targetData = PlayerManager.getPlayerData(data.getUniqueId());
		Runnable back = () -> new MainSpectateMenu(data, spectator);
		switch (e.getSlot()) {
		case STATS:
			if (targetData != null) new StatsMenuInventory(spectator, targetData, back);
			break;
		case ACHIEVEMENTS:
			if (targetData != null) new AchievementsMenuInventory(spectator, targetData, back);
			break;
		case UNLOCKS:
			if (targetData != null) new UnlocksMenuInventory(spectator, targetData, back);
			break;
		case BACK:
			new BukkitRunnable() {
				public void run() {
					new PlayerSessionSpectateInventory(data, spectator);
				}
			}.runTask(NeoRogue.inst());
			break;
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
