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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.RunStats;
import me.neoblade298.neorogue.player.RunStats.PartyMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

// Displays a player's run statistics: winrate (lifetime and this calendar month) and winstreaks
// (current and best), broken down globally and per class, and contextualized by notoriety level.
// All numbers are derived on the fly from PlayerData.getRunStats() (see RunStats).
public class StatsMenuInventory extends CoreInventory {
	private static final DecimalFormat pct = new DecimalFormat("#0.#");
	private static final int GLOBAL_SLOT = 4;
	private static final int MODE_SLOT = 0;
	private static final int BACK_SLOT = 22;
	private static final int[] CLASS_SLOTS = { 10, 12, 14, 16 };
	private static final EquipmentClass[] CLASSES = { EquipmentClass.WARRIOR, EquipmentClass.THIEF,
			EquipmentClass.ARCHER, EquipmentClass.MAGE };

	// Reopens the inventory the back button should return to. Null falls back to the main menu.
	private Runnable prevInventory;
	// Whose stats are shown; kept so the mode toggle can rebuild in place.
	private final PlayerData target;
	// Current party-size filter applied to every tile.
	private PartyMode mode = PartyMode.COMBINED;

	public StatsMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Statistics", NamedTextColor.DARK_AQUA)));
		this.target = PlayerManager.getPlayerData(p.getUniqueId());
		setupInventory();
	}

	// Spectator view of another player's global statistics.
	public StatsMenuInventory(Player spectator, PlayerData target) {
		this(spectator, target, null);
	}

	// Spectator view of another player's global statistics, with a custom back target.
	public StatsMenuInventory(Player spectator, PlayerData target, Runnable prevInventory) {
		super(spectator, Bukkit.createInventory(spectator, 27,
				Component.text(target.getDisplay() + "'s Statistics", NamedTextColor.DARK_AQUA)));
		this.prevInventory = prevInventory;
		this.target = target;
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		RunStats stats = target != null ? target.getRunStats() : new RunStats(new ArrayList<RunStats.RunRecord>());

		inv.setItem(MODE_SLOT, buildModeItem());
		inv.setItem(GLOBAL_SLOT, buildScopeItem(stats, null, Material.NETHER_STAR, "Global",
				NamedTextColor.GOLD));
		Material[] icons = { Material.IRON_SWORD, Material.IRON_INGOT, Material.BOW, Material.BLAZE_ROD };
		for (int i = 0; i < CLASSES.length; i++) {
			inv.setItem(CLASS_SLOTS[i],
					buildScopeItem(stats, CLASSES[i], icons[i], CLASSES[i].getDisplay(), NamedTextColor.YELLOW));
		}
		inv.setItem(BACK_SLOT, CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED)));
	}

	// The mode toggle button. Click cycles Combined -> Solo -> Multiplayer, highlighting the active one.
	private ItemStack buildModeItem() {
		ItemStack item = CoreInventory.createButton(Material.COMPARATOR,
				Component.text("View: " + modeLabel(mode), NamedTextColor.LIGHT_PURPLE));
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = new ArrayList<Component>();
		for (PartyMode m : PartyMode.values()) {
			boolean active = m == mode;
			lore.add(line(Component.text((active ? "\u25B6 " : "  ") + modeLabel(m),
					active ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY)));
		}
		lore.add(Component.empty());
		lore.add(line(Component.text("Click to cycle", NamedTextColor.YELLOW)));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private static String modeLabel(PartyMode mode) {
		switch (mode) {
		case SOLO:
			return "Solo";
		case MULTIPLAYER:
			return "Multiplayer";
		default:
			return "Combined";
		}
	}

	// Builds a scope tile (global or a single class). ec == null means global (all classes).
	private ItemStack buildScopeItem(RunStats stats, EquipmentClass ec, Material icon, String title,
			NamedTextColor titleColor) {
		ItemStack item = CoreInventory.createButton(icon, Component.text(title + " Statistics", titleColor));
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		List<Component> lore = new ArrayList<Component>();

		RunStats.Winrate lifetime = stats.winrate(ec, null, false, mode);
		if (!lifetime.hasRuns()) {
			lore.add(line(Component.text("No " + modeLabel(mode).toLowerCase() + " runs recorded yet.",
					NamedTextColor.GRAY)));
			meta.lore(lore);
			item.setItemMeta(meta);
			return item;
		}

		RunStats.Winrate month = stats.winrate(ec, null, true, mode);
		lore.add(line(Component.text("Lifetime winrate: ", NamedTextColor.GRAY).append(winrate(lifetime))));
		lore.add(line(Component.text("This month: ", NamedTextColor.GRAY).append(winrate(month))));
		lore.add(line(Component.text("Best winstreak: ", NamedTextColor.GRAY)
				.append(Component.text(stats.bestStreakAnyNotoriety(ec, mode), NamedTextColor.AQUA))
				.append(Component.text(" (any notoriety)", NamedTextColor.DARK_GRAY))));
		lore.add(Component.empty());
		lore.add(line(Component.text("By notoriety ", NamedTextColor.GRAY)
				.append(Component.text("(lifetime | month | streak cur/best)", NamedTextColor.DARK_GRAY))));

		for (int notoriety : stats.playedNotorieties(ec, mode)) {
			RunStats.Winrate life = stats.winrate(ec, notoriety, false, mode);
			RunStats.Winrate mon = stats.winrate(ec, notoriety, true, mode);
			RunStats.Streak streak = stats.streak(ec, notoriety, mode);
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
			if (prevInventory != null) prevInventory.run();
			else new MainMenuInventory(p);
		}
		else if (e.getSlot() == MODE_SLOT) {
			PartyMode[] modes = PartyMode.values();
			mode = modes[(mode.ordinal() + 1) % modes.length];
			setupInventory();
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
