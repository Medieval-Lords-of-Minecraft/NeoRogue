package me.neoblade298.neorogue.session.fight.status;

import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class BleedStatus extends DecrementStackStatus {
	private static String id = "BLEED";

	public BleedStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}
	
	@Override
	public void onTickAction() {
		// Owner is arbitrarily first slice of damage
		FightData owner = slices.getSliceOwners().entrySet().iterator().next().getKey();
		DamageMeta meta = new DamageMeta(owner);
		meta.isSecondary(true);
		for (Entry<FightData, Integer> ent : slices.getSliceOwners().entrySet()) {
			meta.addDamageSlice(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.BLEED, true));
		}
		FightInstance.dealDamage(meta, data.getEntity());
	}
}
