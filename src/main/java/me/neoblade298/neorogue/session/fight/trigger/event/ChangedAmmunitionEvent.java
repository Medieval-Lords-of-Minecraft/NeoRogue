package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.AmmunitionInstance;

public class ChangedAmmunitionEvent {
	private AmmunitionInstance old, curr;

	public ChangedAmmunitionEvent(AmmunitionInstance old, AmmunitionInstance curr) {
		this.old = old;
		this.curr = curr;
	}

	public AmmunitionInstance getOldAmmo() {
		return old;
	}

	public AmmunitionInstance getCurrentAmmo() {
		return curr;
	}
}
