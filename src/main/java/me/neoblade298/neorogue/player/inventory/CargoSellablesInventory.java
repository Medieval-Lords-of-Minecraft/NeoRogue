package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.caravan.SellablePackage;
import me.neoblade298.neorogue.player.caravan.SellablePackageRegistry;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

// Read-only cargo permit browser. Packages occupy the first row, the selected package's materials
// occupy rows 2-5, and all pagination controls stay on the final row.
public class CargoSellablesInventory extends CoreInventory {
	private static final int PACKAGE_PAGE_SIZE = 9;
	private static final int MATERIAL_PAGE_SIZE = 36;
	private static final int MATERIAL_START = 9;
	private static final int PREV_PACKAGES = 45, PREV_MATERIALS = 48, INFO = 49,
			NEXT_MATERIALS = 50, NEXT_PACKAGES = 53;

	private final PlayerData pd;
	private final boolean returnToCaravanMenu;
	private final int returnHold;
	private final List<SellablePackage> packages = new ArrayList<SellablePackage>();
	private final Map<Integer, Integer> slotToPackage = new HashMap<Integer, Integer>();
	private int selectedPackage;
	private int packagePage;
	private int materialPage;

	public CargoSellablesInventory(Player p, PlayerData pd, boolean returnToCaravanMenu, int returnHold) {
		super(p, Bukkit.createInventory(p, 54, Component.text("Cargo Sellables", NamedTextColor.GOLD)));
		this.pd = pd;
		this.returnToCaravanMenu = returnToCaravanMenu;
		this.returnHold = returnHold;
		for (SellablePackage pkg : SellablePackageRegistry.getPackages()) {
			if (SellablePackageRegistry.canAccess(pd.getSellablePackages(), p, pkg)) packages.add(pkg);
		}
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		render();
	}

	private void render() {
		inv.clear();
		slotToPackage.clear();
		for (int slot = 0; slot < inv.getSize(); slot++) inv.setItem(slot, buildFillerPane());

		int packageStart = packagePage * PACKAGE_PAGE_SIZE;
		for (int slot = 0; slot < PACKAGE_PAGE_SIZE && packageStart + slot < packages.size(); slot++) {
			int index = packageStart + slot;
			inv.setItem(slot, buildPackageButton(packages.get(index), index == selectedPackage));
			slotToPackage.put(slot, index);
		}

		if (!packages.isEmpty()) {
			SellablePackage selected = packages.get(selectedPackage);
			List<Material> materials = sortedMaterials(selected);
			int materialStart = materialPage * MATERIAL_PAGE_SIZE;
			for (int offset = 0; offset < MATERIAL_PAGE_SIZE && materialStart + offset < materials.size(); offset++) {
				inv.setItem(MATERIAL_START + offset, buildMaterialItem(materials.get(materialStart + offset), selected));
			}

			if (materialPage > 0) inv.setItem(PREV_MATERIALS, buildPageButton(false, "Items"));
			if (materialStart + MATERIAL_PAGE_SIZE < materials.size())
				inv.setItem(NEXT_MATERIALS, buildPageButton(true, "Items"));
			inv.setItem(INFO, buildInfoItem(selected, materials.size()));
		} else {
			inv.setItem(INFO, CoreInventory.createButton(Material.BARRIER,
					Component.text("No Sellable Packages", NamedTextColor.RED)));
		}

		if (packagePage > 0) inv.setItem(PREV_PACKAGES, buildPageButton(false, "Packages"));
		if (packageStart + PACKAGE_PAGE_SIZE < packages.size())
			inv.setItem(NEXT_PACKAGES, buildPageButton(true, "Packages"));
	}

