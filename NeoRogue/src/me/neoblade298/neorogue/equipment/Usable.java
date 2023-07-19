package me.neoblade298.neorogue.equipment;

public abstract class Usable extends Equipment {
	private long lastUsed = 0L;
	protected int cooldown = 0;

	public Usable(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
	}
	
	public int getCooldown() {
		return cooldown;
	}
	
	public boolean isOnCooldown() {
		long timeElapsed = System.currentTimeMillis() - lastUsed;
		return (timeElapsed/1000) > cooldown;
	}
}
