package me.neoblade298.neorogue.session.fight.buff;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;

// In the future maybe buffs for other types like shields, status application?
public class DamageBuffType {
    private DamageCategory category;
	private DamageOrigin origin;
	private static DamageBuffType[] values = new DamageBuffType[DamageCategory.values().length * DamageOrigin.values().length];

	static {
		for (DamageCategory category : DamageCategory.values()) {
			for (DamageOrigin origin : DamageOrigin.values()) {
				values[hashCode(category, origin)] = new DamageBuffType(category, origin);
			}
		}
	}

	public static DamageBuffType of(DamageCategory category, DamageOrigin origin) {
		return values[hashCode(category, origin)];
	}

	private static int hashCode(DamageCategory category, DamageOrigin origin) {
		return category.ordinal() * DamageOrigin.values().length + origin.ordinal();
	}

	public static DamageBuffType fromString(String category) {
		return of(DamageCategory.valueOf(category), DamageOrigin.NORMAL);
	}

	public static DamageBuffType of(DamageCategory category) {
		return of(category, DamageOrigin.NORMAL);
	}

	private DamageBuffType(DamageCategory category, DamageOrigin origin) {
		this.category = category;
		this.origin = origin;
	}

	@Override
	public int hashCode() {
		return hashCode(category, origin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DamageBuffType other = (DamageBuffType) obj;
		if (category != other.category)
			return false;
		if (origin != other.origin)
			return false;
		return true;
	}
}