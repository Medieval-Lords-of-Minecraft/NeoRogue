package me.neoblade298.neorogue.session.fight;

import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public enum DamageCategory {
	GENERAL("General", GlossaryTag.GENERAL),
	PHYSICAL("Physical", GlossaryTag.PHYSICAL),
	MAGICAL("Magical", GlossaryTag.MAGICAL),
	SLASHING("Slashing", GlossaryTag.SLASHING),
	PIERCING("Piercing", GlossaryTag.PIERCING),
	BLUNT("Blunt", GlossaryTag.BLUNT),
	FIRE("Fire", GlossaryTag.FIRE),
	ICE("Ice", GlossaryTag.ICE),
	LIGHTNING("Lightning", GlossaryTag.LIGHTNING),
	EARTHEN("Earthen", GlossaryTag.EARTHEN),
	DARK("Dark", GlossaryTag.DARK),
	LIGHT("Light", GlossaryTag.LIGHT),
	POISON("Poison", GlossaryTag.POISON),
	BURN("Burn", GlossaryTag.BURN),
	STATUS("Status", null),
	OTHER("Other", null),
	ALL("All", null);

	private String display;
	private GlossaryTag tag;
	private DamageCategory(String display, GlossaryTag tag) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}

	public boolean hasType(DamageType type) {
		return type.getCategories().contains(this);
	}

	public GlossaryTag toGlossary() {
		return tag;
	}
}
