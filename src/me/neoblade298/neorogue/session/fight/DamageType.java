package me.neoblade298.neorogue.session.fight;

import java.util.EnumSet;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;

public enum DamageType {
	SLASHING("Slashing", "<yellow>Slashing</yellow>", GlossaryTag.SLASHING,
		EnumSet.of(DamageCategory.SLASHING, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	PIERCING("Piercing", "<red>Piercing</red>", GlossaryTag.PIERCING,
		EnumSet.of(DamageCategory.PIERCING, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	BLUNT("Blunt", "<gold>Blunt</gold>", GlossaryTag.BLUNT,
		EnumSet.of(DamageCategory.BLUNT, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	FIRE("Fire", "<dark_red>Fire</dark_red>", GlossaryTag.FIRE,
		EnumSet.of(DamageCategory.FIRE, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	ICE("Ice", "<blue>Ice</blue>", GlossaryTag.ICE,
		EnumSet.of(DamageCategory.ICE, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	LIGHTNING("Lightning", "<yellow>Lightning</yellow>", GlossaryTag.LIGHTNING,
		EnumSet.of(DamageCategory.LIGHTNING, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	EARTHEN("Earthen", "<dark_green>Earthen</dark_green>", GlossaryTag.EARTHEN,
		EnumSet.of(DamageCategory.EARTHEN, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	DARK("Dark", "<dark_purple>Dark</dark_purple>", GlossaryTag.DARK,
		EnumSet.of(DamageCategory.DARK, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	LIGHT("Light", "<white>Light</white>", GlossaryTag.LIGHT,
		EnumSet.of(DamageCategory.LIGHT, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	POISON("Poison", StatusType.POISON.tag, GlossaryTag.POISON,
		EnumSet.of(DamageCategory.STATUS, DamageCategory.ALL)),
	BURN("Burn", StatusType.BURN.tag, GlossaryTag.BURN,
		EnumSet.of(DamageCategory.FIRE, DamageCategory.STATUS, DamageCategory.ALL)),
	ELECTRIFIED("Electrified", StatusType.ELECTRIFIED.tag, GlossaryTag.ELECTRIFIED,
		EnumSet.of(DamageCategory.LIGHTNING, DamageCategory.STATUS, DamageCategory.ALL)),
	THORNS("Thorns", StatusType.THORNS.tag, GlossaryTag.THORNS,
		EnumSet.of(DamageCategory.STATUS, DamageCategory.ALL)),
	REFLECT("Reflect", StatusType.REFLECT.tag, GlossaryTag.REFLECT,
		EnumSet.of(DamageCategory.STATUS, DamageCategory.ALL)),
	FALL("Fall", "<white>Fall</white>", null,
		EnumSet.of(DamageCategory.ALL));
	
	public String tag;
	public Component ctag;
	private GlossaryTag glossary;
	private String display;
	private EnumSet<DamageCategory> categories;
	private DamageType(String display, String tag, GlossaryTag glossary, EnumSet<DamageCategory> categories) {
		this.display = display;
		this.categories = categories;
		this.tag = tag;
		this.ctag = SharedUtil.color(tag);
	}
	
	public EnumSet<DamageCategory> getCategories() {
		return categories;
	}
	
	public String getDisplay() {
		return display;
	}

	public GlossaryTag toGlossary() {
		return glossary;
	}
}
