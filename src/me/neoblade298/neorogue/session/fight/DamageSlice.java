package me.neoblade298.neorogue.session.fight;

public class DamageSlice {
	private FightData owner;
	private double damage;
	private DamageType type, postBuffType;
	private DamageStatTracker tracker;
	private boolean ignoreShields;
	public DamageSlice(FightData owner, double damage, DamageType type, DamageStatTracker tracker) {
		super();
		this.owner = owner;
		this.damage = damage;
		this.type = type;
		this.tracker = tracker;
	}
	public DamageSlice(FightData owner, double damage, DamageType type, DamageType postBuffType,
			DamageStatTracker tracker) {
		this(owner, damage, type, tracker);
		this.postBuffType = postBuffType;
	}
	public DamageSlice(FightData owner, double damage, DamageType type, boolean ignoreShields,
			DamageStatTracker tracker) {
		this(owner, damage, type, tracker);
		this.ignoreShields = ignoreShields;
		this.tracker = tracker;
	}
	public double getDamage() {
		return damage;
	}
	public DamageType getType() {
		return type;
	}
	public DamageStatTracker getTracker() {
		return tracker;
	}
	public void setPostBuffType(DamageType postBuffType) {
		this.postBuffType = postBuffType;
	}
	public DamageType getPostBuffType() {
		return this.postBuffType != null ? this.postBuffType : this.type;
	}
	public boolean isSimilar(DamageSlice slice) {
		return this.tracker.getId().equals(slice.getTracker().getId()) &&
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

	@Override
	public String toString() {
		return type.getDisplay() + "-" + damage;
	}
}
