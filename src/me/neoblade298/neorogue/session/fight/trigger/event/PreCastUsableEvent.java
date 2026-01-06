package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.ArrayList;

import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class PreCastUsableEvent {
	private EquipmentInstance instance;
	private ArrayList<String> tags = new ArrayList<String>();
	private BuffList staminaBuff = new BuffList(), manaBuff = new BuffList(), cooldownBuff = new BuffList();

	public EquipmentInstance getInstance() {
		return instance;
	}

	public PreCastUsableEvent(EquipmentInstance instance) {
		super();
		this.instance = instance;
	}

	// Tag is so that equipment that modify the event can later check if the event
	// successfully cast, like Energy Battery
	public void addBuff(PropertyType type, String tag, Buff b) {
		BuffList curr = getBuff(type);
		tags.add(tag);
		curr.add(b);
	}

	public BuffList getBuff(PropertyType type) {
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

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

	public ArrayList<String> getTags() {
		return tags;
	}
}
