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
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.Cargo;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.RunStats;
import me.neoblade298.neorogue.player.unlock.UnlockNode;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.tutorial.book.TutorialBookRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MainMenuInventory extends CoreInventory {
	private static final int HOST_GAME = 11, JOIN_GAME = 12, ACHIEVEMENTS = 13, UNLOCKS = 14, CARGO = 15, STATS = 4, TUTORIAL = 18;
	private static final DecimalFormat pct = new DecimalFormat("#0.#");

	public MainMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("NeoRogue", NamedTextColor.DARK_RED)));
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		ItemStack[] contents = inv.getContents();
		contents[HOST_GAME] = CoreInventory.createButton(Material.WRITABLE_BOOK,
				Component.text("Host Game", NamedTextColor.GREEN));
		contents[JOIN_GAME] = CoreInventory.createButton(Material.SPYGLASS,
				Component.text("Join Game", NamedTextColor.YELLOW));
		contents[ACHIEVEMENTS] = CoreInventory.createButton(Material.DIAMOND,
				Component.text("Achievements", NamedTextColor.AQUA));
		contents[UNLOCKS] = CoreInventory.createButton(Material.ENDER_EYE,
				Component.text("Unlocks", NamedTextColor.LIGHT_PURPLE));
		int totalAvailable = countAvailableUnlocks(pd);
		if (totalAvailable > 0) contents[UNLOCKS].setAmount(Math.min(totalAvailable, 64));
		contents[CARGO] = createCargoButton(pd);
		contents[STATS] = createStatsButton(pd);
		contents[TUTORIAL] = CoreInventory.createButton(Material.WRITABLE_BOOK,
				Component.text("Tutorial", NamedTextColor.GOLD));
		inv.setContents(contents);
	}

	private ItemStack createCargoButton(PlayerData pd) {
		// Cargo stays locked until the caravan "cargo_access" upgrade is purchased.
		if (!pd.hasFlag(PlayerData.FLAG_CARGO_ACCESS)) {
			ItemStack locked = CoreInventory.createButton(Material.BARRIER,
					Component.text("Cargo (Locked)", NamedTextColor.DARK_GRAY));
			ItemMeta meta = locked.getItemMeta();
			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("Unlock cargo access from", NamedTextColor.GRAY)
					.decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("the Caravan Upgrades menu.", NamedTextColor.GRAY)
					.decoration(TextDecoration.ITALIC, false));
			meta.lore(lore);
			locked.setItemMeta(meta);
			return locked;
		}
		Cargo cargo = pd.getCargo();
		ItemStack item = CoreInventory.createButton(Material.CHEST_MINECART,
				Component.text("Cargo", NamedTextColor.GOLD));
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Items: " + cargo.getTotalItems() + " / " + cargo.getCapacity(), NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack createStatsButton(PlayerData pd) {
		ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
		item.setAmount(Math.min(Math.max(pd.getLevel(), 1), 64));
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Stats", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Global Level: " + pd.getLevel(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text("  Exp: " + pd.getExp() + "/" + PlayerData.getXpRequired(pd.getLevel()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		RunStats.Winrate wr = pd.getRunStats().winrate(null, null, false, RunStats.PartyMode.COMBINED);
		if (wr.hasRuns()) {
			lore.add(Component.text("Winrate: " + pct.format(wr.rate() * 100) + "% (" + wr.wins + "/" + wr.total + ")", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
		}
		lore.add(Component.empty());
		for (EquipmentClass ec : new EquipmentClass[] { EquipmentClass.WARRIOR, EquipmentClass.THIEF, EquipmentClass.ARCHER, EquipmentClass.MAGE }) {
			int level = pd.getLevel(ec);
			int exp = pd.getExp(ec);
			int required = PlayerData.getXpRequired(level);
			int notoriety = pd.getMaxNotoriety(ec);
			lore.add(Component.text(ec.getDisplay() + " Level: " + level, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("  Exp: " + exp + "/" + required, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("  Max Notoriety: " + notoriety, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		}
		lore.add(Component.empty());
		lore.add(Component.text("Click to view detailed statistics", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private int countAvailableUnlocks(PlayerData data) {
		int total = 0;
		for (EquipmentClass ec : new EquipmentClass[] { EquipmentClass.WARRIOR, EquipmentClass.THIEF, EquipmentClass.ARCHER, EquipmentClass.MAGE }) {
			total += countAvailableForClass(data, ec);
		}
		total += countAvailableForClass(data, null);
		return total;
	}

	private int countAvailableForClass(PlayerData data, EquipmentClass ec) {
		int available = 0;
		int points = data.getPoints(ec);
		for (UnlockNode node : UnlockRegistry.getNodesForClass(ec)) {
			if (data.hasUnlockNode(node.getId())) continue;
			if (node.getCost() <= points && node.checkRequirementsMet(data)) {
				available++;
			}
		}
		return available;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		if (pd == null) return;

		switch (e.getSlot()) {
		case HOST_GAME:
			if (!pd.hasFlag(PlayerData.FLAG_PLAYED_BEFORE)) {
				pd.addFlag(PlayerData.FLAG_PLAYED_BEFORE);
				p.closeInventory();
				SessionManager.createTutorialSession(p, 1);
			} else {
				if (SessionManager.getSession(p) != null) {
					Util.displayError(p, "You're already in a session!");
					return;
				}
				new HostGameInventory(p, pd);
			}
			break;
		case JOIN_GAME:
			new JoinGameInventory(p);
			break;
		case ACHIEVEMENTS:
			new AchievementsMenuInventory(p);
			break;
		case UNLOCKS:
			new UnlocksMenuInventory(p);
			break;
		case CARGO:
			if (!pd.hasFlag(PlayerData.FLAG_CARGO_ACCESS)) {
				Util.displayError(p, "You haven't unlocked cargo access yet! Buy it from the Caravan Upgrades menu.");
				return;
			}
			new CargoInventory(p, pd);
			break;
		case STATS:
			new StatsMenuInventory(p);
			break;
		case TUTORIAL:
			p.closeInventory();
			TutorialBookRegistry.openTableOfContents(p, "neorogue_tutorial");
			break;
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
