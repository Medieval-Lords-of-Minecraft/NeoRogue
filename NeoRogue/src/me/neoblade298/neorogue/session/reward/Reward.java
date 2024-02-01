package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.player.PlayerSessionData;

public interface Reward {
	// True if the reward can be removed, false if not (usually when it involves opening a secondary choice inventory
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv);
	public ItemStack getIcon();
	public String serialize();
	
	public static Reward deserialize(String str) {
		if (str.startsWith("coins")) {
			return new CoinsReward(str.substring("coins:".length()));
		}
		else if (str.startsWith("choice")) {
			return new EquipmentChoiceReward(str.substring("choice:".length()));
		}
		else if (str.startsWith("equipment")) {
			return new EquipmentReward(str.substring("equipment:".length()));
		}
		return null;
	}
	
	public static ArrayList<Reward> deserializeArray(String str) {
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		String[] split = str.split(",");
		for (String s : split) {
			if (s.isBlank()) continue;
			rewards.add(Reward.deserialize(s));
		}
		return rewards;
	}
}
