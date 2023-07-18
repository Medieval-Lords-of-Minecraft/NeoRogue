package me.neoblade298.neorogue.equipment;

public abstract class Usable extends Equipment {
	private long lastUsed = 0L;
	
	public abstract int getCooldown();
}
