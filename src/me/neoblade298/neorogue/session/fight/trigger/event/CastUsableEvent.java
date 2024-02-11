package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.EquipmentInstance;

public class CastUsableEvent {
	private EquipmentInstance instance;

	public EquipmentInstance getInstance() {
		return instance;
	}

	public void setInstance(EquipmentInstance instance) {
		this.instance = instance;
	}

	public CastUsableEvent(EquipmentInstance instance) {
		super();
		this.instance = instance;
	}
}
