package me.neoblade298.neorogue.session;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CampfireUpgradeInventory extends CoreInventory {
	private CampfireInstance inst;
	
	public CampfireUpgradeInventory(Player p, CampfireInstance inst) {
		super(p, Bukkit.createInventory(p, InventoryType.SMITHING, Component.text("Upgrade Equipment", NamedTextColor.BLUE)));
		this.inst = inst;
		ItemStack[] contents = inv.getContents();
		contents[1] = CoreInventory.createButton(Material.PAPER, Component.text("Upgrade", NamedTextColor.BLUE),
				(TextComponent) NeoCore.miniMessage().deserialize("<gray>Place an item on the left to see what it upgrades into. "
				+ "<red>To skip upgrading, shift left click this paper."), 250, NamedTextColor.GRAY);
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.SMITHING) return;
		int slot = e.getSlot();
		
		if (slot == 0) {
			new BukkitRunnable() {
				public void run() {
					updateOutput();
				}
			}.runTask(NeoRogue.inst());
		}
		else if (slot == 1) {
			e.setCancelled(true);
			if (e.getClick().isShiftClick()) {
				inst.useUpgrade(p.getUniqueId());
			}
		}
		else {
			e.setCancelled(true);
			inv.setItem(0, null);
			ItemStack item = e.getCurrentItem();
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			if (id.isBlank()) {
				Util.displayError(p, "&cInvalid upgrade!");
				return;
			}
			
			p.getInventory().addItem(Equipment.get(id, true).getItem());
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			inst.useUpgrade(p.getUniqueId());
			p.closeInventory();
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (inv.getItem(0) != null) {
			p.getInventory().addItem(inv.getItem(0));
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	public void updateOutput() {
		ItemStack item = inv.getItem(0);
		if (item == null) {
			inv.setItem(2, null);
		}
		else {
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			boolean isUpgraded = nbti.getBoolean("isUpgraded");
			if (id.isBlank()) {
				inv.setItem(2, CoreInventory.createButton(Material.BARRIER, Component.text("This item is not equipment", NamedTextColor.RED)));
				return;
			}
			if (isUpgraded) {
				inv.setItem(2, CoreInventory.createButton(Material.BARRIER, Component.text("This item is already upgraded", NamedTextColor.RED)));
				return;
			}
			
			inv.setItem(2, Equipment.get(id, true).getItem());
		}
	}
}
