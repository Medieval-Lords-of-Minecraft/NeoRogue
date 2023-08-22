package me.neoblade298.neorogue.session;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;

public class RewardInventory extends CoreInventory {

	private ArrayList<Reward> rewards;
	
	public RewardInventory(Player p, Inventory inv, ArrayList<Reward> rewards) {
		super(p, inv);
		
		this.rewards = rewards;
		ItemStack[] contents = inv.getContents();
		int i = 0;
		for (Reward reward : rewards) {
			contents[i++] = reward.getIcon();
		}
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Reward reward = rewards.get(e.getRawSlot());
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		// TODO Auto-generated method stub
		
	}

}
