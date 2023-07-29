package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

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

public class FightData {
	private FightInstance inst;
	private PlayerSessionData sessdata;
	private HashMap<Trigger, HashMap<String, TriggerAction>> triggers = new HashMap<Trigger, HashMap<String, TriggerAction>>();
	private HashMap<String, Status> statuses = new HashMap<String, Status>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>();
	private HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>>();

	private HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();

	private double health, stamina = 0, mana = 0;
	
	private Barrier barrier = null;
	private ShieldHolder shields = null;
	private Damageable entity = null;
	private LinkedList<TickAction> tickActions = new LinkedList<TickAction>();
	private FightStatistics stats = new FightStatistics();

	// Buffs
	private HashMap<BuffType, Buff> damageBuffs = new HashMap<BuffType, Buff>();
	private HashMap<BuffType, Buff> defenseBuffs = new HashMap<BuffType, Buff>();

	public FightData(FightInstance inst, PlayerSessionData data) {
		this(data.getPlayer());
		
		this.inst = inst;
		this.sessdata = data;
		this.health = data.getHealth();

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

	public void cleanup() {
		for (BukkitTask task : tasks.values()) {
			task.cancel();
		}
	}

	public FightData(Damageable e) {
		// Only use this for mobs
		this.entity = e;
		this.shields = new ShieldHolder(this);
		Plot p = Plot.locationToPlot(e.getLocation());
		Session s = SessionManager.getSession(p);
		inst = (FightInstance) s.getInstance();
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
	
	public Damageable getEntity() {
		return entity;
	}

	public void addBuff(UUID applier, boolean damageBuff, boolean multiplier, BuffType type, double amount) {
		Buff b = damageBuff ? damageBuffs.getOrDefault(type, new Buff()) : defenseBuffs.getOrDefault(type, new Buff());
		if (multiplier)
			b.addMultiplier(applier, amount);
		else
			b.addIncrease(applier, amount);
		
		if (damageBuff) damageBuffs.put(type, b);
		else defenseBuffs.put(type, b);
	}

	public void addBuff(UUID applier, String id, boolean damageBuff, boolean multiplier, BuffType type, double amount, int seconds) {
		addBuff(applier, damageBuff, multiplier, type, amount);
		String uid = applier.toString().substring(0,10) + id;

		if (seconds > 0) {
			addTask(uid, new BukkitRunnable() {
				public void run() {
					addBuff(applier, id, damageBuff, multiplier, type, -amount, -1);
					tasks.remove(uid);
				}
			}.runTaskLater(NeoRogue.inst(), seconds * 20));
		}
	}

	public Buff getBuff(boolean damageBuff, BuffType type) {
		return damageBuff ? damageBuffs.get(type) : defenseBuffs.get(type);
	}

	public void addTask(String id, BukkitTask task) {
		tasks.put(id, task);
	}

	public void removeTask(String id) {
		tasks.remove(id);
	}
	
	public void setBarrier(Barrier barrier) {
		this.barrier = barrier;
	}
	
	public Barrier getBarrier() {
		return barrier;
	}
	
	public ShieldHolder getShields() {
		return shields;
	}
	
	public void addTickAction(TickAction action) {
		FightInstance.addToTickList(entity.getUniqueId());
		tickActions.add(action);
	}
	
	public boolean runTickActions() {
		Iterator<TickAction> iter = tickActions.iterator();
		while (iter.hasNext()) {
			if (iter.next().run()) iter.remove();
		}
		return tickActions.isEmpty();
	}
	
	public void removeStatus(String id) {
		Status s = statuses.remove(id);
		if (s != null) s.cleanup();
	}
	
	public FightStatistics getStats() {
		return stats;
	}
	
	public boolean hasStatus(String id) {
		return statuses.containsKey(id);
	}
	
	public Status getStatus(String id) {
		return statuses.get(id);
	}
	
	public void applyStatus(String id, UUID applier, int stacks, int seconds) {
		Status s = statuses.getOrDefault(id, Status.createFromId(id, applier, this));
		s.apply(applier, stacks, seconds);
		statuses.put(id, s);
	}
}
