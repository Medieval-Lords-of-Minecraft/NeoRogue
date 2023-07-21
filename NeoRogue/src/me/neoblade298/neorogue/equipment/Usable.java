package me.neoblade298.neorogue.equipment;

public abstract class Usable extends Equipment {
	protected int cooldown = 0;

	public Usable(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
	}
	
	public int getCooldown() {
		return cooldown;
	}
}
