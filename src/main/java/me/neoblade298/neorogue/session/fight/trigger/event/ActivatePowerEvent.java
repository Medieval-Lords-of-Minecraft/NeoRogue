package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;

public class ActivatePowerEvent {
	private Equipment equipment;
	private int slot;
	private EquipSlot es;

	public ActivatePowerEvent(Equipment equipment, int slot, EquipSlot es) {
		this.equipment = equipment;
		this.slot = slot;
		this.es = es;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public int getSlot() {
		return slot;
	}

	public EquipSlot getEquipSlot() {
		return es;
	}
}
