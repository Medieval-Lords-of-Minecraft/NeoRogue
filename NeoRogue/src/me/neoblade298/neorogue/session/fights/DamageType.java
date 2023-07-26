package me.neoblade298.neorogue.session.fights;

public enum DamageType {
	SLASHING(DamageCategory.PHYSICAL, new BuffType[]  { BuffType.SLASHING, BuffType.PHYSICAL, BuffType.GENERAL }),
	PIERCING(DamageCategory.PHYSICAL, new BuffType[]  { BuffType.PIERCING, BuffType.PHYSICAL, BuffType.GENERAL }),
	BLUNT(DamageCategory.PHYSICAL, new BuffType[]  { BuffType.BLUNT, BuffType.PHYSICAL, BuffType.GENERAL }),
	FIRE(DamageCategory.MAGICAL, new BuffType[]  { BuffType.FIRE, BuffType.MAGICAL, BuffType.GENERAL }),
	ICE(DamageCategory.MAGICAL, new BuffType[]  { BuffType.ICE, BuffType.MAGICAL, BuffType.GENERAL }),
	LIGHTNING(DamageCategory.MAGICAL, new BuffType[]  { BuffType.LIGHTNING, BuffType.MAGICAL, BuffType.GENERAL }),
	EARTH(DamageCategory.MAGICAL, new BuffType[]  { BuffType.EARTH, BuffType.MAGICAL, BuffType.GENERAL }),
	DARK(DamageCategory.MAGICAL, new BuffType[]  { BuffType.DARK, BuffType.MAGICAL, BuffType.GENERAL }),
	LIGHT(DamageCategory.MAGICAL, new BuffType[]  { BuffType.LIGHT, BuffType.MAGICAL, BuffType.GENERAL }),
	BLEED(DamageCategory.STATUS, new BuffType[]  { BuffType.STATUS, BuffType.GENERAL }),
	POISON(DamageCategory.STATUS, new BuffType[]  { BuffType.STATUS, BuffType.GENERAL }),
	OTHER(DamageCategory.OTHER, new BuffType[]  { BuffType.OTHER, BuffType.GENERAL });
	
	private DamageCategory category;
	private BuffType[] buffTypes;
	private DamageType(DamageCategory category, BuffType[] buffTypes) {
		this.category = category;
		this.buffTypes = buffTypes;
	}
	
	public DamageCategory getCategory() {
		return category;
	}
	
	public BuffType[] getBuffTypes() {
		return buffTypes;
	}
}
