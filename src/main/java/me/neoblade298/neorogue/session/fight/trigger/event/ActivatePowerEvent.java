package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.Equipment;

public class ActivatePowerEvent {
	private Equipment equipment;

	public ActivatePowerEvent(Equipment equipment) {
		this.equipment = equipment;
	}

	public Equipment getEquipment() {
		return equipment;
	}
}
