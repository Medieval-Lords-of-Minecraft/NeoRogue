package me.neoblade298.neorogue.equipment;

import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;

// For most common use cases of having a counter and cooldown
public class StandardEquipmentInstance extends EquipmentInstance {
	private long time;
	private int count;
	private boolean bool;

	public StandardEquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
		super(data, eq, slot, es);
	}

	public StandardEquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, TriggerAction action) {
		super(data, eq, slot, es, action);
	}
	
	public StandardEquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, TriggerAction action, TriggerCondition condition) {
		super(data, eq, slot, es, action, condition);
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addCount(int count) {
		this.count += count;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public boolean getBool() {
		return bool;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}
}
