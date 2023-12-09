package me.neoblade298.neorogue.session.fight.buff;

public enum BuffType {
	SLASHING("Slashing"),
	PIERCING("Piercing"),
	BLUNT("Blunt"),
	FIRE("Fire"),
	ICE("Ice"),
	LIGHTNING("Lightning"),
	EARTH("Earth"),
	DARK("Dark"),
	LIGHT("Light"),
	STATUS("Status"),
	PHYSICAL("Physical"),
	MAGICAL("Magical"),
	GENERAL("General"),
	SHIELD("Shield"),
	BLEED("Bleed"),
	POISON("Poison"),
	OTHER("Other");
	
	private String display;
	private BuffType(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
}
