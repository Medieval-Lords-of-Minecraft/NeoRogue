package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neocore.bukkit.particles.ParticleAnimation;
import me.neoblade298.neocore.bukkit.particles.ParticleAnimation.ParticleAnimationInstance;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.MapSpawnerInstance;

public class FightData {
	protected FightInstance inst;
	protected HashMap<String, Status> statuses = new HashMap<String, Status>();

	protected HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();
	protected HashMap<String, Runnable> cleanupTasks = new HashMap<String, Runnable>();
	
	protected Barrier barrier = null;
	protected ShieldHolder shields = null;
	protected Damageable entity = null;
	protected LinkedList<TickAction> tickActions = new LinkedList<TickAction>(); // Every 20 ticks
	protected MapSpawnerInstance spawner;

	// Buffs
	protected HashMap<BuffType, Buff> damageBuffs = new HashMap<BuffType, Buff>();
	protected HashMap<BuffType, Buff> defenseBuffs = new HashMap<BuffType, Buff>();

	public void cleanup() {
		for (Runnable task : cleanupTasks.values()) {
			task.run();
		}
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

		if (seconds > 0) {
			addTask(id, new BukkitRunnable() {
				public void run() {
					addBuff(applier, damageBuff, multiplier, type, -amount);
					tasks.remove(id);
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
	
	public void runAnimation(String id, ParticleAnimation anim, Location loc) {
		ParticleAnimationInstance inst = anim.run(loc);
		cleanupTasks.put(id + "-anim", new BukkitRunnable() {
			public void run() {
				inst.cancel();
			}
		});
	}
	
	public void runAnimation(String id, ParticleAnimation anim, Entity ent) {
		ParticleAnimationInstance inst = anim.run(ent);
		cleanupTasks.put(id + "-anim", new BukkitRunnable() {
			public void run() {
				inst.cancel();
			}
		});
	}
	
	public void addCleanupTask(String id, Runnable runnable) {
		cleanupTasks.put(id, runnable);
	}
	
	public void addGuaranteedTask(UUID uuid, Runnable runnable, long delay) {
		String id = uuid.toString();
		tasks.put(id, new BukkitRunnable() {
			public void run() {
				runnable.run();
				tasks.remove(id);
				cleanupTasks.remove(id);
			}
		}.runTaskLater(NeoRogue.inst(), delay));
		
		cleanupTasks.put(id, runnable);
	}

	public void removeTask(String id) {
		tasks.remove(id);
	}
	
	public void removeCleanupTask(String id) {
		cleanupTasks.remove(id);
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
	
	public void addShield(UUID applier, double amt, boolean decayPercent, double decayDelay, double decayAmount, double decayPeriod, int decayRepetitions) {
		shields.addShield(new Shield(this, applier, amt, decayPercent, decayDelay, decayAmount, decayPeriod, decayRepetitions));
	}
	
	public void addTickAction(TickAction action) {
		FightInstance.addToTickList(entity.getUniqueId());
		tickActions.add(action);
	}
	
	public TickResult runTickActions() {
		Iterator<TickAction> iter = tickActions.iterator();
		while (iter.hasNext()) {
			TickResult tr = iter.next().run();
			if (tr == TickResult.REMOVE) iter.remove();
		}
		return tickActions.isEmpty() ? TickResult.REMOVE : TickResult.KEEP;
	}
	
	public void removeStatus(String id) {
		Status s = statuses.remove(id);
		if (s != null) s.cleanup();
	}
	
	public boolean hasStatus(String id) {
		return statuses.containsKey(id);
	}
	
	public boolean hasStatus(StatusType type) {
		return statuses.containsKey(type.name());
	}
	
	public Status getStatus(String id) {
		return statuses.get(id);
	}
	
	public Status getStatus(StatusType type) {
		return statuses.get(type.name());
	}
	
	public void applyStatus(StatusType type, UUID applier, int stacks, int seconds) {
		if (FightInstance.getUserData().containsKey(applier)) {
			PlayerFightData data = FightInstance.getUserData(applier);
			FightInstance.trigger(data.getPlayer(), Trigger.APPLY_STATUS, new Object[] { this, type.name(), stacks, seconds });
		}
		Status s = statuses.getOrDefault(type.name(), Status.createByType(type, applier, this));
		s.apply(applier, stacks, seconds);
		statuses.put(type.name(), s);
	}
	
	public void applyStatus(GenericStatusType type, String id, UUID applier, int stacks, int seconds) {
		if (FightInstance.getUserData().containsKey(applier)) {
			PlayerFightData data = FightInstance.getUserData(applier);
			FightInstance.trigger(data.getPlayer(), Trigger.APPLY_STATUS, new Object[] { this, id, stacks, seconds });
		}
		Status s = statuses.getOrDefault(type.name(), Status.createByGenericType(type, id, applier, this));
		s.apply(applier, stacks, seconds);
		statuses.put(id, s);
	}
	
	public void applyStatus(String id, UUID applier, int stacks) {
		if (FightInstance.getUserData().containsKey(applier)) {
			PlayerFightData data = FightInstance.getUserData(applier);
			FightInstance.trigger(data.getPlayer(), Trigger.APPLY_STATUS, new Object[] { this, id, stacks, -1 });
		}
		Status s = Status.createByGenericType(GenericStatusType.BASIC, id, applier, this);
		s.apply(applier, stacks, -1);
		statuses.put(id, s);
	}
	
	public MapSpawnerInstance getSpawner() {
		return spawner;
	}
}
