package me.neoblade298.neorogue.session.fight.trigger;

import me.neoblade298.neorogue.equipment.EquipmentInstance;

public class TriggerCastUsableEvent {
	private EquipmentInstance eqi;
	
	public TriggerCastUsableEvent(EquipmentInstance eqi) {
		this.eqi = eqi;
	}
	
	public EquipmentInstance getEquipmentInstance() {
		return eqi;
	}
	
	public void setEquipmentInstance(EquipmentInstance eqi) {
		this.eqi = eqi;
	}
}
