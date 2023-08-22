package me.neoblade298.neorogue.player;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.RewardInventory;

public class EquipmentChoiceInventory extends CoreInventory {
	private RewardInventory prev;
	private ArrayList<Equipment> equips;
	private int prevSlot;

	public EquipmentChoiceInventory(Player p, Inventory inv, ArrayList<Equipment> equips, RewardInventory prev, int prevSlot) {
		super(p, inv);
		this.prev = prev;
		this.prevSlot = prevSlot;
		this.equips = equips;
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < equips.size(); i++) {
			contents[i] = equips.get(i).getItem();
		}
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;
		
		int slot = e.getSlot();
		if (slot < equips.size()) {
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			Equipment eq = equips.get(slot);
			Util.msg(p, "You claimed your reward of " + eq.getDisplay());
			HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(eq.getItem());
			if (!overflow.isEmpty()) {
				for (ItemStack item : overflow.values()) {
					p.getWorld().dropItem(p.getLocation(), item);
				}
			}
			prev.claimReward(prevSlot);
			prev.openInventory();
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
