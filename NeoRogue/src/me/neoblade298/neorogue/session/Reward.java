package me.neoblade298.neorogue.session;

import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.player.PlayerSessionData;

public interface Reward {
	// True if the reward can be removed, false if not (usually when it involves opening a secondary choice inventory
	public boolean claim(PlayerSessionData data);
	public ItemStack getIcon();
}
