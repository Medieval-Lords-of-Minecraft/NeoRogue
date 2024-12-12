package me.neoblade298.neorogue.session.fight;

import java.util.Map.Entry;

import me.neoblade298.neorogue.session.fight.status.Status;

public class StatusDamageSlice extends DamageSlice {
	private static final double ROUNDTO = 0.2;
	private Status s;
	public StatusDamageSlice(DamageType type, Status s) {
		super(s.getSlices().first().getFightData(), s.getStacks() * 0.2, type);
		this.s = s;
	}
	@Override
	public void handleStatistics(DamageType type, double damage) {
		int stacks = s.getStacks();
		// Evenly split damage stat among all players, rounded to 0.2
		PlayerFightData first = null;
		for (Entry<FightData, Integer> ent : s.getSlices().getSliceOwners().entrySet()) {
			if (ent.getKey() instanceof PlayerFightData) {
				PlayerFightData pfd = (PlayerFightData) ent.getKey();
				if (first == null) first = pfd;
				double pct = (double) ent.getValue() / stacks;
				pfd.getStats().addDamageDealt(type, Math.round(damage * pct / ROUNDTO) * ROUNDTO);
			}
		}

		// Arbitrarily give first stack owner leftover damage that isn't rounded to 0.2
		if (first != null) first.getStats().addDamageDealt(type, damage % 0.2);
	}
}