	private ItemStack buildPackageButton(SellablePackage pkg, boolean selected) {
		ItemStack item = new ItemStack(selected ? Material.CHEST_MINECART : Material.CHEST);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text(pkg.getDisplay(),
				selected ? NamedTextColor.YELLOW : NamedTextColor.WHITE)));
		List<Material> materials = sortedMaterials(pkg);
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Includes:", NamedTextColor.GRAY)));
		for (int i = 0; i < Math.min(5, materials.size()); i++) {
			lore.add(line(Component.text("  " + prettyName(materials.get(i)), NamedTextColor.WHITE)));
		}
		if (materials.size() > 5) {
			lore.add(line(Component.text("  ... and " + (materials.size() - 5) + " more", NamedTextColor.GRAY)));
		}
		lore.add(Component.empty());
		if (selected) lore.add(line(Component.text("Currently viewing", NamedTextColor.GREEN)));
		else lore.add(line(Component.text("Left click: ", NamedTextColor.YELLOW)
				.append(Component.text("view all", NamedTextColor.WHITE))));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildMaterialItem(Material material, SellablePackage pkg) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text(prettyName(material), NamedTextColor.WHITE)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Accepted by " + pkg.getDisplay(), NamedTextColor.GRAY)));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildInfoItem(SellablePackage pkg, int materialCount) {
		ItemStack item = new ItemStack(Material.BOOK);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text(pkg.getDisplay(), NamedTextColor.GOLD)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Items: ", NamedTextColor.GRAY)
				.append(Component.text(materialCount, NamedTextColor.WHITE))));
		lore.add(line(Component.text("Page: ", NamedTextColor.GRAY)
				.append(Component.text((materialPage + 1) + " / "
						+ Math.max(1, (int) Math.ceil(materialCount / (double) MATERIAL_PAGE_SIZE)), NamedTextColor.WHITE))));
		lore.add(Component.empty());
		lore.add(line(Component.text("Close to return to cargo.", NamedTextColor.GRAY)));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildPageButton(boolean next, String type) {
		ItemStack item = new ItemStack(Material.SPECTRAL_ARROW);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text(next ? "Next " + type + " \u2192" : "\u2190 Previous " + type,
				NamedTextColor.AQUA)));
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildFillerPane() {
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = pane.getItemMeta();
		meta.displayName(Component.empty());
		pane.setItemMeta(meta);
		return pane;
	}

	private static List<Material> sortedMaterials(SellablePackage pkg) {
		List<Material> materials = new ArrayList<Material>(pkg.getMaterials());
		materials.sort(Comparator.comparing(Material::name));
		return materials;
	}

	private static String prettyName(Material material) {
		String[] words = material.name().toLowerCase().split("_");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (result.length() > 0) result.append(' ');
			result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return result.toString();
	}

	private static Component line(Component component) {
		return component.decoration(TextDecoration.ITALIC, State.FALSE);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() != inv) return;
		int slot = e.getSlot();
		Integer packageIndex = slotToPackage.get(slot);
		if (packageIndex != null) {
			selectedPackage = packageIndex;
			materialPage = 0;
			render();
			click();
			return;
		}
		if (slot == PREV_MATERIALS && materialPage > 0) {
			materialPage--;
			render();
			click();
			return;
		}
		if (slot == NEXT_MATERIALS && !packages.isEmpty()) {
			int size = packages.get(selectedPackage).getMaterials().size();
			if ((materialPage + 1) * MATERIAL_PAGE_SIZE < size) {
				materialPage++;
				render();
				click();
			}
			return;
		}
		if (slot == PREV_PACKAGES && packagePage > 0) {
			packagePage--;
			selectFirstPackageOnPage();
			return;
		}
		if (slot == NEXT_PACKAGES && (packagePage + 1) * PACKAGE_PAGE_SIZE < packages.size()) {
			packagePage++;
			selectFirstPackageOnPage();
		}
	}

	private void selectFirstPackageOnPage() {
		selectedPackage = packagePage * PACKAGE_PAGE_SIZE;
		materialPage = 0;
		render();
		click();
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (p.isOnline() && p.hasPermission(SessionManager.GENERAL_PERMISSION)
						&& SessionManager.getSession(p) == null) {
					new CargoInventory(p, pd, returnToCaravanMenu, returnHold);
				}
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private void click() {
		p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
	}
}