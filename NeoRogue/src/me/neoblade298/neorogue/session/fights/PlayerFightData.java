package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.player.KeyBind;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Status;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.TriggerAction;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;

public class PlayerFightData extends FightData {
	private PlayerSessionData sessdata;
	private HashMap<Trigger, HashMap<String, TriggerAction>> triggers = new HashMap<Trigger, HashMap<String, TriggerAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>();
	private HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>>();

	private double stamina = 0, mana = 0;
	
	private FightStatistics stats = new FightStatistics();

	public PlayerFightData(FightInstance inst, PlayerSessionData data) {
		super(data.getPlayer(), inst);
		
		this.inst = inst;
		this.sessdata = data;

		// Initialize fight data
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.initialize(data.getPlayer(), this, null, -1);
		}
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.initialize(data.getPlayer(), this, null, -1);
		}
		for (int i = 0; i < data.getHotbar().length; i++) {
			HotbarCompatible hotbar = data.getHotbar()[i];
			if (hotbar == null) continue;
			hotbar.initialize(data.getPlayer(), this, Trigger.getFromHotbarSlot(i), i);
		}
		for (int i = 0; i < data.getOtherBinds().length; i++) {
			Usable other = data.getOtherBinds()[i];
			if (other == null) continue;
			other.initialize(data.getPlayer(), this, KeyBind.getBindFromData(i).getTrigger(), -1);
		}
		for (Artifact art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize(data.getPlayer(), this, null, -1);
		}
		
		if (data.getOffhand() != null) {
			data.getOffhand().initialize(data.getPlayer(), this, null, -1);
		}
	}

	public boolean runActions(Trigger trigger, Object[] inputs) {
		if (triggers.containsKey(trigger)) {
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					if (!ei.canTrigger()) {
						ei.sendCooldownMessage(sessdata.getPlayer());
						continue;
					}
				}
				if (!inst.trigger(inputs)) iter.remove();
			}
			return true;
		}
		return false;
	}

	public boolean runSlotBasedTriggers(Trigger trigger, int hotbar, Object[] inputs) {
		if (!slotBasedTriggers.containsKey(hotbar)) return false;
		HashMap<Trigger, HashMap<String, TriggerAction>> triggers = slotBasedTriggers.get(hotbar);

		if (triggers.containsKey(trigger)) {
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					if (!ei.canTrigger()) {
						ei.sendCooldownMessage(sessdata.getPlayer());
						continue;
					}
				}
				if (!inst.trigger(inputs)) iter.remove();
			}
			return true;
		}
		return false;
	}
	
	public void addHotbarTrigger(String id, int hotbar, Trigger trigger, TriggerAction action) {
		HashMap<Trigger, HashMap<String, TriggerAction>> triggers = slotBasedTriggers.getOrDefault(hotbar, 
				new HashMap<Trigger, HashMap<String, TriggerAction>>());
		
		HashMap<String, TriggerAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new HashMap<String, TriggerAction>();
		addTrigger(id, actions, action);
		triggers.put(trigger, actions);
		slotBasedTriggers.put(hotbar, triggers);
	}

	public void addTrigger(String id, Trigger trigger, TriggerAction action) {
		HashMap<String, TriggerAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new HashMap<String, TriggerAction>();
		addTrigger(id, actions, action);
		triggers.put(trigger, actions);
	}
	
	private void addTrigger(String id, HashMap<String, TriggerAction> actions, TriggerAction action) {
		actions.put(id, action);

		if (action instanceof EquipmentInstance) {
			EquipmentInstance inst = (EquipmentInstance) action;
			equips.put(id, inst);
		}
	}

	public FightInstance getInstance() {
		return inst;
	}

	public Player getPlayer() {
		return sessdata.getData().getPlayer();
	}
	
	public FightStatistics getStats() {
		return stats;
	}
}
