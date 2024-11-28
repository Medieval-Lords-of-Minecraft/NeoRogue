package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation.ParticleAnimationInstance;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class FightData {
	protected FightInstance inst;
	protected String mobDisplay;
	protected ActiveMob am;
	protected Mob mob;
	protected double knockbackMult;
	protected UUID uuid;
	protected HashMap<String, Status> statuses = new HashMap<String, Status>();
	protected ArrayList<Entity> holograms = new ArrayList<Entity>();

	protected HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();
	protected HashMap<String, Runnable> cleanupTasks = new HashMap<String, Runnable>();
	
	protected Barrier barrier = null;
	protected ShieldHolder shields; // Never null
	protected LivingEntity entity = null;
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
		for (TickAction tickAction : tickActions) {
			tickAction.setCancelled(true);
		}
		for (Entity ent : holograms) {
			new BukkitRunnable() {
				public void run() {
					ent.remove();
				}
			}.runTask(NeoRogue.inst());
		}
	}
	
	public FightData() {
		// Empty for null entities
	}

	public FightData(LivingEntity p, FightInstance inst) {
		// Only use this for players
		this.inst = inst;
		this.entity = p;
		this.shields = new ShieldHolder(this);
		this.uuid = p.getUniqueId();
	}

	public FightData(LivingEntity e, ActiveMob am, Mob mob, MapSpawnerInstance spawner) {
		// Only use this for mobs
		if (e == null) return; // Sometimes gets called when a dead mob's poison ticks
		Plot p = Plot.locationToPlot(e.getLocation());
		Session s = SessionManager.getSession(p);
		if (s == null || !(s.getInstance() instanceof FightInstance)) return;
		inst = (FightInstance) s.getInstance();
		this.entity = e;
		this.shields = new ShieldHolder(this);
		this.spawner = spawner;
		this.uuid = e.getUniqueId();
		this.knockbackMult = mob != null ? mob.getKnockbackMultiplier() : 1;
		if (am != null && am.getType().getDisplayName() != null && am.getType().getDisplayName().isPresent()) this.mobDisplay = am.getType().getDisplayName().get();
		this.am = am;

		// Set up base mob resistances
		if (mob != null) {
			for (Entry<BuffType, Integer> ent : mob.getResistances().entrySet()) {
				addBuff(this, false, true, ent.getKey(), (double) ent.getValue() / 100);
			}
		}
	}
	
	public UUID getUniqueId() {
		return uuid;
	}

	public FightInstance getInstance() {
		return inst;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
	
	public void setKnockbackMultiplier(double amt) {
		this.knockbackMult = amt;
	}
	
	public Mob getMob() {
		return this.mob;
	}

	public void addBuff(FightData applier, boolean damageBuff, boolean multiplier, BuffType type, double amount) {
		addBuff(applier, damageBuff, multiplier, type, amount, null);
	}

	public void addBuff(FightData applier, boolean damageBuff, boolean multiplier, BuffType type, double amount, DamageOrigin origin) {
		Buff b = damageBuff ? damageBuffs.getOrDefault(type, new Buff()) : defenseBuffs.getOrDefault(type, new Buff());
		if (multiplier)
			b.addMultiplier(applier, amount);
		else
			b.addIncrease(applier, amount);
		b.setOrigin(origin);
		if (damageBuff) damageBuffs.put(type, b);
		else defenseBuffs.put(type, b);
	}

	public void addBuff(FightData applier, String id, boolean damageBuff, boolean multiplier, BuffType type, double amount, int ticks) {
		addBuff(applier, id, damageBuff, multiplier, type, amount, null, ticks);
	}

	public void addBuff(FightData applier, String id, boolean damageBuff, boolean multiplier, BuffType type, double amount, DamageOrigin origin, int ticks) {
		addBuff(applier, damageBuff, multiplier, type, amount, origin);

		if (ticks > 0) {
			addTask(id, new BukkitRunnable() {
				public void run() {
					addBuff(applier, damageBuff, multiplier, type, -amount);
					tasks.remove(id);
				}
			}.runTaskLater(NeoRogue.inst(), ticks));
		}
	}

	public Buff getBuff(boolean damageBuff, BuffType type) {
		return damageBuff ? damageBuffs.get(type) : defenseBuffs.get(type);
	}
	
	public HashMap<BuffType, Buff> getBuffs(boolean damageBuff) {
		return damageBuff ? damageBuffs : defenseBuffs;
	}

	public void addTask(BukkitTask task) {
		tasks.put(UUID.randomUUID().toString(), task);
	}

	public void addTask(String id, BukkitTask task) {
		while (tasks.containsKey(id)) id += "1";
		tasks.put(id, task);
	}
	
	public void updateDisplayName() {
		if (am == null || entity == null) return;
		
		if (entity.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			am.setShowCustomNameplate(false);
			return;
		}
		
		am.setShowCustomNameplate(true);
		double healthPct = entity.getHealth() / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		String healthColor;
		if (healthPct < 0.33) {
			healthColor = "&c";
		}
		else if (healthPct < 0.67) {
			healthColor = "&e";
		}
		else {
			healthColor = "&a";
		}
		
		String bottomLine = healthColor + (int) Math.ceil(entity.getHealth()) + "&f/" + healthColor + (int) entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		bottomLine += " " + mobDisplay;
		
		ArrayList<Status> list = new ArrayList<Status>(statuses.values());
		Collections.sort(list, Status.comp);
		String statuses = "";
		int displaySize = 0;
		for (int i = 0; i < list.size() && displaySize < 5; i++) {
			Status s = list.get(i);
			if (s.isHidden() || s.getStacks() <= 0) {
				continue;
			}
			statuses += list.get(i).getDisplay() + "\\n";
			displaySize++;
		}
		am.setDisplayName(list.isEmpty() ? bottomLine : statuses + bottomLine);
	}
	
	public void runAnimation(String id, Player origin, ParticleAnimation anim, Location loc) {
		ParticleAnimationInstance inst = anim.play(origin, loc);
		cleanupTasks.put(id + "-anim", new BukkitRunnable() {
			public void run() {
				inst.cancel();
			}
		});
	}
	
	public void runAnimation(String id, Player origin, ParticleAnimation anim, Entity ent) {
		ParticleAnimationInstance inst = anim.play(origin, ent);
		cleanupTasks.put(id + "-anim", new BukkitRunnable() {
			public void run() {
				inst.cancel();
			}
		});
	}
	
	public void addHealth(double amount) {
		double curr = entity.getHealth();
		double after = Math.min(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), curr + amount);
		entity.setHealth(after);
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
	
	// No decay
	public Shield addSimpleShield(UUID applier, double amt, long decayDelayTicks, boolean isSecondary) {
		return addShield(applier, amt, true, decayDelayTicks, 100, 0, 1, isSecondary);
	}
	// No decay
	public Shield addSimpleShield(UUID applier, double amt, long decayDelayTicks) {
		return addShield(applier, amt, true, decayDelayTicks, 100, 0, 1, false);
	}
	
	public Shield addPermanentShield(UUID applier, double amt, boolean isSecondary) {
		return addShield(applier, amt, true, 0, 0, 0, 0, isSecondary);
	}
	
	public Shield addPermanentShield(UUID applier, double amt) {
		return addShield(applier, amt, true, 0, 0, 0, 0, false);
	}
	
	public Shield addShield(UUID applier, double amt, boolean decayPercent, long decayDelayTicks, double decayAmount, long decayPeriodTicks, int decayRepetitions, boolean isSecondary) {
		PlayerFightData applierData = FightInstance.getUserData(applier);
		Shield shield = new Shield(applier, amt, decayPercent, decayDelayTicks, decayAmount, decayPeriodTicks, decayRepetitions);
		GrantShieldsEvent ev = new GrantShieldsEvent(applierData, this, shield, isSecondary);
		if (applierData != null) {
			FightInstance.trigger(applierData.getPlayer(), Trigger.GRANT_SHIELDS, ev);
		}
		if (this instanceof PlayerFightData) {
			FightInstance.trigger(((PlayerFightData) this).getPlayer(), Trigger.RECEIVE_SHIELDS, ev);
		}
		shield.applyBuff(ev.getBuff());
		shields.addShield(shield);
		if (shield.getTask() != null) tasks.put(UUID.randomUUID().toString(), shield.getTask());
		return shield;
	}
	
	public void addTickAction(TickAction action) {
		if (entity == null) return;
		FightInstance.addToTickList(entity.getUniqueId());
		tickActions.add(action);
	}
	
	public TickResult runTickActions() {
		Iterator<TickAction> iter = tickActions.iterator();
		while (iter.hasNext()) {
			TickAction ta = iter.next();
			if (ta.isCancelled()) {
				iter.remove();
				continue;
			}
			TickResult tr = ta.run();
			if (tr == TickResult.REMOVE) iter.remove();
		}
		return tickActions.isEmpty() ? TickResult.REMOVE : TickResult.KEEP;
	}
	
	public void removeStatus(StatusType type) {
		Status s = statuses.remove(type.name());
		if (s != null) s.cleanup();
	}
	
	public void removeStatus(String id) {
		Status s = statuses.remove(id);
		if (s != null) s.cleanup();
	}
	
	public boolean hasStatus(String id) {
		return statuses.containsKey(id) && statuses.get(id).getStacks() > 0;
	}
	
	public boolean hasStatus(StatusType type) {
		return statuses.containsKey(type.name()) && statuses.get(type.name()).getStacks() > 0;
	}
	
	public Status getStatus(String id) {
		return statuses.getOrDefault(id, Status.EMPTY);
	}
	
	public Status getStatus(StatusType type) {
		return statuses.getOrDefault(type.name(), Status.EMPTY);
	}
	
	public void applyStatus(StatusType type, FightData applier, int stacks, int ticks) {
		applyStatus(Status.createByType(type, this), applier, stacks, ticks, null, false);
	}
	
	public void applyStatus(StatusType type, FightData applier, int stacks, int ticks, DamageMeta meta, boolean isSecondary) {
		applyStatus(Status.createByType(type, this), applier, stacks, ticks, meta, isSecondary);
	}
	
	public void applyStatus(Status status, FightData applier, int stacks, int ticks) {
		applyStatus(status, applier, stacks, ticks, null, false);
	}
	
	public void applyStatus(Status status, FightData applier, int stacks, int ticks, DamageMeta meta, boolean isSecondary) {
		if (!entity.isValid()) return;
		String id = status.getId();
		status = statuses.getOrDefault(id, status); // If status exists, use that, otherwise add the new one

		PreApplyStatusEvent ev = new PreApplyStatusEvent(this, status, stacks, ticks, isSecondary, meta);
		if (this instanceof PlayerFightData) {
			PlayerFightData data = (PlayerFightData) this;
			data.updateBoardLines();
			FightInstance.trigger(data.getPlayer(), Trigger.PRE_RECEIVE_STATUS, ev);
		}
		if (applier instanceof PlayerFightData) {
			FightInstance.trigger(((PlayerFightData) applier).getPlayer(), Trigger.PRE_APPLY_STATUS, ev);
		}
		int finalStacks = (int) Math.ceil(ev.getStacksBuff().apply(stacks));
		int finalDuration = (int) Math.ceil(ev.getDurationBuff().apply(ticks));
		status.apply(applier, finalStacks, finalDuration);
		ApplyStatusEvent ev2 = new ApplyStatusEvent(this, status, finalStacks, finalDuration, isSecondary, meta);
		if (applier instanceof PlayerFightData) {
			FightInstance.trigger(((PlayerFightData) applier).getPlayer(), Trigger.APPLY_STATUS, ev2);
			try {
				((PlayerFightData) applier).getStats().addStatusApplied(StatusType.valueOf(id), finalStacks);
			}
			catch (IllegalArgumentException ex) {
			}
		}
		
		if (statuses.isEmpty()) {
			addTickAction(new StatusUpdateTickAction());
		}
		statuses.put(id, status);
		if (am != null) {
			updateDisplayName();
		}
	}

	public double getKnockbackMultiplier() {
		return knockbackMult;
	}

	private class StatusUpdateTickAction extends TickAction {
		@Override
		public TickResult run() {
			if (!entity.isValid()) return TickResult.REMOVE;
			if (areStatusesEmpty()) {
				updateDisplayName();
				statuses.clear();
				return TickResult.REMOVE;
			}
			updateDisplayName();
			return TickResult.KEEP;
		}
	}
	
	private boolean areStatusesEmpty() {
		for (Status s : statuses.values()) {
			if (s.getStacks() > 0) return false;
			
			statuses.remove(s.getId());
		}
		return true;
	}
	
	public MapSpawnerInstance getSpawner() {
		return spawner;
	}
}
