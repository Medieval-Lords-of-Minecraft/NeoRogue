package me.neoblade298.neorogue.session.fight.status;

import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;

import java.util.UUID;

public class BleedStatus extends DecrementStackStatus {
	private static String id = "BLEED";

	public BleedStatus(FightData data) {
		super(id, data);
	}
	
	@Override
	public void onTickAction() {
		FightInstance.dealDamage(new DamageMeta(null, stacks, DamageType.BLEED, false, true), data.getEntity());
		for (Entry<UUID, Integer> ent : slices.getSliceOwners().entrySet()) {
			FightInstance.getUserData(ent.getKey()).getStats().addDamageDealt(DamageType.BLEED, ent.getValue());
		}
	}
}
