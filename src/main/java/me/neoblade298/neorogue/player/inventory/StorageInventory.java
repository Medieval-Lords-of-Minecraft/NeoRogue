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

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.instances.ShopInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import me.neoblade298.neorogue.session.shop.ShopInventory;
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
		int itemCount = data.countSavedStorageItems();
		int slotsNeeded = Math.max(data.getMaxStorage(), itemCount) + controls;
		int rows = (int) Math.ceil(slotsNeeded / 9.0D);
		if (rows < 1) rows = 1;
		return rows * 9;
	}
	
	private void setupInventory() {
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		SessionEquipment[] storage = data.getStorage();
		int maxStorage = data.getMaxStorage();
		int controlSlot = contents.length - 1;
		trashSlot = isShop ? -1 : controlSlot;
		sellSlot = isShop ? controlSlot : -1;

		int itemSlot = 0;
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null) continue;
			contents[itemSlot++] = storage[i].getItem();
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

		boolean overStorageLimit = data.countSavedStorageItems() > maxStorage;
		for (int i = maxStorage; i < contents.length; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(
					overStorageLimit ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE,
					Component.text("Max Storage: " + data.getMaxStorage(), overStorageLimit ? NamedTextColor.RED : NamedTextColor.GRAY)
			);
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		ItemStack clicked = e.getCurrentItem();
		ItemStack cursor = e.getCursor();
		String clickedEquipId = clicked != null ? NBT.get(clicked, nbt -> nbt.hasTag("equipId") ? nbt.getString("equipId") : null) : null;
		String cursorEquipId = !cursor.isEmpty() ? NBT.get(cursor, nbt -> nbt.hasTag("equipId") ? nbt.getString("equipId") : null) : null;
		boolean cursorUpgraded = !cursor.isEmpty() && Boolean.TRUE.equals(NBT.get(cursor, nbt -> { return nbt.getBoolean("isUpgraded"); }));
		boolean clickedUpgraded = clicked != null && Boolean.TRUE.equals(NBT.get(clicked, nbt -> { return nbt.getBoolean("isUpgraded"); }));
		
		// If right click with empty hand, open glossary, disgusting code due to if statement handling
		if (e.isRightClick() && clickedEquipId != null && e.getCursor().getType().isAir()) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new EquipmentGlossaryInventory(p, Equipment.get(clickedEquipId, false), null);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		if (spectator != null) {
			e.setCancelled(true);
			return;
		}

		// Block hotbar swaps to prevent moving non-equipment items into storage
		if (e.getAction() == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP) {
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
		
		// Handle shift clicks
		else if (e.isShiftClick()) {
			if (clickedEquipId == null) return;
			e.setCancelled(true);
			if (!pinv.canShiftClickIn(inv.getItem(e.getSlot()))) return;
			pinv.handleShiftClickIn(e, inv.getItem(e.getSlot()));
			e.setCurrentItem(null);
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
		// Handle left click
		else {
			// First check for curses in cursor
			if (cursorEquipId != null) {
				Equipment eq = Equipment.get(cursorEquipId, cursorUpgraded);
				if (eq.isCursed()) {
					displayError("You can't unequip cursed items!", false);
					e.setCancelled(true);
					return;
				}
			}

			// Picking up an item
			if (clickedEquipId != null && cursor.isEmpty()) {
				e.setCancelled(true);
				Equipment eq = Equipment.get(clickedEquipId, false);
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
			else if (!cursor.isEmpty() && clicked != null) {
				e.setCancelled(true);
				if (clickedEquipId == null || cursorEquipId == null) {
					return;
				}
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				String eqId = cursorEquipId;
				String eqedId = clickedEquipId;
				Equipment eq = Equipment.get(eqId, cursorUpgraded);
				Equipment eqed = Equipment.get(eqedId, clickedUpgraded);
				// Reforged item check
				Equipment[] reforgePair = Equipment.resolveReforgePair(eq, eqed);
				if (reforgePair != null) {
					if (!Equipment.canReforge(reforgePair[0], reforgePair[1], data.getSession())) {
						String msg = NotorietySetting.REFORGE_REQUIRES_BOTH.isActive(data.getSession())
								? "Both items must be upgraded to reforge!"
								: "At least one of the items must be upgraded to reforge!";
						displayError(msg, true);
						return;
					}
					new BukkitRunnable() {
						public void run() {
							p.setItemOnCursor(null);
							inv.setItem(e.getSlot(), null);
							handleInventoryClose();
							new ReforgeOptionsInventory(data, reforgePair[0], reforgePair[1]);
						}
					}.runTask(NeoRogue.inst());
					return;
				}

				// Wildcard reforge check (e.g. Transmutation): reforge any item into any of its results
				Equipment[] wildcardPair = Equipment.resolveWildcardReforge(eq, eqed);
				if (wildcardPair != null) {
					new BukkitRunnable() {
						public void run() {
							p.setItemOnCursor(null);
							inv.setItem(e.getSlot(), null);
							handleInventoryClose();
							new WildcardReforgeInventory(data, wildcardPair[0], wildcardPair[1]);
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
			else if (clicked == null && !cursor.isEmpty()) {
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
		Util.msgRaw(p, error);
		if (closeInventory) p.closeInventory();
	}

	SessionEquipment[] getLiveStorageSnapshot() {
		SessionEquipment[] snapshot = new SessionEquipment[PlayerSessionData.MAX_STORAGE_SIZE];
		int iter = 0;
		ItemStack[] contents = inv.getContents();
		int controlSlot = contents.length - 1;

		for (int i = 0; i < contents.length; i++) {
			if (i == controlSlot) continue; // Skip control button (trash/sell)
			ItemStack item = contents[i];
			if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
			if (!NBT.get(item, nbt -> { return nbt.hasTag("equipId"); })) continue;
			SessionEquipment se = SessionEquipment.fromItem(item);
			if (se != null) {
				snapshot[iter++] = se;
			}
		}

		return snapshot;
	}
	
	public void handleInventoryClose() {
		if (spectator != null) return;
		// Save storage - include overflow items beyond maxStorage
		SessionEquipment[] newSave = new SessionEquipment[PlayerSessionData.MAX_STORAGE_SIZE];
		int iter = 0;
		ItemStack[] contents = inv.getContents();
		int controlSlot = contents.length - 1;
		
		for (int i = 0; i < contents.length; i++) {
			if (i == controlSlot) continue; // Skip control button (trash/sell)
			ItemStack item = contents[i];
			if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
			String equipId = NBT.get(item, nbt -> { return nbt.hasTag("equipId") ? nbt.getString("equipId") : null; });
			if (equipId == null) continue;
			SessionEquipment se = SessionEquipment.fromItem(item);
			if (se == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to save item " + equipId + " to storage of " + p.getName());
				continue;
			}
			newSave[iter++] = se;
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
		String equipId = NBT.get(cursor, nbt -> { return nbt.hasTag("equipId") ? nbt.getString("equipId") : null; });
		if (equipId == null) {
			Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " tried to trash non-equipment item: " + cursor);
			return;
		}
		boolean isUpgraded = Boolean.TRUE.equals(NBT.get(cursor, nbt -> { return nbt.getBoolean("isUpgraded"); }));
		Equipment eq = Equipment.get(equipId, isUpgraded);
		if (eq == null) {
			Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " tried to trash non-equipment item: " + cursor);
			return;
		}
		if (eq.isCursed()) {
			Util.displayError(p, "You can't trash cursed items!");
			return;
		}

		SessionEquipment[] projectedStorage = getLiveStorageSnapshot();

		String restriction = data.getRemovalRestriction(eq, projectedStorage, false, "trash");
		if (restriction != null) {
			Util.displayError(p, restriction);
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
