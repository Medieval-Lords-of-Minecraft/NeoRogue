package me.neoblade298.neorogue.session.fight.buff;

import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public enum BuffType {
	SLASHING("Slashing"),
	PIERCING("Piercing"),
	BLUNT("Blunt"),
	FIRE("Fire"),
	ICE("Ice"),
	LIGHTNING("Lightning"),
	EARTHEN("Earthen"),
	DARK("Dark"),
	LIGHT("Light"),
	STATUS("Status"),
	PHYSICAL("Physical"),
	MAGICAL("Magical"),
	GENERAL("General"),
	SHIELD("Shield"),
	BLEED("Bleed"),
	POISON("Poison"),
	ALL("All");
	
	private String display;
	private BuffType(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public static GlossaryTag toGlossary(BuffType type) {
		switch (type) {
		case BLUNT: return GlossaryTag.BLUNT;
		case DARK: return GlossaryTag.DARK;
		case EARTHEN: return GlossaryTag.EARTHEN;
		case FIRE: return GlossaryTag.FIRE;
		case ICE: return GlossaryTag.ICE;
		case LIGHT: return GlossaryTag.LIGHT;
		case LIGHTNING: return GlossaryTag.LIGHTNING;
		case PIERCING: return GlossaryTag.PIERCING;
		case POISON: return GlossaryTag.POISON;
		case SLASHING: return GlossaryTag.SLASHING;
		case MAGICAL: return GlossaryTag.MAGICAL;
		case PHYSICAL: return GlossaryTag.PHYSICAL;
		case SHIELD: return GlossaryTag.SHIELDS;
		default: return null; // So far general and status have this
		}
	}
}
