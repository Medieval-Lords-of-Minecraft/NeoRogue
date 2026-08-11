package me.neoblade298.neorogue.session.fight.status;

import java.util.HashSet;
import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public abstract class FixedContributionStatus extends DecrementStackStatus {
	private final DamageBuffType buffType;
	private final double increase;
	private final double multiplier;
	private final boolean defenseBuff;
	private final boolean damageBuffed;
	private final HashSet<PlayerFightData> creditedAppliers = new HashSet<PlayerFightData>();
	private FightData fallbackApplier;

	protected FixedContributionStatus(StatusType type, FightData holder, DamageCategory category,
			double increase, double multiplier, boolean defenseBuff, boolean damageBuffed) {
		super(type.name(), holder, StatusClass.NEGATIVE);
		this.buffType = DamageBuffType.of(category);
		this.increase = increase;
		this.multiplier = multiplier;
		this.defenseBuff = defenseBuff;
		this.damageBuffed = damageBuffed;
	}

	@Override
	public void apply(FightData applier, int stacks, int ticks) {
		super.apply(applier, stacks, ticks);
		if (this.stacks <= 0) {
			clearContributionBuffs();
			return;
		}
		refreshContributionBuffs();
	}

	@Override
	public void onTickAction(int toRemove) {
		if (stacks - toRemove <= 0) clearContributionBuffs();
	}

	@Override
	protected void onStacksDecremented(int stacksRemoved) {
		refreshContributionBuffs();
	}

	private void refreshContributionBuffs() {
		HashSet<PlayerFightData> activeAppliers = new HashSet<PlayerFightData>();
		for (Entry<FightData, Integer> entry : slices.getSliceOwners().entrySet()) {
			if (entry.getValue() > 0 && entry.getKey() instanceof PlayerFightData) {
				activeAppliers.add((PlayerFightData) entry.getKey());
			}
		}

		clearContributionBuffs();
		if (activeAppliers.isEmpty()) {
			fallbackApplier = slices.first().getFightData();
			addBuff(new Buff(fallbackApplier, increase, multiplier, BuffStatTracker.of(StatusType.valueOf(id))));
			return;
		}

		for (PlayerFightData applier : activeAppliers) {
			BuffStatTracker tracker = damageBuffed
					? BuffStatTracker.statusDamageBuff(StatusType.valueOf(id), applier.getUniqueId())
					: BuffStatTracker.statusDamageMitigated(StatusType.valueOf(id), applier.getUniqueId());
			addBuff(new Buff(applier, increase / activeAppliers.size(), multiplier / activeAppliers.size(), tracker));
		}
		creditedAppliers.addAll(activeAppliers);
	}

	private void clearContributionBuffs() {
		if (fallbackApplier != null) {
			addBuff(new Buff(fallbackApplier, 0, 0, BuffStatTracker.of(StatusType.valueOf(id))));
			fallbackApplier = null;
		}
		for (PlayerFightData applier : creditedAppliers) {
			BuffStatTracker tracker = damageBuffed
					? BuffStatTracker.statusDamageBuff(StatusType.valueOf(id), applier.getUniqueId())
					: BuffStatTracker.statusDamageMitigated(StatusType.valueOf(id), applier.getUniqueId());
			addBuff(new Buff(applier, 0, 0, tracker));
		}
		creditedAppliers.clear();
	}

	private void addBuff(Buff buff) {
		if (defenseBuff) holder.addDefenseBuff(buffType, buff);
		else holder.addDamageBuff(buffType, buff);
	}
}