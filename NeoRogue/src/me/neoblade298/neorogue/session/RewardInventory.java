package me.neoblade298.neorogue.session;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class RewardInventory extends CoreInventory {

	private ArrayList<Reward> rewards;
	private PlayerSessionData data;
	
	public RewardInventory(PlayerSessionData data, Inventory inv, ArrayList<Reward> rewards) {
		super(data.getPlayer(), inv);
		
		this.data = data;
		this.rewards = rewards;
		ItemStack[] contents = inv.getContents();
		int i = 0;
		for (Reward reward : rewards) {
			contents[i++] = reward.getIcon();
		}
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getSlot();
		if (slot < rewards.size()) {
			Reward reward = rewards.get(slot);
			
			if (reward.claim(data, slot, this)) {
				claimReward(slot);
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

	public void claimReward(int slot) {
		rewards.remove(slot);
		ItemStack[] contents = inv.getContents();
		for (int i = slot; i < rewards.size() - 1; i++) {
			contents[i] = contents[i + 1];
		}
		contents[rewards.size() - 1] = null;
	}
}
