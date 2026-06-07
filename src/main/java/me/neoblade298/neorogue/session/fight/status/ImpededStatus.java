package me.neoblade298.neorogue.session.fight.status;

import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class ImpededStatus extends BasicStatus {
	private static final String ID = "IMPEDED";
	private static final double VELOCITY_DEBUFF = -0.5;
	private boolean triggerAdded = false;

	public ImpededStatus(FightData data) {
		super(ID, data, StatusClass.NEGATIVE);
	}

	@Override
	public void onApply(FightData applier, int stacks) {
		super.onApply(applier, stacks);
		if (this.stacks > 0 && !triggerAdded && holder instanceof PlayerFightData pdata) {
			triggerAdded = true;
			pdata.addTrigger(ID, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, (pdata2, in) -> {
				if (this.stacks <= 0) return TriggerResult.remove();
				PreLaunchProjectileGroupEvent ev = (PreLaunchProjectileGroupEvent) in;
				ev.getVelocityBuffList().add(new Buff(slices.first().getFightData(), 0, VELOCITY_DEBUFF, BuffStatTracker.of(StatusType.IMPEDED)));
				return TriggerResult.keep();
			});
		}
	}
}
