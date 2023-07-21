package me.neoblade298.neorogue.session.fights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.player.KeyBind;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Status;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.TriggerAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.*;

public class FightData {
	private FightInstance inst;
	private PlayerSessionData sessdata;
	private HashMap<Trigger, HashMap<String, EquipmentInstance>> equips = new HashMap<Trigger, HashMap<String, EquipmentInstance>>();
	private HashMap<String, Status> statuses = new HashMap<String, Status>();
	
	private HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();
	
	private double health, stamina = 0, mana = 0;
	
	// Buffs
	private HashMap<BuffType, Buff> damageBuffs = new HashMap<BuffType, Buff>();
	private HashMap<BuffType, Buff> defenseBuffs = new HashMap<BuffType, Buff>();
	
	public FightData(FightInstance inst, PlayerSessionData data) {
		this.inst = inst;
		this.sessdata = data;
		this.health = data.getHealth();
		
		// Initialize buffs
		for (BuffType type : BuffType.values()) {
			damageBuffs.put(type, new Buff());
			defenseBuffs.put(type, new Buff());
		}
		
		// Initialize fight data
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.initialize(data.getPlayer(), this, inst, null);
		}
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.initialize(data.getPlayer(), this, inst, null);
		}
		for (int i = 0; i < data.getHotbar().length; i++) {
			Usable hotbar = data.getHotbar()[i];
			if (hotbar == null) continue;
			
			Trigger t = null;
			hotbar.initialize(data.getPlayer(), this, inst, t);
		}
		for (int i = 0; i < data.getOtherBinds().length; i++) {
			Usable other = data.getOtherBinds()[i];
			if (other == null) continue;
			other.initialize(data.getPlayer(), this, inst, KeyBind.getBindFromData(i).getTrigger());
		}
		for (Artifact art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize(data.getPlayer(), this, inst, null);
		}
		data.getOffhand().initialize(data.getPlayer(), this, inst, null);
	}
	
	public void cleanup() {
		for (BukkitTask task : tasks.values()) {
			task.cancel();
		}
	}
	
	public FightData(UUID uuid) {
		// Used to initialize mobs
	}
	
	public boolean runActions(Trigger trigger, Object[] inputs) {
		if (equips.containsKey(trigger)) {
			Iterator<EquipmentInstance> iter = equips.get(trigger).values().iterator();
			while (iter.hasNext()) {
				EquipmentInstance inst = iter.next();
				if (inst.canTrigger()) {
					if (inst.trigger(inputs)) iter.remove();
				}
				else {
					inst.sendCooldownMessage(sessdata.getPlayer());
				}
			}
			return true;
		}
		return false;
	}
	
	public void addEquipmentInstance(String id, Trigger trigger, EquipmentInstance inst) {
		HashMap<String, EquipmentInstance> actions = equips.containsKey(trigger) ?
				equips.get(trigger) : new HashMap<String, EquipmentInstance>();
		actions.put(id, inst);
		equips.putIfAbsent(trigger, actions);
	}
	
	public FightInstance getInstance() {
		return inst;
	}
	
	public Player getPlayer() {
		return sessdata.getData().getPlayer();
	}
	
	public void addBuff(String id, boolean damageBuff, boolean multiplier, BuffType type, double amount, int seconds) {
		Buff b = damageBuff ? damageBuffs.get(type) : defenseBuffs.get(type);
		if (multiplier) b.addMultiplier(amount);
		else b.addIncrease(amount);
		
		if (seconds > 0) {
			tasks.put(id, new BukkitRunnable() {
				public void run() {
					addBuff(id, damageBuff, multiplier, type, -amount, -1);
					tasks.remove(id);
				}
			}.runTaskLater(NeoRogue.inst(), seconds * 20));
		}
	}
	
	public Buff getBuff(boolean damageBuff, BuffType type) {
		return damageBuff ? damageBuffs.get(type) : defenseBuffs.get(type);
	}
}
