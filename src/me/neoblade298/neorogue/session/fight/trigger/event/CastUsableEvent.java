package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.ArrayList;
import java.util.UUID;

import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.buff.Buff;

public class CastUsableEvent {
	private EquipmentInstance instance;
	private ArrayList<String> modifiers = new ArrayList<String>();
	private Buff staminaBuff = new Buff(), manaBuff = new Buff(), cooldownBuff = new Buff();

	public EquipmentInstance getInstance() {
		return instance;
	}

	public CastUsableEvent(EquipmentInstance instance) {
		super();
		this.instance = instance;
	}
	
	// Id is so that equipment that modify the event can later check if the event successfully cast
	public void addBuff(PropertyType type, UUID applier, String id, double amount, boolean multiplier) {
		Buff b = getBuff(type);
		modifiers.add(id);
		if (multiplier) {
			b.addMultiplier(applier, amount);
		}
		else {
			b.addIncrease(applier, amount);
		}
	}
	
	public Buff getBuff(PropertyType type) {
		switch (type) {
		case COOLDOWN:
			return cooldownBuff;
		case MANA_COST:
			return manaBuff;
		case STAMINA_COST:
			return staminaBuff;
		default:
			return null;
		}
	}
	
	public boolean hasId(String id) {
		return modifiers.contains(id);
	}
}
