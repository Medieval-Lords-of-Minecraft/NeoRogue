package me.neoblade298.neorogue.session.fights;

public class DamageMeta {
	private double damage;
	private DamageType type;
	private boolean hitBarrier, isSecondary, bypassShields;
	
	public DamageMeta(double damage, DamageType type) {
		this.damage = damage;
		this.type = type;
	}
	
	public DamageMeta(double damage, DamageType type, boolean isSecondary) {
		this.damage = damage;
		this.type = type;
		this.isSecondary = isSecondary;
	}
	
	public DamageMeta(double damage, DamageType type, boolean hitBarrier, boolean isSecondary, boolean bypassShields) {
		this.damage = damage;
		this.type = type;
		this.hitBarrier = hitBarrier;
		this.isSecondary = isSecondary;
		this.bypassShields = bypassShields;
	}

	public double getDamage() {
		return damage;
	}

	public DamageType getType() {
		return type;
	}

	public boolean hitBarrier() {
		return hitBarrier;
	}

	// Secondary damage is only dealt from primary damage and will never result in more damage calls
	// Use for things that would otherwise loop forever, like thorns damage dealing thorns damage or dark damage applying on insanity
	public boolean isSecondary() {
		return isSecondary;
	}

	public boolean bypassShields() {
		return bypassShields;
	}
	
	public void setHitBarrier(boolean hitBarrier) {
		this.hitBarrier = hitBarrier;
	}
	
	public void setBypassShields(boolean bypassShields) {
		this.bypassShields = bypassShields;
	}
	
	public void setIsSecondary(boolean isSecondary) {
		this.isSecondary = isSecondary;
	}
}
