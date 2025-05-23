package me.neoblade298.neorogue.session.fight;

public class DamageSlice {
	private FightData owner;
	private double damage;
	private DamageType type, postBuffType;
	private boolean ignoreShields;
	public DamageSlice(FightData owner, double damage, DamageType type) {
		super();
		this.owner = owner;
		this.damage = damage;
		this.type = type;
	}
	public DamageSlice(FightData owner, double damage, DamageType type, DamageType postBuffType) {
		super();
		this.owner = owner;
		this.damage = damage;
		this.type = type;
		this.postBuffType = postBuffType;
	}
	public DamageSlice(FightData owner, double damage, DamageType type, boolean ignoreShields) {
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
	public void setPostBuffType(DamageType postBuffType) {
		this.postBuffType = postBuffType;
	}
	public DamageType getPostBuffType() {
		return this.postBuffType != null ? this.postBuffType : this.type;
	}
	public boolean isSimilar(DamageSlice slice) {
		return this.type == slice.getType() && this.postBuffType == slice.getPostBuffType() &&
				this.ignoreShields == slice.isIgnoreShields() && this.owner == slice.owner;
	}
	public void add(DamageSlice slice) {
		this.damage += slice.getDamage();
	}
	public boolean isIgnoreShields() {
		return ignoreShields;
	}
	public boolean isPlayerOwner() {
		return owner instanceof PlayerFightData;
	}
	public PlayerFightData getOwner() {
		return (PlayerFightData) owner;
	}
	public void handleStatistics(DamageType type, double damage) {
		if (owner instanceof PlayerFightData) {
			PlayerFightData pfd = (PlayerFightData) owner;
			pfd.getStats().addDamageDealt(type, damage);
		}
	}

	@Override
	public String toString() {
		return type.getDisplay() + "-" + damage;
	}
}
