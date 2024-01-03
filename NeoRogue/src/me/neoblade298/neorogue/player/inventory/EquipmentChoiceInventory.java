package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.reward.RewardInventory;

public class EquipmentChoiceInventory extends CoreInventory {
	private RewardInventory prev;
	private ArrayList<Equipment> equips;
	private int prevSlot;
	private PlayerSessionData data;

	public EquipmentChoiceInventory(PlayerSessionData data, Inventory inv, ArrayList<Equipment> equips, RewardInventory prev, int prevSlot) {
		super(data.getPlayer(), inv);
		this.data = data;
		this.prev = prev;
		this.prevSlot = prevSlot;
		this.equips = equips;
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < equips.size(); i++) {
			contents[i] = equips.get(i).getItem();
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
		if (slot < equips.size()) {
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			Equipment eq = equips.get(slot);
			data.giveEquipment(eq);
			if (prev.claimReward(prevSlot)) {
				prev.openInventory();
			}
			else {
				p.playSound(p, Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
				p.closeInventory();
			}
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
