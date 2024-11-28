package me.neoblade298.neorogue.session.fight.buff;

import java.util.EnumMap;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;

// In the future maybe buffs for other types like shields, status application?
public class DamageBuffType {
    private DamageCategory damageCategory;
	private DamageOrigin damageOrigin;
	private BuffOrigin buffOrigin;
	private static EnumMap<DamageCategory, EnumMap<DamageOrigin, EnumMap<BuffOrigin, DamageBuffType>>> map =
		new EnumMap<DamageCategory, EnumMap<DamageOrigin, EnumMap<BuffOrigin, DamageBuffType>>>(DamageCategory.class);

	static {
		for (DamageCategory category : DamageCategory.values()) {
			EnumMap<DamageOrigin, EnumMap<BuffOrigin, DamageBuffType>> submap = new EnumMap<DamageOrigin, EnumMap<BuffOrigin, DamageBuffType>>(DamageOrigin.class);
			map.put(category, submap);
			for (DamageOrigin origin : DamageOrigin.values()) {
				EnumMap<BuffOrigin, DamageBuffType> subsubmap = new EnumMap<BuffOrigin, DamageBuffType>(BuffOrigin.class);
				submap.put(origin, subsubmap);
				for (BuffOrigin bo : BuffOrigin.values()) {
					subsubmap.put(bo, new DamageBuffType(category, origin, bo));
				}
			}
		}
	}

	public static DamageBuffType of(DamageCategory category, DamageOrigin origin, BuffOrigin buffOrigin) {
		return map.get(category).get(origin).get(buffOrigin);
	}

	public static DamageBuffType fromString(String category) {
		return of(DamageCategory.valueOf(category), DamageOrigin.NORMAL, BuffOrigin.NORMAL);
	}

	public static DamageBuffType of(DamageCategory category) {
		return of(category, DamageOrigin.NORMAL, BuffOrigin.NORMAL);
	}

	private DamageBuffType(DamageCategory damageCategory, DamageOrigin damageOrigin, BuffOrigin buffOrigin) {
		this.damageCategory = damageCategory;
		this.damageOrigin = damageOrigin;
		this.buffOrigin = buffOrigin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((damageCategory == null) ? 0 : damageCategory.hashCode());
		result = prime * result + ((damageOrigin == null) ? 0 : damageOrigin.hashCode());
		result = prime * result + ((buffOrigin == null) ? 0 : buffOrigin.hashCode());
		return result;
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
		if (damageCategory != other.damageCategory)
			return false;
		if (damageOrigin != other.damageOrigin)
			return false;
		if (buffOrigin != other.buffOrigin)
			return false;
		return true;
	}
}