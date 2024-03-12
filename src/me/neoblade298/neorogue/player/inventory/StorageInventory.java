package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StorageInventory extends CoreInventory {
	private Player spectator;
	private PlayerSessionData data;

	public StorageInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), data.getMaxStorage() <= 9 ? 9 : 18, Component.text("Storage", NamedTextColor.GOLD)));
		new PlayerSessionInventory(data);
		this.data = data;
		setupInventory();
	}
	
	public StorageInventory(PlayerSessionData data, Player spectator) {
		super(spectator, Bukkit.createInventory(spectator, data.getMaxStorage() <= 9 ? 9 : 18, Component.text(data.getData().getDisplay() + "'s Storage", NamedTextColor.GOLD)));
		this.data = data;
		this.spectator = spectator;
		setupInventory();
	}
	
	private void setupInventory() {
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		Equipment[] storage = data.getStorage();
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null) continue;
			contents[i] = storage[i].getItem();
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		if (spectator != null) {
			e.setCancelled(true);
		}
		if (e.getCurrentItem() == null) return;
		if (e.isShiftClick()) {
			e.setCancelled(true);
			PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
			if (!pinv.handleShiftClickIn(e.getCurrentItem())) return;
			e.setCurrentItem(null);
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
		else {
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (spectator != null) return;
		// Save storage
		Equipment[] newSave = new Equipment[PlayerSessionData.MAX_STORAGE_SIZE];
		int iter = 0;
		for (ItemStack item : inv.getContents()) {
			if (item == null) continue;
			NBTItem nbti = new NBTItem(item);
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
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
	
	public boolean canShiftClickIn() {
		return inv.firstEmpty() != -1;
	}

	public void handleShiftClickIn(ItemStack item) {
		inv.addItem(item);
	}
}
