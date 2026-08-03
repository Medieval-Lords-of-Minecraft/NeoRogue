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

// Read-only hub for viewing a player's global achievements, unlocks, and statistics. It can be
// opened from a session, by interacting with another player, or for an offline player via command.
public class MainSessionMenu extends CoreInventory {
	private static final int STATS = 11, ACHIEVEMENTS = 13, UNLOCKS = 15, BACK = 22;
	private final PlayerData targetData;
	private final Runnable back;

	// Owner viewing their own in-session hub.
	public MainSessionMenu(PlayerSessionData data) {
		this(data.getPlayer(), PlayerManager.getPlayerData(data.getUniqueId()), null);
	}

	// Spectator (non-null) viewing the given player's hub, or the owner themselves when spectator is null.
	public MainSessionMenu(PlayerSessionData data, Player spectator) {
		this(spectator != null ? spectator : data.getPlayer(), PlayerManager.getPlayerData(data.getUniqueId()),
				spectator == null ? null : () -> new PlayerSessionSpectateInventory(data, spectator));
	}

	public MainSessionMenu(Player viewer, PlayerData targetData) {
		this(viewer, targetData, null);
	}

	private MainSessionMenu(Player viewer, PlayerData targetData, Runnable back) {
		super(viewer, Bukkit.createInventory(viewer, 27, title(targetData)));
		this.targetData = targetData;
		this.back = back;
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

	private static Component title(PlayerData targetData) {
		return Component.text(targetData.getDisplay() + "'s Menu", NamedTextColor.DARK_RED);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		Runnable reopen = () -> new MainSessionMenu(p, targetData, back);
		switch (e.getSlot()) {
		case STATS:
			new StatsMenuInventory(p, targetData, reopen);
			break;
		case ACHIEVEMENTS:
			new AchievementsMenuInventory(p, targetData, reopen);
			break;
		case UNLOCKS:
			new UnlocksMenuInventory(p, targetData, reopen);
			break;
		case BACK:
			if (back != null) {
				new BukkitRunnable() {
					public void run() {
						back.run();
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
