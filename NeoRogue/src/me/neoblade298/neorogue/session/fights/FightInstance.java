package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Status;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class FightInstance implements Instance {
	private static HashMap<UUID, PlayerFightData> userData = new HashMap<UUID, PlayerFightData>();
	private static HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	private static HashMap<UUID, BukkitTask> blockTasks = new HashMap<UUID, BukkitTask>();
	private static HashSet<UUID> toTick = new HashSet<UUID>();
	
	private Map map;
	private MapSpawnerInstance[] spawners;
	private Session s;
	private HashMap<String, Barrier> enemyBarriers = new HashMap<String, Barrier>();
	
	static {
		new BukkitRunnable() {
			boolean alternate = false;
			public void run() {
				alternate = !alternate;
				
				// Every 20 ticks
				if (alternate && !toTick.isEmpty()) {
					Iterator<UUID> iter = toTick.iterator();
					while (iter.hasNext()) {
						if (fightData.get(iter.next()).runTickActions()) iter.remove();
					}
				}
				
				// Every 10 ticks
				for (Session s : SessionManager.getSessions()) {
					if (!(s.getInstance() instanceof FightInstance)) continue;
					for (Barrier b : ((FightInstance) s.getInstance()).getBarriers().values()) {
						b.tick();
					}
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L);
	}
	
	public FightInstance(Session s) {
		this.s = s;
		map = Map.generate(s.getArea().getType(), 2);
	}
	
	public void instantiate() {
		map.instantiate(this, s.getXOff(), s.getZOff());
	}
	
	// This will only ever handle basic left click
	public static void handleDamage(EntityDamageByEntityEvent e, boolean playerDamager) {
		Player p = playerDamager ? (Player) e.getDamager() : (Player) e.getEntity();
		e.setCancelled(true);
		if (p.getAttackCooldown() < 0.9F) return;
		
		if (playerDamager) {
			trigger(p, Trigger.LEFT_CLICK_HIT, new Object[] {p, e.getEntity()});
		}
		else {
			trigger(p, Trigger.RECEIVED_DAMAGE, new Object[] {p, e.getDamager()});
		}
	}
	
	public static void handleHotbarSwap(PlayerItemHeldEvent e) {
		// Only cancel swap if something is bound to the trigger
		e.setCancelled(trigger(e.getPlayer(), Trigger.getFromHotbarSlot(e.getNewSlot()), null));
	}
	
	public static void handleOffhandSwap(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p, p.isSneaking() ? Trigger.SHIFT_SWAP : Trigger.SWAP, null);
	}
	
	public static void handleDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p, p.isSneaking() ? Trigger.SHIFT_DROP : Trigger.DROP, null);
	}
	
	public static void handleRightClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		// Look for non-offhand triggers
		if (e.getHand() == EquipmentSlot.OFF_HAND) {
			trigger(p, Trigger.RAISE_SHIELD, null);
			trigger(p, Trigger.RIGHT_CLICK, null);
			
			if (blockTasks.containsKey(uuid)) {
				blockTasks.get(uuid).cancel();
			}
			
			blockTasks.put(uuid, new BukkitRunnable() {
				public void run() {
					if (p == null || !p.isBlocking()) {
						this.cancel();
						trigger(p, Trigger.LOWER_SHIELD, null);
						blockTasks.remove(uuid);
					}
					else {
						trigger(p, Trigger.SHIELD_TICK, null);
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 10L, 10L));
		}
		else {
			double y = p.getEyeLocation().getDirection().normalize().getY();
			if (y > 0.9) {
				trigger(p, Trigger.UP_RCLICK, null);
			}
			else if (y < 0.1) {
				trigger(p, Trigger.DOWN_RCLICK, null);
			}
			else if (p.isSneaking()) {
				trigger(p, Trigger.SHIFT_RCLICK, null);
			}
		}
	}
	
	public static void handleLeftClick(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		if (e.getPlayer().getAttackCooldown() < 0.9F) return;
		
		trigger(e.getPlayer(), Trigger.LEFT_CLICK_NO_HIT, null);
	}
	
	public static void handleMythicDespawn(MythicMobDespawnEvent e) {
		removeFightData(e.getEntity().getUniqueId());
	}
	
	public static void handleMythicDeath(MythicMobDeathEvent e) {
		removeFightData(e.getEntity().getUniqueId());
	}
	
	// Returns true if the event should be cancelled (basically only on hotbar swap)
	private static boolean trigger(Player p, Trigger trigger, Object[] obj) {
		PlayerFightData data = userData.get(p.getUniqueId());
		if (trigger.isSlotDependent()) {
			data.runSlotBasedTriggers(trigger, p.getInventory().getHeldItemSlot(), obj);
		}
		return data.runActions(trigger, obj);
	}
	
	public static FightData getFightData(UUID uuid) {
		if (!fightData.containsKey(uuid)) {
			FightData fd = new FightData((Damageable) Bukkit.getEntity(uuid));
			fightData.put(uuid, fd);
		}
		return fightData.get(uuid);
	}
	
	public static HashMap<UUID, PlayerFightData> getUserData() {
		return userData;
	}
	
	public static PlayerFightData getUserData(UUID uuid) {
		return userData.get(uuid);
	}
	
	public static void giveHeal(Damageable caster, double amount, Damageable... targets) {
		for (Damageable target : targets) {
			if (!(target instanceof Attributable)) continue;
			double toSet = Math.min(caster.getHealth() + amount, ((Attributable) caster).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			caster.setHealth(toSet);
		}
	}
	
	public static void dealDamage(Damageable damager, DamageType type, double amount, Damageable... targets) {
		dealDamage(damager, new DamageMeta(amount, type), targets);
	}
	
	public static void dealDamage(Damageable damager, DamageMeta meta, Damageable... targets) {
		UUID uuid = damager.getUniqueId();
		FightData data = getFightData(uuid);
		double amount = meta.getDamage();
		DamageType type = meta.getType();
		double original = amount;
		double multiplier = 1;
		for (BuffType buffType : type.getBuffTypes()) {
			Buff b = data.getBuff(true, buffType);
			if (b == null) continue;
			
			amount += b.getIncrease();
			multiplier += b.getMultiplier();
			for (Entry<UUID, BuffSlice> ent : b.getSlices().entrySet()) {
				BuffSlice slice = ent.getValue();
				userData.get(ent.getKey()).getStats().addDefenseBuffed(slice.getIncrease() + (slice.getMultiplier() * original));
			}
		}
		
		if (data.hasStatus("FROST") && type.containsBuffType(BuffType.PHYSICAL)) {
			Status status = data.getStatus("FROST");
			int stacks = status.getStacks();
			for (Entry<UUID, Integer> ent : status.getSlices().getSliceOwners().entrySet()) {
				userData.get(ent.getKey()).getStats().addDamageMitigated(amount * Math.max(100, ent.getValue()) * 0.01);
			}
			amount *= 1 - (stacks * 0.01);
			status.apply(uuid, (int) (-stacks * 0.1), 0);
		}
		amount *= multiplier;
		
		if (amount <= 0) amount = 0.1;
		if (data instanceof PlayerFightData) {
			((PlayerFightData) data).getStats().addDamageDealt(type, amount * targets.length);
		}

		for (Damageable target : targets) {
			receiveDamage(damager, meta, target);
		}
	}
	
	public static void receiveDamage(Damageable damager, DamageMeta meta, Damageable target) {
		UUID uuid = target.getUniqueId();
		FightData data = getFightData(uuid);
		double amount = meta.getDamage();
		double original = amount;
		DamageType type = meta.getType();
		
		// Reduce damage from barriers
		if (meta.hitBarrier() && data.getBarrier() != null) {
			if (data instanceof PlayerFightData) {
				((PlayerFightData) data).getStats().addDamageBarriered(amount);
			}
			amount = data.getBarrier().applyDefenseBuffs(amount, type);
		}
		
		// Status effects
		if (!meta.isSecondary()) {
			if (data.hasStatus("BURN")) {
				dealDamage(damager, DamageType.FIRE, amount * 0.1, target);
			}
			
			if (data.hasStatus("ELECTRIFIED")) {
				for (Entity e : target.getNearbyEntities(5, 5, 5)) {
					if (e == target) continue;
					if (e instanceof Player) continue;
					if (!(e instanceof Damageable)) continue;
					Damageable dmg = (Damageable) e;
					int stacks = data.getStatus("ELECTRIFIED").getStacks();
					dealDamage(damager, DamageType.LIGHTNING, amount * stacks * 0.1, dmg);
				}
			}
			
			if (data.hasStatus("CONCUSSED") && type.containsBuffType(BuffType.PHYSICAL)) {
				int stacks = data.getStatus("CONCUSSED").getStacks();
				dealDamage(damager, DamageType.EARTH, amount * stacks * 0.1, target);
			}
			
			if (data.hasStatus("INSANITY") && type.containsBuffType(BuffType.MAGICAL)) {
				int stacks = data.getStatus("CONCUSSED").getStacks();
				dealDamage(damager, DamageType.DARK, amount * stacks * 0.1, target);
			}
			
			if (data.hasStatus("SANCTIFIED")) {
				int stacks = data.getStatus("SANCTIFIED").getStacks();
				giveHeal(damager, amount * stacks * 0.1, target);
			}
		}

		// Calculate damage to shields
		if (!data.getShields().isEmpty() && !meta.bypassShields()) {
			ShieldHolder shields = data.getShields();
			amount = Math.max(0, shields.useShields(amount));
			new BukkitRunnable() {
				public void run() {
					shields.update();
				}
			}.runTask(NeoRogue.inst());
			
			if (amount <= 0) {
				// Make sure player never dies from chip damage from shields
				if (target.getHealth() < 10) target.setHealth(target.getHealth() + 0.1);
				target.damage(0.1);
				return;
			}
		}
		
		if (meta.bypassShields()) {
			amount += target.getAbsorptionAmount();
			new BukkitRunnable() {
				public void run() {
					data.getShields().update();
				}
			}.runTask(NeoRogue.inst());
		}
		
		// Finally calculate hp damage
		double multiplier = 1;
		for (BuffType buffType : type.getBuffTypes()) {
			Buff b = data.getBuff(false, buffType);
			if (b == null) continue; 
			
			amount -= b.getIncrease();
			multiplier -= b.getMultiplier();
			for (Entry<UUID, BuffSlice> ent : b.getSlices().entrySet()) {
				BuffSlice slice = ent.getValue();
				userData.get(ent.getKey()).getStats().addDefenseBuffed(slice.getIncrease() + (slice.getMultiplier() * original));
			}
		}
		amount *= multiplier;
		target.damage(amount);
	}

	@Override
	public void start(Session s) {
		s.setInstance(this);

		for (Player p : s.getOnlinePlayers()) {
			setup(p, s.getData(p.getUniqueId()));
		}
	}
	
	public static void putFightData(UUID uuid, FightData data) {
		fightData.put(uuid, data);
	}
	
	private void setup(Player p, PlayerSessionData data) {
		UUID uuid = p.getUniqueId();
		PlayerFightData fd = new PlayerFightData(this, data);
		fightData.put(uuid, fd);
		userData.put(uuid, fd);
	}
	
	public void cleanup() {
		for (UUID uuid : s.getParty().keySet()) {
			userData.remove(uuid).cleanup();
			fightData.remove(uuid).cleanup();
		}
	}
	
	public static void removeFightData(UUID uuid) {
		toTick.remove(uuid);
		fightData.remove(uuid);
	}
	
	public static void addToTickList(UUID uuid) {
		toTick.add(uuid);
	}
	
	public void addBarrier(FightData owner, String id, Barrier b, int duration) {
		if (id == null) {
			id = UUID.randomUUID().toString().substring(0, 8);
		}
		enemyBarriers.put(id, b);
		final String fid = id;
		
		owner.addTask(id, new BukkitRunnable() {
			public void run() {
				enemyBarriers.remove(fid);
				owner.removeTask(fid);
			}
		}.runTaskLater(NeoRogue.inst(), duration));
	}
	
	public void removeBarrier(FightData owner, String id) {
		enemyBarriers.remove(id);
		owner.removeTask(id);
	}
	
	public HashMap<String, Barrier> getBarriers() {
		return enemyBarriers;
	}
}
