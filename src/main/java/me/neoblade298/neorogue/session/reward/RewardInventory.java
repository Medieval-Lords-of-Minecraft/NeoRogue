package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;

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

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neorogue.equipment.accessories.PotOfGreed;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import me.neoblade298.neorogue.session.SessionType;
import me.neoblade298.neorogue.session.event.ClearRewardsEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RewardInventory extends CoreInventory {
	private boolean playSound = true;
	private ArrayList<Reward> rewards;
	private PlayerSessionData data;
	private Player spectator;
	
	public RewardInventory(PlayerSessionData data, ArrayList<Reward> rewards) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 9, Component.text("Rewards", NamedTextColor.BLUE)));
		
		this.data = data;
		this.rewards = rewards;
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		setupInventory();
	}
	public RewardInventory(PlayerSessionData data, ArrayList<Reward> rewards, Player spectator) {
		super(spectator, Bukkit.createInventory(spectator, 9, Component.text(data.getData().getDisplay() + "'s Rewards", NamedTextColor.BLUE)));
		
		this.data = data;
		this.rewards = rewards;
		this.spectator = spectator;
		setupInventory();
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		int i = 0;
		for (Reward reward : rewards) {
			contents[i++] = reward.getIcon(data);
		}
		if (data.getSession().getParty().size() > 1) 
			contents[7] = CoreInventory.createButton(Material.SPYGLASS, Component.text("View other players' rewards", NamedTextColor.GOLD));

		contents[8] = createSkipButton("Clear remaining rewards");
		inv.setContents(contents);
	}

	public ItemStack createSkipButton(String name) {
		if (data.getSession().getSessionType() == SessionType.TUTORIAL) return null;
		if (data.getArtifacts().containsKey(PotOfGreed.ID)) {
			ItemStack item = PotOfGreed.get().getItem();
			return CoreInventory.createButton(item.getType(),
					Component.text(name, NamedTextColor.RED),
					Component.text("Gain ", NamedTextColor.GRAY)
							.append(Component.text(PotOfGreed.GOLD + " " + PlayerSessionData.CURRENCY + " ", NamedTextColor.YELLOW))
							.append(Component.text("for each non-currency reward skipped")));
		}
		return CoreInventory.createButton(Material.RED_WOOL, Component.text(name, NamedTextColor.RED));
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getSlot();
		if (slot < rewards.size()) {
			Reward reward = rewards.get(slot);
			
			// Special spectator behavior
			if (spectator != null) {
				if (reward instanceof EquipmentChoiceReward) {
					((EquipmentChoiceReward) reward).spectate(data, slot, this, spectator);
				}
				return;
			}
			
			if (reward.claim(data, slot, this)) {
				playSound = false;
				if (!claimReward(slot)) {
					playSound = true;
					p.closeInventory();
				}
			}
		}
		else if (slot == 7 && data.getSession().getParty().size() > 1) {
			new SpectateSelectInventory(data.getSession(), p, data, true);
		}
		else if (slot == 8) {
			if (spectator != null) return;
			if (data.getSession().getSessionType() == SessionType.TUTORIAL) return;
			skipRewards(new ArrayList<Reward>(rewards));
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

	public boolean skipReward(int slot) {
		Reward reward = rewards.get(slot);
		ArrayList<Reward> skipped = new ArrayList<Reward>();
		skipped.add(reward);
		data.trigger(SessionTrigger.CLEAR_REWARDS, new ClearRewardsEvent(skipped));
		return claimReward(slot);
	}

	private void skipRewards(ArrayList<Reward> skipped) {
		data.trigger(SessionTrigger.CLEAR_REWARDS, new ClearRewardsEvent(skipped));
		rewards.removeAll(skipped);
		((RewardInstance) data.getSession().getInstance()).onRewardClaim();
	}
}
