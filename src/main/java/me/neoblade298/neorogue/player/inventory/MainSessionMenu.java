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

// In-session hub for viewing a player's global achievements, unlocks, and statistics (read-only)
// without leaving the run. Serves both the owner viewing their own menu (spectator == null; closing
// returns to their inventory) and a spectator viewing another player's menu (spectator != null;
// back returns to the spectate inventory).
public class MainSessionMenu extends CoreInventory {
	private static final int STATS = 11, ACHIEVEMENTS = 13, UNLOCKS = 15, BACK = 22;
	private final PlayerSessionData data;
	// Null when the player is viewing their own menu; otherwise the spectator viewing this player's hub.
	private final Player spectator;

	// Owner viewing their own in-session hub.
	public MainSessionMenu(PlayerSessionData data) {
		this(data, null);
	}

	// Spectator (non-null) viewing the given player's hub, or the owner themselves when spectator is null.
	public MainSessionMenu(PlayerSessionData data, Player spectator) {
		super(spectator != null ? spectator : data.getPlayer(),
				Bukkit.createInventory(spectator != null ? spectator : data.getPlayer(), 27, title(data)));
		this.data = data;
		this.spectator = spectator;
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
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
		// The viewer (p) is the owner for a self view and the spectator otherwise; passing the target's
		// own PlayerData keeps these sub-menus read-only. Back reopens this hub for the same viewer.
		Runnable back = () -> new MainSessionMenu(data, spectator);
		switch (e.getSlot()) {
		case STATS:
			if (targetData != null) new StatsMenuInventory(p, targetData, back);
			break;
		case ACHIEVEMENTS:
			if (targetData != null) new AchievementsMenuInventory(p, targetData, back);
			break;
		case UNLOCKS:
			if (targetData != null) new UnlocksMenuInventory(p, targetData, back);
			break;
		case BACK:
			if (spectator != null) {
				new BukkitRunnable() {
					public void run() {
						new PlayerSessionSpectateInventory(data, spectator);
					}
				}.runTask(NeoRogue.inst());
			} else {
				p.closeInventory();
			}
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
