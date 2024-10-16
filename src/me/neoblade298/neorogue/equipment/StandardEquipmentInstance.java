package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;

// For most common use cases of having a counter and cooldown
public class StandardEquipmentInstance extends EquipmentInstance {
	private long time;
	private int count;
	private boolean bool;

	public StandardEquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es) {
		super(p, eq, slot, es);
	}

	public StandardEquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es, TriggerAction action) {
		super(p, eq, slot, es, action);
	}
	
	public StandardEquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es, TriggerAction action, TriggerCondition condition) {
		super(p, eq, slot, es, action, condition);
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
