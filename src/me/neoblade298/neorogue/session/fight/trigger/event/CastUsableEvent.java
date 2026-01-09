package me.neoblade298.neorogue.session.fight.trigger.event;

import java.util.ArrayList;

import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;

public class CastUsableEvent {
	private EquipmentInstance instance;
	private CastType type;
	private double manaCost, staminaCost, cooldown;
	private ArrayList<String> tags;

	public EquipmentInstance getInstance() {
		return instance;
	}

	public CastUsableEvent(EquipmentInstance instance, CastType type, double manaCost, double staminaCost, double cooldown, ArrayList<String> tags) {
		super();
		this.instance = instance;
		this.type = type;
		this.manaCost = manaCost;
		this.staminaCost = staminaCost;
		this.cooldown = cooldown;
		this.tags = tags;
	}

	public CastType getType() {
		return type;
	}

	public double getManaCost() {
		return manaCost;
	}
	public double getStaminaCost() {
		return staminaCost;
	}
	public double getCooldown() {
		return cooldown;
	}
	public ArrayList<String> getTags() {
		return tags;
	}
	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}
}
