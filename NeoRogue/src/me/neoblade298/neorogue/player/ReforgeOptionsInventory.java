package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;

public class ReforgeOptionsInventory extends CoreInventory {
	private int slot, dataSlot;
	private boolean isEquipSlot;
	private Equipment toReforge;
	private PlayerSessionInventory prev;
	private ItemStack hostage;
	private String type;
	private ArrayList<Equipment> reforgeOptions = new ArrayList<Equipment>();
	public ReforgeOptionsInventory(PlayerSessionInventory prev, int slot, boolean isEquipSlot, String type, int dataSlot, Equipment toReforge, ItemStack hostage) {
		super(prev.getPlayer(), Bukkit.createInventory(prev.getPlayer(), 9, "§9Reforge Options"));
		
		this.slot = slot;
		this.isEquipSlot = isEquipSlot;
		this.toReforge = toReforge;
		this.hostage = hostage;
		this.prev = prev;
		this.type = type;
		this.dataSlot = dataSlot;

		ItemStack[] contents = inv.getContents();
		
		ArrayList<String> options = toReforge.getReforgeOptions();
		int offset = options.size() - 6; // -5 for middle of inv, -1 for 0 offset at size 1
		contents[0] = toReforge.getItem();
		for (int i = 0; i < options.size(); i++) {
			Equipment eq = Equipment.getEquipment(options.get(i), false);
			contents[(2 * i) - offset] = eq.getItem();
			reforgeOptions.add(eq);
		}
		
		for (int i = 0; i < 9; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, "&cCancel");
		}
		
		inv.setContents(contents);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getSlot() == 0) return;
		if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
			p.closeInventory();
		}
		else {
			Equipment reforged = getFromSlot(e.getSlot());
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			
			if (isEquipSlot) {
				prev.setEquipment(type, dataSlot, reforged.getId(), false);
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
		// TODO Auto-generated method stub
		
	}
	
	private Equipment getFromSlot(int slot) {
		int offset = reforgeOptions.size() - 6;
		slot += offset;
		return reforgeOptions.get(slot / 2);
	}
}