package me.neoblade298.neorogue.session.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.TriggerAction;
import me.neoblade298.neorogue.equipment.*;

public class FightData {
	private PlayerSessionData sessdata;
	private HashMap<Trigger, ArrayList<TriggerAction>> triggers = new HashMap<Trigger, ArrayList<TriggerAction>>();
	
	public FightData(PlayerSessionData data) {
		this.sessdata = data;
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.initialize(this);
		}
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.initialize(this);
		}
		for (Usable hotbar : data.getHotbar()) {
			if (hotbar == null) continue;
			hotbar.initialize(this);
		}
		for (Usable other : data.getOtherBinds()) {
			if (other == null) continue;
			other.initialize(this);
		}
		for (Artifact art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize(this);
		}
		data.getOffhand().initialize(this);
	}
	
	public FightData(UUID uuid) {
		// Used to initialize mobs
	}
	
	public void runActions(Trigger trigger, Object[] inputs) {
		if (triggers.containsKey(trigger)) {
			Iterator<TriggerAction> iter = triggers.get(trigger).iterator();
			while (iter.hasNext()) {
				if (!iter.next().run(inputs)) iter.remove();
			}
		}
	}
	
	public void addTrigger(Trigger trigger, TriggerAction action) {
		ArrayList<TriggerAction> actions = triggers.containsKey(trigger) ?
				triggers.get(trigger) : new ArrayList<TriggerAction>();
		actions.add(action);
		triggers.putIfAbsent(trigger, actions);
	}
}
