package me.neoblade298.neorogue.session.fight.status;

import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.TickAction;

public class PoisonStatus extends BasicStatus {
	private static String id = "POISON";

	public PoisonStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		if (this.stacks > 0 && action == null) {
			action = new PoisonTickAction();
			holder.addTickAction(action);
		}
	}

	private class PoisonTickAction extends TickAction {
		@Override
		public TickResult run() {
			if (action.isCancelled()) return TickResult.REMOVE;
			if (stacks <= 0) return TickResult.REMOVE;

			double damagePerStack = 1;
			FightData owner = slices.getSliceOwners().entrySet().iterator().next().getKey();
			DamageMeta meta = new DamageMeta(owner);
			meta.isSecondary(true);
			for (Entry<FightData, Integer> ent : slices.getSliceOwners().entrySet()) {
				meta.addDamageSlice(new DamageSlice(ent.getKey(), ent.getValue() * damagePerStack, DamageType.POISON, true, DamageStatTracker.poison()));
			}
			FightInstance.dealDamage(meta, holder.getEntity());
			return TickResult.KEEP;
		}
	}
}
