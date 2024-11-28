package me.neoblade298.neorogue.session.fight;

import java.util.EnumSet;

import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public enum DamageCategory {
	GENERAL("General", GlossaryTag.GENERAL, EnumSet.of(DamageType.SLASHING, DamageType.PIERCING, DamageType.BLUNT, DamageType.FIRE, DamageType.ICE,
		DamageType.LIGHTNING, DamageType.EARTHEN, DamageType.DARK, DamageType.LIGHT)),
	PHYSICAL("Physical", GlossaryTag.PHYSICAL, EnumSet.of(DamageType.SLASHING, DamageType.PIERCING, DamageType.BLUNT)),
	MAGICAL("Magical", GlossaryTag.MAGICAL, EnumSet.of(DamageType.FIRE, DamageType.ICE, DamageType.LIGHTNING, DamageType.EARTHEN, DamageType.DARK, DamageType.LIGHT)),
	SLASHING("Slashing", GlossaryTag.SLASHING, EnumSet.of(DamageType.SLASHING)),
	PIERCING("Piercing", GlossaryTag.PIERCING, EnumSet.of(DamageType.PIERCING)),
	BLUNT("Blunt", GlossaryTag.BLUNT, EnumSet.of(DamageType.BLUNT)),
	FIRE("Fire", GlossaryTag.FIRE, EnumSet.of(DamageType.FIRE)),
	ICE("Ice", GlossaryTag.ICE, EnumSet.of(DamageType.ICE)),
	LIGHTNING("Lightning", GlossaryTag.LIGHTNING, EnumSet.of(DamageType.LIGHTNING)),
	EARTHEN("Earthen", GlossaryTag.EARTHEN, EnumSet.of(DamageType.EARTHEN)),
	DARK("Dark", GlossaryTag.DARK, EnumSet.of(DamageType.DARK)),
	LIGHT("Light", GlossaryTag.LIGHT, EnumSet.of(DamageType.LIGHT)),
	STATUS("Status", null, EnumSet.of(DamageType.POISON, DamageType.THORNS, DamageType.REFLECT)),
	OTHER("Other", null, EnumSet.of(DamageType.FALL)),
	ALL("All", null, EnumSet.of(DamageType.SLASHING, DamageType.PIERCING, DamageType.BLUNT, DamageType.FIRE, DamageType.ICE,
		DamageType.LIGHTNING, DamageType.EARTHEN, DamageType.DARK, DamageType.LIGHT, DamageType.POISON));

	private String display;
	private GlossaryTag tag;
	private EnumSet<DamageType> types;
	private DamageCategory(String display, GlossaryTag tag, EnumSet<DamageType> types) {
		this.display = display;
		this.types = types;
	}

	public String getDisplay() {
		return display;
	}

	public boolean hasType(DamageType type) {
		return types.contains(type);
	}

	public GlossaryTag toGlossary() {
		return tag;
	}
}
