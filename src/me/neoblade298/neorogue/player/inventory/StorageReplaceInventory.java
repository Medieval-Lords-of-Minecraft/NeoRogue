package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StorageReplaceInventory extends CoreInventory {
	private PlayerSessionData data;
	private Equipment newEquipment;
	private Component toSelf;
	private Component toOthers;
	private Runnable onComplete;
	private Runnable onCancel;
	private boolean replaced = false;
	
	private static final int NEVERMIND = 8;
	
	public StorageReplaceInventory(PlayerSessionData data, Equipment newEquipment, Component toSelf, Component toOthers, Runnable onComplete, Runnable onCancel) {
		super(data.getPlayer(), createInventory(data));
		this.data = data;
		this.newEquipment = newEquipment;
		this.toSelf = toSelf;
		this.toOthers = toOthers;
		this.onComplete = onComplete;
		this.onCancel = onCancel;
		setupInventory();
	}
	
	private static Inventory createInventory(PlayerSessionData data) {
		int rows = (int) Math.ceil(data.getMaxStorage() / 9.0) + 1;
		if (rows < 2) rows = 2;
		return Bukkit.createInventory(data.getPlayer(), rows * 9, Component.text("Replace a storage item", NamedTextColor.RED));
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		
		// Header row
		contents[0] = newEquipment.getItem();
		for (int i = 1; i < 8; i++) {
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		contents[NEVERMIND] = CoreInventory.createButton(Material.BARRIER, Component.text("Nevermind", NamedTextColor.RED));
		
		// Storage items starting at row 2 (index 9)
		Equipment[] storage = data.getStorage();
		for (int i = 0; i < data.getMaxStorage(); i++) {
			if (i < storage.length && storage[i] != null) {
				contents[i + 9] = storage[i].getItem();
			}
		}
		
		// Gray panes for remaining slots
		for (int i = 9; i < contents.length; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;
		
		int slot = e.getSlot();
		
		// Nevermind button
		if (slot == NEVERMIND) {
			p.playSound(p, Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
			p.closeInventory();
			return;
		}
		
		// Header row (slot 0 = new item display, slots 1-7 = panes)
		if (slot < 9) {
			if (slot == 0 && e.isRightClick()) {
				new BukkitRunnable() {
					public void run() {
						new EquipmentGlossaryInventory(p, newEquipment, StorageReplaceInventory.this);
					}
				}.runTask(NeoRogue.inst());
			}
			return;
		}
		
		// Storage area
		int storageIndex = slot - 9;
		Equipment[] storage = data.getStorage();
		if (storageIndex >= data.getMaxStorage() || storageIndex >= storage.length) return;
		if (storage[storageIndex] == null) return;
		
		Equipment existing = storage[storageIndex];
		
		// Right-click: glossary
		if (e.isRightClick()) {
			new BukkitRunnable() {
				public void run() {
					new EquipmentGlossaryInventory(p, existing, StorageReplaceInventory.this);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		
		// Left-click: replace
		if (existing.isCursed()) {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
			Util.msg(p, "You can't discard cursed items!");
			return;
		}
		if (existing.getType() == EquipmentType.WEAPON && data.countOwnedWeapons() <= 1) {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
			Util.msg(p, "You can't discard your last weapon!");
			return;
		}
		if (PlayerSessionData.isUnlimitedAmmunition(existing) && data.countOwnedUnlimitedAmmunition() <= 1) {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
			Util.msg(p, "You can't discard your last unlimited ammunition!");
			return;
		}
		
		if (toSelf != null) {
			data.getSession().broadcastOthers(toOthers, p);
			Util.msg(p, toSelf.append(Component.text(", it replaced ")).append(existing.getHoverable()).append(Component.text(" in storage.")));
		}
		storage[storageIndex] = newEquipment;
		replaced = true;
		p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		p.closeInventory();
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (replaced) {
			if (onComplete != null) onComplete.run();
		} else if (onCancel != null) {
			new BukkitRunnable() {
				public void run() {
					onCancel.run();
				}
			}.runTask(NeoRogue.inst());
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
