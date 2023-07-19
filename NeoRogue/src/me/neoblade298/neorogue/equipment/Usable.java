package me.neoblade298.neorogue.equipment;

import org.bukkit.inventory.ItemStack;

public abstract class Usable extends Equipment {
	private long lastUsed = 0L;
	private int cooldown = 0;

	public Usable(String id, ItemStack item) {
		super(id, item);
	}
	
	public int getCooldown() {
		return cooldown;
	}
}
