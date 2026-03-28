package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.ShopInstance;
import me.neoblade298.neorogue.session.ShopInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class StorageInventory extends CoreInventory implements ShiftClickableInventory {
	private Player spectator;
	private PlayerSessionData data;
	private boolean isShop;
	private int sellSlot = -1;
	private int trashSlot;

	public StorageInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), getStorageInventorySize(data, data.getSession().getInstance() instanceof ShopInstance), Component.text("Storage", NamedTextColor.GOLD)));
		new PlayerSessionInventory(data);
		this.data = data;
		isShop = data.getSession().getInstance() instanceof ShopInstance;
		setupInventory();
	}
	
	public StorageInventory(PlayerSessionData data, Player spectator) {
		super(spectator, Bukkit.createInventory(spectator, getStorageInventorySize(data, false), Component.text(data.getData().getDisplay() + "'s Storage", NamedTextColor.GOLD)));
		this.data = data;
		this.spectator = spectator;
		setupInventory();
	}

	private static int getStorageInventorySize(PlayerSessionData data, boolean includeSellButton) {
		int controls = 1; // Exactly one control button: Trash (normal) or Sell (shop)
		int slotsNeeded = data.getMaxStorage() + controls;
		int rows = (int) Math.ceil(slotsNeeded / 9.0D);
		if (rows < 1) rows = 1;
		return rows * 9;
	}
	
	private void setupInventory() {
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		Equipment[] storage = data.getStorage();
		int maxStorage = data.getMaxStorage();
		int controlSlot = contents.length - 1;
		trashSlot = isShop ? -1 : controlSlot;
		sellSlot = isShop ? controlSlot : -1;

		for (int i = 0; i < maxStorage; i++) {
			if (storage[i] == null) continue;
			contents[i] = storage[i].getItem();
		}

		if (!isShop) {
			contents[trashSlot] = CoreInventory.createButton(
					Material.HOPPER, Component.text("Trash", NamedTextColor.RED),
					(TextComponent) NeoCore.miniMessage().deserialize("Drag equipment here to trash it."),
					250, NamedTextColor.GRAY
			);
		}
		
		if (isShop) {
			contents[sellSlot] = CoreInventory.createButton(
					Material.GOLD_NUGGET, Component.text("Sell Items", NamedTextColor.RED),
					(TextComponent) NeoCore.miniMessage().deserialize(
							"Drag equipment here to sell them " + "for <yellow>" + ShopInventory.SELL_PRICE + " coins</yellow>."
					), 250, NamedTextColor.GRAY
			);
		}

		for (int i = maxStorage; i < contents.length; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(
					Material.GRAY_STAINED_GLASS_PANE,
					Component.text("Max Storage: " + data.getMaxStorage(), NamedTextColor.GRAY)
			);
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		if (spectator != null) {
			e.setCancelled(true);
			return;
		}
		PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
		
		// First check for sells
		if (e.getSlot() == sellSlot) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null) return;
			ShopInventory.trySellItem(p, data, e.getCursor(), getLiveStorageSnapshot());
			return;
		}

		if (trashSlot >= 0 && e.getSlot() == trashSlot && !e.getCursor().getType().isAir()) {
			e.setCancelled(true);
			tryTrashCursorItem(pinv, e.getCursor());
			return;
		}

		// Prevent picking up trash button
		if (trashSlot >= 0 && e.getSlot() == trashSlot) {
			e.setCancelled(true);
			return;
		}

		// Ignore gray panes
		if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
			e.setCancelled(true);
			return;
		}

		ItemStack clicked = e.getCurrentItem();
		ItemStack cursor = e.getCursor();
		NBTItem nclicked = e.getCurrentItem() != null ? new NBTItem(e.getCurrentItem()) : null;
		NBTItem ncursor = !e.getCursor().isEmpty() ? new NBTItem(e.getCursor()) : null;
		
		// If right click with empty hand, open glossary, disgusting code due to if statement handling
		if (e.isRightClick() && nclicked != null && nclicked.hasTag("equipId") && e.getCursor().getType().isAir()) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new EquipmentGlossaryInventory(p, Equipment.get(nclicked.getString("equipId"), false), null);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		// Handle shift clicks
		else if (e.isShiftClick()) {
			if (nclicked == null || !nclicked.hasTag("equipId")) return;
			e.setCancelled(true);
			if (!pinv.canShiftClickIn(inv.getItem(e.getSlot()))) return;
			pinv.handleShiftClickIn(e, inv.getItem(e.getSlot()));
			e.setCurrentItem(null);
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
		// Handle left click
		else {
			// First check for curses in cursor
			if (ncursor != null && ncursor.hasTag("equipId")) {
				Equipment eq = Equipment.get(ncursor.getString("equipId"), ncursor.getBoolean("isUpgraded"));
				if (eq.isCursed()) {
					displayError("You can't unequip cursed items!", false);
					e.setCancelled(true);
					return;
				}
			}

			// Picking up an item
			if (nclicked != null && nclicked.hasTag("equipId") && ncursor == null) {
				e.setCancelled(true);
				Equipment eq = Equipment.get(nclicked.getString("equipId"), false);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				p.setItemOnCursor(e.getCurrentItem());
				inv.setItem(e.getSlot(), null);
				if (e.getSlot() == sellSlot) {
					pinv.clearHighlights();
				}
				else {
					pinv.setHighlights(eq.getType());
				}
			}
			// Swapping an item
			else if (ncursor != null && nclicked != null) {
				e.setCancelled(true);
				if (!nclicked.hasTag("equipId") || !ncursor.hasTag("equipId")) {
					return;
				}
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				String eqId = ncursor.getString("equipId");
				String eqedId = nclicked.getString("equipId");
				Equipment eq = Equipment.get(eqId, ncursor.getBoolean("isUpgraded"));
				Equipment eqed = Equipment.get(eqedId, nclicked.getBoolean("isUpgraded"));
				// Reforged item check
				if (eq.containsReforgeOption(eqedId)) {
					if (!Equipment.canReforge(eq, eqed)) {
						displayError("At least one of the items must be upgraded to reforge!", true);
						return;
					}
					new BukkitRunnable() {
						public void run() {
							p.setItemOnCursor(null);
							inv.setItem(e.getSlot(), null);
							handleInventoryClose();
							new ReforgeOptionsInventory(data, eq, eqed);
						}
					}.runTask(NeoRogue.inst());
					return;
				}
				else if (eqed != null && eqed.containsReforgeOption(eqId)) {
					if (!Equipment.canReforge(eq, eqed)) {
						displayError("At least one of the items must be upgraded to reforge!", true);
						return;
					}
					new BukkitRunnable() {
						public void run() {
							p.setItemOnCursor(null);
							inv.setItem(e.getSlot(), null);
							handleInventoryClose();
							new ReforgeOptionsInventory(data, eqed, eq);
						}
					}.runTask(NeoRogue.inst());
					return;
				}
				// Prevent stacking
				else if (e.getCursor().isSimilar(e.getCurrentItem())) {
					p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				}
				
				inv.setItem(e.getSlot(), cursor);
				p.setItemOnCursor(clicked);
			}
			// dropping an item
			else if (nclicked == null && ncursor != null) {
				e.setCancelled(true);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				pinv.clearHighlights();
				inv.setItem(e.getSlot(), cursor);
				p.setItemOnCursor(null);
			}
		}
	}

	private void displayError(String error, boolean closeInventory) {
		p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
		Util.msg(p, error);
		if (closeInventory) p.closeInventory();
	}

	private Equipment[] getLiveStorageSnapshot() {
		Equipment[] snapshot = new Equipment[PlayerSessionData.MAX_STORAGE_SIZE];
		int iter = 0;
		ItemStack[] contents = inv.getContents();

		for (int i = 0; i < data.getMaxStorage(); i++) {
			ItemStack item = contents[i];
			if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
			NBTItem nbti = new NBTItem(item);
			if (!nbti.hasTag("equipId")) continue;
			Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
			if (eq != null) {
				snapshot[iter++] = eq;
			}
		}

		return snapshot;
	}
	
	public void handleInventoryClose() {
		if (spectator != null) return;
		// Save storage
		Equipment[] newSave = new Equipment[PlayerSessionData.MAX_STORAGE_SIZE];
		int iter = 0;
		ItemStack[] contents = inv.getContents();
		
		for (int i = 0; i < data.getMaxStorage(); i++) {
			ItemStack item = contents[i];
			if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
			NBTItem nbti = new NBTItem(item);
			if (!nbti.hasTag("equipId")) continue;
			Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
			if (eq == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to save item " + nbti.getString("equipId") + " to storage of " + p.getName());
				continue;
			}
			newSave[iter++] = eq;
		}
		data.setStorage(newSave);
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		handleInventoryClose();
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private void tryTrashCursorItem(PlayerSessionInventory pinv, ItemStack cursor) {
		NBTItem nbti = new NBTItem(cursor);
		if (!nbti.hasTag("equipId")) {
			Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " tried to trash non-equipment item: " + cursor);
			return;
		}
		Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
		if (eq == null) {
			Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " tried to trash non-equipment item: " + cursor);
			return;
		}
		if (eq.isCursed()) {
			Util.displayError(p, "You can't trash cursed items!");
			return;
		}

		Equipment[] projectedStorage = getLiveStorageSnapshot();

		if (eq.getType() == Equipment.EquipmentType.WEAPON && data.countOwnedWeapons(projectedStorage) == 0) {
			Util.displayError(p, "You can't trash your last weapon!");
			return;
		}
		if (PlayerSessionData.isUnlimitedAmmunition(eq) && data.countOwnedUnlimitedAmmunition(projectedStorage) == 0) {
			Util.displayError(p, "You can't trash your last unlimited ammunition!");
			return;
		}

		pinv.clearHighlights();
		p.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F);
		p.setItemOnCursor(null);
	}

	@Override
	public boolean canShiftClickIn(ItemStack item) {
		for (int i = 0; i < data.getMaxStorage(); i++) {
			if (inv.getItem(i) == null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleShiftClickIn(InventoryClickEvent ev, ItemStack item) {
		for (int i = 0; i < data.getMaxStorage(); i++) {
			if (inv.getItem(i) == null) {
				inv.setItem(i, item);
				return;
			}
		}
	}
}
