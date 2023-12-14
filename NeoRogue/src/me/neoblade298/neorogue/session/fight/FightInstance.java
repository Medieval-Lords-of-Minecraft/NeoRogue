package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.Coordinates;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.LoseInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffSlice;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class FightInstance extends Instance {
	private static HashMap<UUID, PlayerFightData> userData = new HashMap<UUID, PlayerFightData>();
	private static HashMap<UUID, Barrier> userBarriers = new HashMap<UUID, Barrier>();
	private static HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	private static HashMap<UUID, BukkitTask> blockTasks = new HashMap<UUID, BukkitTask>();
	private static HashSet<UUID> toTick = new HashSet<UUID>();
	private static final int KILLS_TO_SCALE = 5; // number of mobs to kill before increasing total mobs by 1
	
	protected HashSet<UUID> party = new HashSet<UUID>();
	protected Map map;
	protected ArrayList<MapSpawnerInstance> spawners = new ArrayList<MapSpawnerInstance>(),
			unlimitedSpawners = new ArrayList<MapSpawnerInstance>();
	protected HashMap<UUID, Barrier> enemyBarriers = new HashMap<UUID, Barrier>();
	protected ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
	protected double spawnCounter; // Holds a value between 0 and 1, when above 1, a mob spawns
	protected double totalSpawnValue; // Keeps track of total mob spawns, to handle scaling of spawning
	
	public FightInstance(Set<UUID> players) {
		party.addAll(players);
	}
	
	public static HashMap<UUID, Barrier> getUserBarriers() {
		return userBarriers;
	}
	
	public void instantiate() {
		map.instantiate(this, s.getXOff(), s.getZOff());
	}
	
	public Map getMap() {
		return map;
	}
	
	public HashSet<UUID> getParty() {
		return party;
	}
	
	public static void handleDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		p.spigot().respawn();
		UUID pu = p.getUniqueId();
		if (!userData.containsKey(pu)) return;
		PlayerFightData data = userData.remove(pu);
		data.getSessionData().setDeath(true);

		// Remove the player's abilities from the fight
		data.cleanup();
		fightData.remove(pu).cleanup();
		PlayerSessionData sdata = data.getSessionData();
		sdata.setDeath(true);
		
		// If that's the last player alive, send them to a post death instance
		for (UUID uuid : data.getInstance().getParty()) {
			PlayerFightData fdata = userData.get(uuid);
			if (fdata.isActive()) return;
		}
		
		// End game as a loss
		data.getInstance().s.setInstance(new LoseInstance());
	}
	
	// This handles basic left click and/or enemy damage
	public static void handleDamage(EntityDamageByEntityEvent e) {
		Player p = (Player) e.getDamager();
		e.setCancelled(true);
		if (p.getAttackCooldown() < 0.9F) return;

		trigger(p, Trigger.LEFT_CLICK, null);
		trigger(p, Trigger.LEFT_CLICK_HIT, new Object[] {p, e.getEntity()});
	}
	
	public static void handleWin() {
		for (PlayerFightData data : userData.values()) {
			trigger(data.getPlayer(), Trigger.WIN_FIGHT, new Object[0]);
		}
	}
	
	public static void handleHotbarSwap(PlayerItemHeldEvent e) {
		// Only cancel swap if something is bound to the trigger
		Player p = e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		e.setCancelled(data.hasTriggerAction(Trigger.getFromHotbarSlot(e.getNewSlot())));
		trigger(e.getPlayer(), Trigger.getFromHotbarSlot(e.getNewSlot()), null);
	}
	
	public static void handleOffhandSwap(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		// Offhand is always cancelled
		trigger(p, p.isSneaking() ? Trigger.SHIFT_SWAP : Trigger.SWAP, null);
	}
	
	public static void handleDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		// Drop item is always cancelled
		trigger(p, p.isSneaking() ? Trigger.SHIFT_DROP : Trigger.DROP, null);
	}
	
	public void handleInteractEvent(PlayerInteractEvent e) {
		Action a = e.getAction();
		if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
			handleLeftClick(e);
		}
		else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			handleRightClick(e);
		}
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

		trigger(e.getPlayer(), Trigger.LEFT_CLICK, null);
		trigger(e.getPlayer(), Trigger.LEFT_CLICK_NO_HIT, null);
	}
	
	public static void handleMythicDespawn(MythicMobDespawnEvent e) {
		FightData data = removeFightData(e.getEntity().getUniqueId());
		if (data == null || data.getInstance() == null) return;
		data.getInstance().handleRespawn(data, e.getMobType().getInternalName(), true);
	}
	
	public static void handleMythicDeath(MythicMobDeathEvent e) {
		FightData data = removeFightData(e.getEntity().getUniqueId());
		if (data == null) return;
		data.getInstance().handleRespawn(data, e.getMobType().getInternalName(), false);
		
		if (data.getInstance() instanceof StandardFightInstance) {
			((StandardFightInstance) data.getInstance()).handleMobKill(e.getMobType().getInternalName());
		}
	}
	
	public void handleRespawn(FightData data, String id, boolean isDespawn) {
		Mob mob = Mob.get(id);
		if (mob == null) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to find meta-info for mob " + id + " to handle respawn");
			return;
		}

		if (data.getSpawner() != null) {
			data.getSpawner().subtractActiveMobs();
		}
		
		if (!isDespawn) {
			totalSpawnValue += mob.getValue();
			if (totalSpawnValue > KILLS_TO_SCALE) {
				spawnCounter++;
				totalSpawnValue -= KILLS_TO_SCALE;
			}
		}
		
		spawnCounter += mob.getValue();
		while (spawnCounter >= 1) {
			spawnCounter--;
			data.getInstance().spawnMob(1);
		}
	}
	
	// Method that's called by all listeners and is directly connected to events
	// Returns true if the event should be cancelled
	private static boolean trigger(Player p, Trigger trigger, Object[] obj) {
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null) return false;
		if (trigger.isSlotDependent()) {
			// Run triggers that change based on slot (like left/right click, NOT hotbar swap)
			data.runSlotBasedActions(data, trigger, p.getInventory().getHeldItemSlot(), obj);
		}
		return data.runActions(data, trigger, obj);
	}
	
	public static FightData getFightData(UUID uuid) {
		if (!fightData.containsKey(uuid)) {
			FightData fd = new FightData((Damageable) Bukkit.getEntity(uuid), (MapSpawnerInstance) null);
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
		
		if (data.hasStatus(StatusType.FROST) && type.containsBuffType(BuffType.PHYSICAL)) {
			Status status = data.getStatus(StatusType.FROST);
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
		else {
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
		
		// See if any of our effects cancel damage first
		if (data instanceof PlayerFightData) {
			PlayerFightData pdata = (PlayerFightData) data;
			if (pdata.runActions(pdata, Trigger.RECEIVED_DAMAGE, new Object[] {target, damager})) {
				return;
			}
		}
		
		// Reduce damage from barriers
		if (meta.hitBarrier() && data.getBarrier() != null) {
			if (data instanceof PlayerFightData) {
				((PlayerFightData) data).getStats().addDamageBarriered(amount);
			}
			amount = data.getBarrier().applyDefenseBuffs(amount, type);
		}
		
		// Status effects
		if (!meta.isSecondary()) {
			if (data.hasStatus(StatusType.BURN)) {
				dealDamage(damager, DamageType.FIRE, amount * 0.1, target);
			}
			
			if (data.hasStatus(StatusType.ELECTRIFIED)) {
				for (Entity e : target.getNearbyEntities(5, 5, 5)) {
					if (e == target) continue;
					if (e instanceof Player) continue;
					if (!(e instanceof Damageable)) continue;
					Damageable dmg = (Damageable) e;
					int stacks = data.getStatus("ELECTRIFIED").getStacks();
					dealDamage(damager, DamageType.LIGHTNING, amount * stacks * 0.1, dmg);
				}
			}
			
			if (data.hasStatus(StatusType.CONCUSSED) && type.containsBuffType(BuffType.PHYSICAL)) {
				int stacks = data.getStatus(StatusType.CONCUSSED).getStacks();
				dealDamage(damager, DamageType.EARTH, amount * stacks * 0.1, target);
			}
			
			if (data.hasStatus(StatusType.INSANITY) && type.containsBuffType(BuffType.MAGICAL)) {
				int stacks = data.getStatus(StatusType.INSANITY).getStacks();
				dealDamage(damager, DamageType.DARK, amount * stacks * 0.1, target);
			}
			
			if (data.hasStatus(StatusType.SANCTIFIED)) {
				int stacks = data.getStatus(StatusType.SANCTIFIED).getStacks();
				giveHeal(damager, amount * stacks * 0.1, target);
			}
			
			if (data.hasStatus(StatusType.THORNS)) {
				dealDamage(damager, new DamageMeta(amount * data.getStatus(StatusType.THORNS).getStacks(), DamageType.THORNS, false), target);
			}
		}

		// Calculate damage to shields
		if (data.getShields() != null && !data.getShields().isEmpty() && !meta.bypassShields()) {
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
		this.s = s;
		instantiate();
		s.broadcast("Commencing fight...");
		for (Player p : s.getOnlinePlayers()) {
			setup(p, s.getData(p.getUniqueId()));
		}
		setupInstance(s);
		
		new BukkitRunnable() {
			public void run() {
				// Choose random teleport location
				int rand = NeoCore.gen.nextInt(map.getPieces().size());
				MapPieceInstance inst = map.getPieces().get(rand);
				Coordinates[] spawns = inst.getSpawns();
				
				spawn = spawns[spawns.length > 1 ? NeoCore.gen.nextInt(spawns.length) : 0].clone().applySettings(inst).toLocation();
				spawn.add(s.getXOff() + MapPieceInstance.X_FIGHT_OFFSET,
						MapPieceInstance.Y_OFFSET,
						MapPieceInstance.Z_FIGHT_OFFSET + s.getZOff());
				spawn.setX(-spawn.getX());
				for (Player p : s.getOnlinePlayers()) {
					p.teleport(spawn);
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
		
		new BukkitRunnable() {
			public void run() {
				spawnMob(5 + (s.getNodesVisited() / 5));
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
		
		tasks.add(new BukkitRunnable() {
			boolean alternate = false;
			public void run() {
				alternate = !alternate;
				
				// Every 20 ticks
				if (alternate && !toTick.isEmpty()) {
					Iterator<UUID> iter = toTick.iterator();
					while (iter.hasNext()) {
						FightData data = fightData.get(iter.next());
						if (data == null || data.runTickActions() == TickResult.REMOVE) iter.remove();
					}
				}
				
				// Every 10 ticks
				for (Session s : SessionManager.getSessions()) {
					if (!(s.getInstance() instanceof FightInstance)) continue;
					for (Barrier b : ((FightInstance) s.getInstance()).getEnemyBarriers().values()) {
						b.tick();
					}
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L));
	}
	
	protected abstract void setupInstance(Session s);
	
	public static void putFightData(UUID uuid, FightData data) {
		fightData.put(uuid, data);
	}
	
	private void setup(Player p, PlayerSessionData data) {
		UUID uuid = p.getUniqueId();
		PlayerFightData fd = new PlayerFightData(this, data);
		fightData.put(uuid, fd);
		userData.put(uuid, fd);
	}
	
	@Override
	public void cleanup() {
		for (UUID uuid : s.getParty().keySet()) {
			userData.remove(uuid).cleanup();
			fightData.remove(uuid).cleanup();
			s.getParty().get(uuid).updateHealth();
		}
		
		for (BukkitTask task : tasks) {
			task.cancel();
		}
	}
	
	public static FightData removeFightData(UUID uuid) {
		toTick.remove(uuid);
		return fightData.remove(uuid);
	}
	
	public static void addToTickList(UUID uuid) {
		toTick.add(uuid);
	}
	
	// For any barrier that isn't the user's personal barrier (shield)
	public void addUserBarrier(PlayerFightData owner, Barrier b, int duration) {
		addBarrier(owner, b, duration, true);
	}
	
	public void addEnemyBarrier(PlayerFightData owner, Barrier b, int duration) {
		addBarrier(owner, b, duration, false);
	}
	
	public void addBarrier(FightData owner, Barrier b, int duration, boolean isUser) {
		HashMap<UUID, Barrier> barriers = isUser ? userBarriers : enemyBarriers;
		UUID uuid = b.getUniqueId();
		barriers.put(uuid, b);
		owner.addGuaranteedTask(uuid, new Runnable() {
			public void run() {
				barriers.remove(uuid);
			}
		}, duration * 20);
	}
	
	public void removeEnemyBarrier(UUID uuid) {
		enemyBarriers.remove(uuid);
	}
	
	public HashMap<UUID, Barrier> getEnemyBarriers() {
		return enemyBarriers;
	}
	
	public void addSpawner(MapSpawnerInstance spawner) {
		spawners.add(spawner);
		if (spawner.getMaxMobs() == -1) {
			unlimitedSpawners.add(spawner);
		}
	}
	
	private void spawnMob(int num) {
		for (int i = 0; i < num; i++) {
			MapSpawnerInstance spawner = spawners.get(NeoCore.gen.nextInt(spawners.size()));
			if (!spawner.canSpawn()) {
				spawner = unlimitedSpawners.get(NeoCore.gen.nextInt(unlimitedSpawners.size()));
			}
			spawner.spawnMob(5 + s.getNodesVisited());
		}
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] FightInstance attempted to save, this should never happen");
		return null;
	}
	
	public abstract String serializeInstanceData();
	public static FightInstance deserializeInstanceData(Session s, String str) {
		if (str.startsWith("STANDARD")) {
			return new StandardFightInstance(s.getParty().keySet(), Map.deserialize(str.substring("STANDARD:".length())));
		}
		else if (str.startsWith("MINIBOSS")) {
			return new MinibossFightInstance(s.getParty().keySet(), Map.deserialize(str.substring("MINIBOSS:".length())));
		}
		else {
			return new BossFightInstance(s.getParty().keySet(), Map.deserialize(str.substring("BOSS:".length())));
		}
	}
}
