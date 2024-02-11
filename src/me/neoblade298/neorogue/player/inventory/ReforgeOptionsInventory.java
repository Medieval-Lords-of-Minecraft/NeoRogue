package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.HashMap;

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
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReforgeOptionsInventory extends CoreInventory {
	private int slot, dataSlot;
	private boolean isEquipSlot;
	private PlayerSessionInventory prev;
	private ItemStack hostage;
	private EquipSlot type;
	private ArrayList<Equipment> reforgeOptions = new ArrayList<Equipment>();
	public ReforgeOptionsInventory(PlayerSessionInventory prev, int slot, boolean isEquipSlot, EquipSlot type, int dataSlot, Equipment toReforge, Equipment reforgeWith, ItemStack hostage) {
		super(prev.getPlayer(), Bukkit.createInventory(prev.getPlayer(), 18, Component.text("Reforge Options", NamedTextColor.BLUE)));
		
		this.slot = slot;
		this.isEquipSlot = isEquipSlot;
		this.hostage = hostage;
		this.prev = prev;
		this.type = type;
		this.dataSlot = dataSlot;

		ItemStack[] contents = inv.getContents();
		
		String[] options = toReforge.getReforgeOptions().get(reforgeWith.getId());
		int offset = options.length - 5; // -5 for middle of inv, -1 for 0 offset at size 2
		contents[3] = toReforge.getItem();
		contents[5] = reforgeWith.getItem();
		for (int i = 0; i < options.length; i++) {
			Equipment eq = Equipment.get(options[i], false);
			if (eq == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to load reforge option " + options[i] + " for item " + toReforge.getId() + ", skipping");
				continue;
			}
			contents[(2 * i) - offset + 9] = eq.getItem();
			reforgeOptions.add(eq);
		}

		for (int i = 0; i < 9; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		for (int i = 9; i < 18; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, Component.text("Cancel", NamedTextColor.RED));
		}
		
		inv.setContents(contents);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getSlot() < 9) return;
		if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
			p.closeInventory();
		}
		else {
			Equipment reforged = getFromSlot(e.getSlot());
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			
			if (isEquipSlot) {
				prev.getData().setEquipment(type, dataSlot, Equipment.get(reforged.getId(), false));
			}
			else {
				p.getInventory().setItem(slot, reforged.getItem());
			}
			hostage = null;
			p.closeInventory();
		}
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (hostage != null) {
			HashMap<Integer, ItemStack> failed = p.getInventory().addItem(hostage);
			if (!failed.isEmpty()) p.getWorld().dropItem(p.getLocation(), hostage);
		}
		new BukkitRunnable() {
			public void run() {
				new PlayerSessionInventory(prev.getData());
			}
		}.runTaskLater(NeoRogue.inst(), 5L);
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		
	}
	
	private Equipment getFromSlot(int slot) {
		int offset = reforgeOptions.size() - 5;
		slot += offset;
		slot -= 9;
		return reforgeOptions.get(slot / 2);
	}
}
