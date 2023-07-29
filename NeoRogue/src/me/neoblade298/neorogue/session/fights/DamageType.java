package me.neoblade298.neorogue.session.fights;

public enum DamageType {
	SLASHING("Slashing", DamageCategory.PHYSICAL, new BuffType[]  { BuffType.SLASHING, BuffType.PHYSICAL, BuffType.GENERAL }),
	PIERCING("Piercing", DamageCategory.PHYSICAL, new BuffType[]  { BuffType.PIERCING, BuffType.PHYSICAL, BuffType.GENERAL }),
	BLUNT("Blunt", DamageCategory.PHYSICAL, new BuffType[]  { BuffType.BLUNT, BuffType.PHYSICAL, BuffType.GENERAL }),
	FIRE("Fire", DamageCategory.MAGICAL, new BuffType[]  { BuffType.FIRE, BuffType.MAGICAL, BuffType.GENERAL }),
	ICE("Ice", DamageCategory.MAGICAL, new BuffType[]  { BuffType.ICE, BuffType.MAGICAL, BuffType.GENERAL }),
	LIGHTNING("Lightning", DamageCategory.MAGICAL, new BuffType[]  { BuffType.LIGHTNING, BuffType.MAGICAL, BuffType.GENERAL }),
	EARTH("Earth", DamageCategory.MAGICAL, new BuffType[]  { BuffType.EARTH, BuffType.MAGICAL, BuffType.GENERAL }),
	DARK("Dark", DamageCategory.MAGICAL, new BuffType[]  { BuffType.DARK, BuffType.MAGICAL, BuffType.GENERAL }),
	LIGHT("Light", DamageCategory.MAGICAL, new BuffType[]  { BuffType.LIGHT, BuffType.MAGICAL, BuffType.GENERAL }),
	BLEED("Bleed", DamageCategory.STATUS, new BuffType[]  { BuffType.STATUS, BuffType.GENERAL }),
	POISON("Poison", DamageCategory.STATUS, new BuffType[]  { BuffType.STATUS, BuffType.GENERAL }),
	OTHER("Other", DamageCategory.OTHER, new BuffType[]  { BuffType.OTHER, BuffType.GENERAL });
	
	private DamageCategory category;
	private BuffType[] buffTypes;
	private String display;
	private DamageType(String display, DamageCategory category, BuffType[] buffTypes) {
		this.display = display;
		this.category = category;
		this.buffTypes = buffTypes;
	}
	
	public DamageCategory getCategory() {
		return category;
	}
	
	public BuffType[] getBuffTypes() {
		return buffTypes;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public boolean containsBuffType(BuffType type) {
		for (BuffType bt : buffTypes) {
			if (type == bt) return true;
		}
		return false;
	}
}
