package me.neoblade298.neorogue.session;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class RewardInventory extends CoreInventory {
	private boolean playSound = true;
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
		contents[8] = CoreInventory.createButton(Material.RED_WOOL, "&cClear remaining rewards");
		inv.setContents(contents);
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
				playSound = false; // Don't play close inventory sound effect if opening another inv
				claimReward(slot);
				playSound = true;
			}
		}
		else if (slot == 8) {
			rewards.clear();
			((RewardInstance) data.getSession().getInstance()).onRewardClaim();
			p.closeInventory();
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (playSound) {
			p.playSound(p, Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	// True if there are still rewards remaining
	public boolean claimReward(int slot) {
		ItemStack[] contents = inv.getContents();
		for (int i = slot; i < rewards.size() - 1; i++) {
			contents[i] = contents[i + 1];
		}
		contents[rewards.size() - 1] = null;
		rewards.remove(slot);
		inv.setContents(contents);
		((RewardInstance) data.getSession().getInstance()).onRewardClaim();
		return !rewards.isEmpty();
	}
}
