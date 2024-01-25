package me.neoblade298.neorogue.session.fight;

import java.util.UUID;

public class DamageSlice {
	private UUID owner;
	private double damage;
	private DamageType type;
	private boolean ignoreShields;
	public DamageSlice(UUID owner, double damage, DamageType type) {
		super();
		this.owner = owner;
		this.damage = damage;
		this.type = type;
	}
	public DamageSlice(UUID owner, double damage, DamageType type, boolean ignoreShields) {
		super();
		this.owner = owner;
		this.damage = damage;
		this.type = type;
		this.ignoreShields = ignoreShields;
	}
	public double getDamage() {
		return damage;
	}
	public DamageType getType() {
		return type;
	}
	public boolean isIgnoreShields() {
		return ignoreShields;
	}
	public boolean isPlayerOwner() {
		return FightInstance.getUserData().containsKey(owner);
	}
	public PlayerFightData getOwner() {
		return FightInstance.getUserData(owner);
	}
}
