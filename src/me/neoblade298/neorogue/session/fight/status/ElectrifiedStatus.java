package me.neoblade298.neorogue.session.fight.status;

import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

public class ElectrifiedStatus extends DecrementStackStatus {

	public ElectrifiedStatus(String id, FightData target) {
		super(id, target, StatusClass.NEGATIVE);
	}

	
	@Override
	public void onTickAction() {
		// Owner is arbitrarily first slice of damage
		FightData owner = slices.getSliceOwners().entrySet().iterator().next().getKey();
		DamageMeta meta = new DamageMeta(owner);
		meta.isSecondary(true);
		for (Entry<FightData, Integer> ent : slices.getSliceOwners().entrySet()) {
			meta.addDamageSlice(new DamageSlice(ent.getKey(), ent.getValue() * 0.2, DamageType.LIGHTNING, true));
		}
		FightInstance.dealDamage(meta, data.getEntity());
	}
	
	// TODO: Create electrified projectile
}
