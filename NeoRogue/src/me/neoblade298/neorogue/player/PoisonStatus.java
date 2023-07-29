package me.neoblade298.neorogue.player;

import java.util.Map.Entry;
import java.util.UUID;

import me.neoblade298.neorogue.session.fights.DamageMeta;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.TickAction;

public class PoisonStatus extends DurationStatus {
	private static String id = "POISON";

	public PoisonStatus(FightData data) {
		super(id, data);
	}
	
	@Override
	public void onTickAction() {
		FightInstance.receiveDamage(null, new DamageMeta(stacks, DamageType.POISON, true), data.getEntity());
		for (Entry<UUID, Integer> ent : slices.getSliceOwners().entrySet()) {
			FightInstance.getFightData(ent.getKey()).getStats().addDamageDealt(DamageType.POISON, ent.getValue());
		}
	}
}
