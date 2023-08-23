package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.entity.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.player.Status;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.MapSpawnerInstance;

public class FightData {
	protected FightInstance inst;
	protected HashMap<String, Status> statuses = new HashMap<String, Status>();

	protected HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();
	
	protected Barrier barrier = null;
	protected ShieldHolder shields = null;
	protected Damageable entity = null;
	protected LinkedList<TickAction> tickActions = new LinkedList<TickAction>(); // Every 20 ticks
	protected MapSpawnerInstance spawner;

	// Buffs
	protected HashMap<BuffType, Buff> damageBuffs = new HashMap<BuffType, Buff>();
	protected HashMap<BuffType, Buff> defenseBuffs = new HashMap<BuffType, Buff>();

	public void cleanup() {
		for (BukkitTask task : tasks.values()) {
			task.cancel();
		}
	}

	public FightData(Damageable p, FightInstance inst) {
		// Only use this for players
		this.inst = inst;
		this.entity = p;
		this.shields = new ShieldHolder(this);
	}

	public FightData(Damageable e, MapSpawnerInstance spawner) {
		// Only use this for mobs
		Plot p = Plot.locationToPlot(e.getLocation());
		Session s = SessionManager.getSession(p);
		if (s == null || !(s.getInstance() instanceof FightInstance)) return;
		inst = (FightInstance) s.getInstance();
		this.entity = e;
		this.shields = new ShieldHolder(this);
		this.spawner = spawner;
	}

	public FightInstance getInstance() {
		return inst;
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
	
	public MapSpawnerInstance getSpawner() {
		return spawner;
	}
}
