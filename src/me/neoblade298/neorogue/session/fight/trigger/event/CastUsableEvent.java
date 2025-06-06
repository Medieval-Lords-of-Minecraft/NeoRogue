package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;

public class CastUsableEvent {
	private EquipmentInstance instance;
	private CastType type;

	public EquipmentInstance getInstance() {
		return instance;
	}

	public CastUsableEvent(EquipmentInstance instance, CastType type) {
		super();
		this.instance = instance;
		this.type = type;
	}

	public CastType getType() {
		return type;
	}
}
