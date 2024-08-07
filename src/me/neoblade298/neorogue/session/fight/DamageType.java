package me.neoblade298.neorogue.session.fight;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;

public enum DamageType {
	SLASHING("Slashing", DamageCategory.PHYSICAL, "<yellow>Slashing</yellow>",
			new BuffType[] { BuffType.SLASHING, BuffType.PHYSICAL, BuffType.GENERAL, BuffType.ALL }),
	PIERCING("Piercing", DamageCategory.PHYSICAL, "<red>Piercing</red>",
			new BuffType[] { BuffType.PIERCING, BuffType.PHYSICAL, BuffType.GENERAL, BuffType.ALL }),
	BLUNT("Blunt", DamageCategory.PHYSICAL, "<gold>Blunt</gold>",
			new BuffType[] { BuffType.BLUNT, BuffType.PHYSICAL, BuffType.GENERAL, BuffType.ALL }),
	FIRE("Fire", DamageCategory.MAGICAL, "<dark_red>Fire</dark_red>",
			new BuffType[] { BuffType.FIRE, BuffType.MAGICAL, BuffType.GENERAL, BuffType.ALL }),
	ICE("Ice", DamageCategory.MAGICAL, "<blue>Ice</blue>",
			new BuffType[] { BuffType.ICE, BuffType.MAGICAL, BuffType.GENERAL, BuffType.ALL }),
	LIGHTNING("Lightning", DamageCategory.MAGICAL, "<yellow>Lightning</yellow>",
			new BuffType[] { BuffType.LIGHTNING, BuffType.MAGICAL, BuffType.GENERAL, BuffType.ALL }),
	EARTHEN("Earthen", DamageCategory.MAGICAL, "<dark_green>Earthen</dark_green>",
			new BuffType[] { BuffType.EARTHEN, BuffType.MAGICAL, BuffType.GENERAL, BuffType.ALL }),
	DARK("Dark", DamageCategory.MAGICAL, "<dark_purple>Dark</dark_purple>",
			 new BuffType[] { BuffType.DARK, BuffType.MAGICAL, BuffType.GENERAL, BuffType.ALL }),
	LIGHT("Light", DamageCategory.MAGICAL, "<white>Light</white>",
			 new BuffType[] { BuffType.LIGHT, BuffType.MAGICAL, BuffType.GENERAL, BuffType.ALL }),
	BLEED("Bleed", DamageCategory.STATUS, StatusType.BLEED.tag,
			 new BuffType[] { BuffType.BLEED, BuffType.STATUS, BuffType.ALL }),
	POISON("Poison", DamageCategory.STATUS, StatusType.POISON.tag,
			 new BuffType[] { BuffType.POISON, BuffType.STATUS, BuffType.ALL }),
	THORNS("Thorns", DamageCategory.STATUS, StatusType.THORNS.tag,
			 new BuffType[] { BuffType.STATUS, BuffType.ALL }),
	REFLECT("Reflect", DamageCategory.STATUS, StatusType.REFLECT.tag,
			 new BuffType[] { BuffType.STATUS, BuffType.ALL }),
	FALL("Fall", DamageCategory.OTHER, "<white>Fall</white>",
			new BuffType[] { BuffType.ALL });
	
	public String tag;
	public Component ctag;
	private BuffType[] buffTypes;
	private String display;
	private DamageCategory category;
	private DamageType(String display, DamageCategory category, String tag, BuffType[] buffTypes) {
		this.display = display;
		this.category = category;
		this.buffTypes = buffTypes;
		this.tag = tag;
		this.ctag = SharedUtil.color(tag);
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
	
	public static GlossaryTag toGlossary(DamageType type) {
		switch (type) {
		case BLEED: return GlossaryTag.BLEED;
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
		case FALL: return null; // Should never need this as a glossary tag
		}
		return null;
	}
}
