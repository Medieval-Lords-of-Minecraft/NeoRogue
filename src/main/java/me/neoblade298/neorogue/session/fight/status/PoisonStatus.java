package me.neoblade298.neorogue.session.fight.status;

import java.util.Map.Entry;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.TickAction;

public class PoisonStatus extends BasicStatus {
	private static String id = "POISON";
	private static final String POTION_CLEANUP_ID = id + "-potion";

	public PoisonStatus(FightData data) {
		super(id, data, StatusClass.NEGATIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		if (this.stacks > 0) {
			holder.getEntity().addPotionEffect(
					new PotionEffect(PotionEffectType.POISON, PotionEffect.INFINITE_DURATION, 0));
			holder.addCleanupTask(POTION_CLEANUP_ID, this::removePotionEffect);
		}
		else {
			removePotionEffect();
			holder.removeCleanupTask(POTION_CLEANUP_ID);
		}
		if (this.stacks > 0 && action == null) {
			action = new PoisonTickAction();
			holder.addTickAction(action);
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();
		removePotionEffect();
		holder.removeCleanupTask(POTION_CLEANUP_ID);
	}

	private void removePotionEffect() {
		if (holder.getEntity() != null) holder.getEntity().removePotionEffect(PotionEffectType.POISON);
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
