package me.neoblade298.neorogue.session.fight;

import java.util.EnumSet;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;

public enum DamageType {
	SLASHING("Slashing", "<yellow>Slashing</yellow>",
		EnumSet.of(DamageCategory.SLASHING, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	PIERCING("Piercing", "<red>Piercing</red>",
		EnumSet.of(DamageCategory.PIERCING, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	BLUNT("Blunt", "<gold>Blunt</gold>",
		EnumSet.of(DamageCategory.BLUNT, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	FIRE("Fire", "<dark_red>Fire</dark_red>",
		EnumSet.of(DamageCategory.FIRE, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	ICE("Ice", "<blue>Ice</blue>",
		EnumSet.of(DamageCategory.ICE, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	LIGHTNING("Lightning", "<yellow>Lightning</yellow>",
		EnumSet.of(DamageCategory.LIGHTNING, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	EARTHEN("Earthen", "<dark_green>Earthen</dark_green>",
		EnumSet.of(DamageCategory.EARTHEN, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	DARK("Dark", "<dark_purple>Dark</dark_purple>",
		EnumSet.of(DamageCategory.DARK, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	LIGHT("Light", "<white>Light</white>",
		EnumSet.of(DamageCategory.LIGHT, DamageCategory.MAGICAL, DamageCategory.GENERAL, DamageCategory.ALL)),
	POISON("Poison", StatusType.POISON.tag,
		EnumSet.of(DamageCategory.STATUS, DamageCategory.ALL)),
	REND("Rend", StatusType.REND.tag,
		EnumSet.of(DamageCategory.PIERCING, DamageCategory.PHYSICAL, DamageCategory.GENERAL, DamageCategory.STATUS, DamageCategory.ALL)),
	ELECTRIFIED("Electrified", StatusType.ELECTRIFIED.tag,
		EnumSet.of(DamageCategory.LIGHTNING, DamageCategory.STATUS, DamageCategory.ALL)),
	THORNS("Thorns", StatusType.THORNS.tag,
		EnumSet.of(DamageCategory.STATUS, DamageCategory.ALL)),
	REFLECT("Reflect", StatusType.REFLECT.tag,
		EnumSet.of(DamageCategory.STATUS, DamageCategory.ALL)),
	FALL("Fall", "<white>Fall</white>",
		EnumSet.of(DamageCategory.ALL));
	
	public String tag;
	public Component ctag;
	private String display;
	private EnumSet<DamageCategory> categories;
	private DamageType(String display, String tag, EnumSet<DamageCategory> categories) {
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
		switch (this) {
		case BLUNT: return GlossaryTag.BLUNT;
		case DARK: return GlossaryTag.DARK;
		case EARTHEN: return GlossaryTag.EARTHEN;
		case FIRE: return GlossaryTag.FIRE;
		case ICE: return GlossaryTag.ICE;
		case LIGHT: return GlossaryTag.LIGHT;
		case LIGHTNING: return GlossaryTag.LIGHTNING;
		case PIERCING: return GlossaryTag.PIERCING;
		case POISON: return GlossaryTag.POISON;
		case REFLECT: return GlossaryTag.REFLECT;
		case SLASHING: return GlossaryTag.SLASHING;
		case THORNS: return GlossaryTag.THORNS;
		default: return null;
		}
	}
}
