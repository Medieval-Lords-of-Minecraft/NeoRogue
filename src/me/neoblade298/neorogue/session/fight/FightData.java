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
import me.libraryaddict.disguise.DisguiseAPI;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation.ParticleAnimationInstance;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.session.Plot;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.MobAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
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
	private HashMap<Trigger, ArrayList<MobAction>> triggers = new HashMap<Trigger, ArrayList<MobAction>>();

	protected HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();
	protected HashMap<String, Runnable> cleanupTasks = new HashMap<String, Runnable>();
	
	protected HashMap<UUID, Barrier> barriers = new HashMap<UUID, Barrier>();
	protected ShieldHolder shields; // Never null
	protected LivingEntity entity = null;
	protected LinkedList<TickAction> tickActions = new LinkedList<TickAction>(); // Every 20 ticks
	protected MapSpawnerInstance spawner;
	protected HashMap<DamageBuffType, BuffList> damageBuffs = new HashMap<DamageBuffType, BuffList>(), defenseBuffs = new HashMap<DamageBuffType, BuffList>();
	
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

	@FunctionalInterface
	public interface FightDataConstructor {
		FightData create(LivingEntity e, ActiveMob am, Mob mob, MapSpawnerInstance spawner);
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
			for (Entry<DamageCategory, Integer> ent : mob.getResistances().entrySet()) {
				addDefenseBuff(DamageBuffType.of(ent.getKey()), new Buff(this, 0, (double) ent.getValue() / 100, BuffStatTracker.ignored("MythicMobs")));
			}
		}

		// This enables the nameplate + hp to show properly
		if (DisguiseAPI.isDisguised(entity)) {
			DisguiseAPI.getDisguise(entity).setDynamicName(true);
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

	public void addDamageBuff(DamageBuffType type, Buff b) {
		addBuff(damageBuffs, type, b);
	}

	public void addDefenseBuff(DamageBuffType type, Buff b) {
		addBuff(defenseBuffs, type, b);
	}

	public void addDamageBuff(DamageBuffType type, Buff b, int ticks) {
		addBuff(damageBuffs, type, b, ticks);
	}

	public void addDefenseBuff(DamageBuffType type, Buff b, int ticks) {
		addBuff(defenseBuffs, type, b, ticks);
	}

	private void addBuff(HashMap<DamageBuffType, BuffList> lists, DamageBuffType type, Buff b) {
		BuffList list = lists.getOrDefault(type, new BuffList());
		list.add(b);
		lists.put(type, list);
	}

	private void addBuff(HashMap<DamageBuffType, BuffList> lists, DamageBuffType type, Buff b, int ticks) {
		Buff inv = b.invert(); // Should be done immediately, since buffs combine and inverting it after it
								// combines will over-remove
		if (ticks > 0) {
			String id = UUID.randomUUID().toString();
			BukkitTask removeTask = new BukkitRunnable() {
				public void run() {
					BuffStatTracker tracker = b.getStatTracker();
					if (tracker.shouldCombine()) {
						addBuff(lists, type, inv);
					}
					else {
						addBuff(lists, type, Buff.empty(b.getApplier(), b.getStatTracker()));
					}
					b.getApplier().tasks.remove(id);
				}
			}.runTaskLater(NeoRogue.inst(), ticks);
			b.setRemoveTask(id);
			b.getApplier().addTask(id, removeTask);
		}

		// Adding buff must be done after the remove task is set
		addBuff(lists, type, b);
	}

	public BuffList getDamageBuffList(DamageBuffType type) {
		return damageBuffs.get(type);
	}

	public BuffList getDefenseBuffList(DamageBuffType type) {
		return defenseBuffs.get(type);
	}

	public HashMap<DamageBuffType, BuffList> getDamageBuffLists() {
		return damageBuffs;
	}

	public HashMap<DamageBuffType, BuffList> getDefenseBuffLists() {
		return defenseBuffs;
	}

	public void addTask(BukkitTask task) {
		tasks.put(UUID.randomUUID().toString(), task);
	}

	public void addTask(String id, BukkitTask task) {
		if (tasks.containsKey(id)) tasks.get(id).cancel();
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

	public void removeAndCancelTask(String id) {
		BukkitTask task = tasks.remove(id);
		if (task != null) task.cancel();
	}
	
	public void removeCleanupTask(String id) {
		cleanupTasks.remove(id);
	}
	
	public UUID addBarrier(Barrier barrier) {
		barriers.put(barrier.getUniqueId(), barrier);
		return barrier.getUniqueId();
	}

	public void removeBarrier(UUID uuid) {
		barriers.remove(uuid);
	}

	public void removeBarrier(Barrier barrier) {
		barriers.remove(barrier.getUniqueId());
	}
	
	public Barrier getBarrier(UUID uuid) {
		return barriers.get(uuid);
	}

	public HashMap<UUID, Barrier> getBarriers() {
		return barriers;
	}
	
	public ShieldHolder getShields() {
		return shields;
	}
	
	// No decay
	public Shield addSimpleShield(UUID applier, double amt, int decayDelayTicks, boolean isSecondary) {
		return addShield(applier, amt, true, decayDelayTicks, 100, 0, 1, isSecondary);
	}
	// No decay
	public Shield addSimpleShield(UUID applier, double amt, int decayDelayTicks) {
		return addShield(applier, amt, true, decayDelayTicks, 100, 0, 1, false);
	}
	
	public Shield addPermanentShield(UUID applier, double amt, boolean isSecondary) {
		return addShield(applier, amt, true, 0, 0, 0, 0, isSecondary);
	}
	
	public Shield addPermanentShield(UUID applier, double amt) {
		return addShield(applier, amt, true, 0, 0, 0, 0, false);
	}
	
	public Shield addShield(UUID applier, double amt, boolean decayPercent, int decayDelayTicks, double decayAmount, 
			int decayPeriodTicks, int decayRepetitions, boolean isSecondary) {
		PlayerFightData applierData = FightInstance.getUserData(applier);
		Shield shield = new Shield(applier, amt, decayPercent, decayDelayTicks, decayAmount, decayPeriodTicks, decayRepetitions);
		GrantShieldsEvent ev = new GrantShieldsEvent(applierData, this, shield, isSecondary);
		if (applierData != null) {
			FightInstance.trigger(applierData.getPlayer(), Trigger.GRANT_SHIELDS, ev);
		}
		if (this instanceof PlayerFightData) {
			FightInstance.trigger(((PlayerFightData) this).getPlayer(), Trigger.RECEIVE_SHIELDS, ev);
		}
		shield.applyBuffs(ev.getAmountBuff(), ev.getDurationBuff());
		shields.addShield(shield);
		if (shield.getTask() != null) tasks.put(UUID.randomUUID().toString(), shield.getTask());
		return shield;
	}
	
	public void addTickAction(TickAction action) {
		if (entity == null) return;
		inst.addToTickList(entity.getUniqueId());
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
		else {
			runMobActions(this, Trigger.PRE_RECEIVE_STATUS, ev);
		}

		if (applier instanceof PlayerFightData) {
			FightInstance.trigger(((PlayerFightData) applier).getPlayer(), Trigger.PRE_APPLY_STATUS, ev);
		}
		int finalStacks = (int) Math.ceil(ev.getStacksBuffList().apply(stacks));
		int finalDuration = (int) Math.ceil(ev.getDurationBuffList().apply(ticks));
		status.apply(applier, finalStacks, finalDuration);
		ApplyStatusEvent ev2 = new ApplyStatusEvent(this, status, finalStacks, finalDuration, isSecondary, meta);
		if (applier instanceof PlayerFightData) {
			// Stat tracking
			for (Buff b : ev.getStacksBuffList().getBuffs()) {
				((PlayerFightData) applier).getStats().addBuffStat(b.getStatTracker(), b.getEffectiveChange(stacks));
			}
			for (Buff b : ev.getDurationBuffList().getBuffs()) {
				((PlayerFightData) applier).getStats().addBuffStat(b.getStatTracker(), b.getEffectiveChange(ticks));
			}


			FightInstance.trigger(((PlayerFightData) applier).getPlayer(), Trigger.APPLY_STATUS, ev2);
			try {
				StatusType type = StatusType.valueOf(id);
				if (!type.isHidden()) ((PlayerFightData) applier).getStats().addStatusApplied(type, finalStacks);
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

	// Exclusively used for mobs like Bandit King that have custom triggers
	public void runMobActions(FightData data, Trigger trigger, Object inputs) {
		if (triggers.containsKey(trigger)) {
			Iterator<MobAction> iter = triggers.get(trigger).iterator();
			while (iter.hasNext()) {
				MobAction inst = iter.next();
				TriggerResult tr = inst.trigger(data, inputs);
				if (tr == TriggerResult.remove()) iter.remove();
			}
		}
	}

	public void addMobTrigger(Trigger trigger, MobAction action) {
		ArrayList<MobAction> list = triggers.getOrDefault(trigger, new ArrayList<MobAction>());
		list.add(action);
		triggers.put(trigger, list);
	}
}
