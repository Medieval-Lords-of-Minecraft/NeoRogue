package me.neoblade298.neorogue.player;

import java.util.Map.Entry;
import java.util.UUID;

import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class BleedStatus extends DecrementStackStatus {
	private static String id = "BLEED";

	public BleedStatus(FightData data) {
		super(id, data);
	}
	
	@Override
	public void onTickAction() {
		FightInstance.receiveDamage(null, DamageType.BLEED, stacks, true, false, data.getEntity());
		for (Entry<UUID, Integer> ent : slices.getSliceOwners().entrySet()) {
			FightInstance.getFightData(ent.getKey()).getStats().addDamageDealt(DamageType.BLEED, ent.getValue());
		}
	}
}