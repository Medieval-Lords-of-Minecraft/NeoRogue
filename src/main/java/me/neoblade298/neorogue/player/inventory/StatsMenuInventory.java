package me.neoblade298.neorogue.player.inventory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.RunStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

// Displays a player's run statistics: winrate (lifetime and this calendar month) and winstreaks
// (current and best), broken down globally and per class, and contextualized by notoriety level.
// All numbers are derived on the fly from PlayerData.getRunStats() (see RunStats).
public class StatsMenuInventory extends CoreInventory {
	private static final DecimalFormat pct = new DecimalFormat("#0.#");
	private static final int GLOBAL_SLOT = 4;
	private static final int BACK_SLOT = 22;
	private static final int[] CLASS_SLOTS = { 10, 12, 14, 16 };
	private static final EquipmentClass[] CLASSES = { EquipmentClass.WARRIOR, EquipmentClass.THIEF,
			EquipmentClass.ARCHER, EquipmentClass.MAGE };

	public StatsMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Statistics", NamedTextColor.DARK_AQUA)));
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		RunStats stats = pd != null ? pd.getRunStats() : new RunStats(new ArrayList<RunStats.RunRecord>());

		inv.setItem(GLOBAL_SLOT, buildScopeItem(stats, null, Material.NETHER_STAR, "Global",
				NamedTextColor.GOLD));
		Material[] icons = { Material.IRON_SWORD, Material.LEATHER_BOOTS, Material.BOW, Material.BLAZE_ROD };
		for (int i = 0; i < CLASSES.length; i++) {
			inv.setItem(CLASS_SLOTS[i],
					buildScopeItem(stats, CLASSES[i], icons[i], CLASSES[i].getDisplay(), NamedTextColor.YELLOW));
		}
		inv.setItem(BACK_SLOT, CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED)));
	}

	// Builds a scope tile (global or a single class). ec == null means global (all classes).
	private ItemStack buildScopeItem(RunStats stats, EquipmentClass ec, Material icon, String title,
			NamedTextColor titleColor) {
		ItemStack item = CoreInventory.createButton(icon, Component.text(title + " Statistics", titleColor));
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = new ArrayList<Component>();

		RunStats.Winrate lifetime = stats.winrate(ec, null, false);
		if (!lifetime.hasRuns()) {
			lore.add(line(Component.text("No runs recorded yet.", NamedTextColor.GRAY)));
			meta.lore(lore);
			item.setItemMeta(meta);
			return item;
		}

		RunStats.Winrate month = stats.winrate(ec, null, true);
		lore.add(line(Component.text("Lifetime winrate: ", NamedTextColor.GRAY).append(winrate(lifetime))));
		lore.add(line(Component.text("This month: ", NamedTextColor.GRAY).append(winrate(month))));
		lore.add(line(Component.text("Best winstreak: ", NamedTextColor.GRAY)
				.append(Component.text(stats.bestStreakAnyNotoriety(ec), NamedTextColor.AQUA))
				.append(Component.text(" (any notoriety)", NamedTextColor.DARK_GRAY))));
		lore.add(Component.empty());
		lore.add(line(Component.text("By notoriety ", NamedTextColor.GRAY)
				.append(Component.text("(lifetime | month | streak cur/best)", NamedTextColor.DARK_GRAY))));

		for (int notoriety : stats.playedNotorieties(ec)) {
			RunStats.Winrate life = stats.winrate(ec, notoriety, false);
			RunStats.Winrate mon = stats.winrate(ec, notoriety, true);
			RunStats.Streak streak = stats.streak(ec, notoriety);
			Component monthPart = mon.hasRuns() ? winrate(mon) : Component.text("-", NamedTextColor.DARK_GRAY);
			lore.add(line(Component.text("  N" + notoriety + ": ", NamedTextColor.WHITE)
					.append(winrate(life))
					.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
					.append(monthPart)
					.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
					.append(Component.text(streak.current + "/" + streak.best, NamedTextColor.AQUA))));
		}

		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	// "<rate>% (<wins>/<total>)" with the rate colored green/yellow/red by performance.
	private Component winrate(RunStats.Winrate wr) {
		double rate = wr.rate();
		NamedTextColor color = rate >= 0.6 ? NamedTextColor.GREEN
				: rate >= 0.35 ? NamedTextColor.YELLOW : NamedTextColor.RED;
		return Component.text(pct.format(rate * 100) + "%", color)
				.append(Component.text(" (" + wr.wins + "/" + wr.total + ")", NamedTextColor.GRAY));
	}

	private static Component line(Component c) {
		return c.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getSlot() == BACK_SLOT) {
			new MainMenuInventory(p);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
