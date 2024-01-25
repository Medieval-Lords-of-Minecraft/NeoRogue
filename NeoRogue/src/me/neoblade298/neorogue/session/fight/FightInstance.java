package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
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
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;

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
			unlimitedSpawners = new ArrayList<MapSpawnerInstance>(),
			initialSpawns = new ArrayList<MapSpawnerInstance>();
	private HashMap<String, Location> mythicLocations = new HashMap<String, Location>();
	protected HashMap<UUID, Barrier> enemyBarriers = new HashMap<UUID, Barrier>();
	protected ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
	protected ArrayList<FightRunnable> initialTasks = new ArrayList<FightRunnable>();
	protected double spawnCounter; // Holds a value between 0 and 1, when above 1, a mob spawns
	protected double totalSpawnValue; // Keeps track of total mob spawns, to handle scaling of spawning
	protected int level; // The level of the instance

	public FightInstance(Set<UUID> players) {
		party.addAll(players);
	}

	public void addInitialTask(FightRunnable runnable) {
		initialTasks.add(runnable);
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
		Location prev = p.getLocation();

		// Remove the player's abilities from the fight
		UUID pu = p.getUniqueId();
		if (!userData.containsKey(pu)) return;
		PlayerFightData data = userData.remove(pu);
		data.cleanup();
		fightData.remove(pu).cleanup();

		new BukkitRunnable() {
			public void run() {
				p.spigot().respawn();
				p.teleport(prev);
				data.getSessionData().setDeath(true);

				// If that's the last player alive, send them to a post death instance
				for (UUID uuid : data.getInstance().getParty()) {
					PlayerFightData fdata = userData.get(uuid);
					if (fdata != null && fdata.isActive()) return;
				}

				// End game as a loss
				data.getInstance().s.setInstance(new LoseInstance());
			}
		}.runTaskLater(NeoRogue.inst(), 5L);
	}

	// This handles basic left click and/or enemy damage
	public static void handleDamage(EntityDamageByEntityEvent e) {
		Player p = (Player) e.getDamager();
		e.setCancelled(true);
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null) return; // If you're dead
		if (!data.canBasicAttack()) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;

		trigger(p, Trigger.LEFT_CLICK, null);
		trigger(p, Trigger.LEFT_CLICK_HIT, new LeftClickHitEvent((LivingEntity) e.getEntity()));
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
			handleRightClickGeneral(e);
		}
	}

	public static void handleRightClickEntity(PlayerInteractEntityEvent e) {
		Player p = (Player) e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null) return; // If you're dead
		if (!data.canBasicAttack(EquipSlot.OFFHAND)) return;
		if (!(e.getRightClicked() instanceof LivingEntity)) return;
		trigger(p, Trigger.RIGHT_CLICK_HIT, new RightClickHitEvent((LivingEntity) e.getRightClicked()));
	}

	public static void handleRightClickGeneral(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
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
			if (p.isSneaking()) {
				trigger(p, Trigger.SHIFT_RCLICK, null);
			}

			if (y > 0) {
				trigger(p, Trigger.UP_RCLICK, null);
			}
			else {
				trigger(p, Trigger.DOWN_RCLICK, null);
			}
		}
	}

	public static void handleLeftClick(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		Player p = e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null) return;
		if (!data.canBasicAttack()) return;

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
		String id = e.getMobType().getInternalName();
		data.getInstance().handleRespawn(data, id, false);
		data.getInstance().handleMobKill(id);
	}

	public abstract void handleMobKill(String id);

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
			data.getInstance().activateSpawner(1);
		}
	}

	// Method that's called by all listeners and is directly connected to events
	// Returns true if the event should be cancelled
	public static boolean trigger(Player p, Trigger trigger, Object obj) {
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null) return false;
		if (trigger.isSlotDependent()) {
			// Run triggers that change based on slot (anything that starts with left click)
			data.runSlotBasedActions(data, trigger, p.getInventory().getHeldItemSlot(), obj);
		}
		return data.runActions(data, trigger, obj);
	}

	public static FightData getFightData(UUID uuid) {
		if (!fightData.containsKey(uuid)) {
			FightData fd = new FightData((LivingEntity) Bukkit.getEntity(uuid), (MapSpawnerInstance) null);
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

	public static void giveHeal(LivingEntity caster, double amount, LivingEntity... targets) {
		for (LivingEntity target : targets) {
			if (!(target instanceof Attributable)) continue;
			PlayerFightData cfd = FightInstance.getUserData(caster.getUniqueId());
			PlayerFightData tfd = FightInstance.getUserData(target.getUniqueId());
			
			double toSet = Math.min(caster.getHealth() + amount,
					((Attributable) caster).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			double actual = toSet - caster.getHealth();
			
			if(caster == target) {
				if (cfd != null) {
					cfd.getStats().addSelfHealing(actual);
				}
			}
			else {
				if (cfd != null) {
					cfd.getStats().addHealingGiven(actual);
				}
				if (tfd != null) {
					tfd.getStats().addHealingReceived(actual);
				}
			}
			
			caster.setHealth(toSet);
		}
	}

	public static void applyStatus(Entity target, String id, Entity applier, int stacks, int seconds) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(GenericStatusType.BASIC, id, applier.getUniqueId(), stacks, seconds);
	}

	public static void applyStatus(Entity target, StatusType type, Entity applier, int stacks, int seconds) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(type, applier.getUniqueId(), stacks, seconds);
	}

	public static void applyStatus(Entity target, GenericStatusType type, String id, Entity applier, int stacks,
			int seconds) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(type, id, applier.getUniqueId(), stacks, seconds);
	}

	public static void dealDamage(FightData owner, DamageType type, double amount, LivingEntity target) {
		dealDamage(new DamageMeta(owner, amount, type), target);
	}

	public static void knockback(Entity src, Entity trg, double force) {
		knockback(src.getLocation(), trg, force);
	}

	public static void knockback(Location src, Entity trg, double force) {
		Vector v = src.subtract(trg.getLocation()).toVector().normalize().multiply(force);
		knockback(v, trg, force);
	}

	public static void knockback(Vector v, Entity trg, double force) {
		trg.setVelocity(trg.getVelocity().add(v.multiply(force)));
	}

	public static void dealDamage(DamageMeta meta, Collection<LivingEntity> targets) {
		trigger((Player) meta.getOwner().getEntity(), Trigger.DEALT_DAMAGE_MULTIPLE, new DealtDamageEvent(meta));
		for (LivingEntity target : targets) {
			meta.clone().dealDamage(target);
		}
	}

	public static void dealDamage(DamageMeta meta, LivingEntity target) {
		trigger((Player) meta.getOwner().getEntity(), Trigger.DEALT_DAMAGE, new DealtDamageEvent(meta));
		meta.dealDamage(target);
	}

	@Override
	public void start(Session s) {
		this.s = s;
		level = 5 + s.getNodesVisited();

		instantiate();
		s.broadcast("Commencing fight...");
		ArrayList<PlayerFightData> fdata = new ArrayList<PlayerFightData>();
		for (Player p : s.getOnlinePlayers()) {
			fdata.add(setup(p, s.getData(p.getUniqueId())));
		}
		setupInstance(s);
		FightInstance fi = this;

		new BukkitRunnable() {
			public void run() {
				// Choose random teleport location
				int rand = NeoRogue.gen.nextInt(map.getPieces().size());
				MapPieceInstance inst = map.getPieces().get(rand);
				Coordinates[] spawns = inst.getSpawns();

				spawn = spawns[spawns.length > 1 ? NeoRogue.gen.nextInt(spawns.length) : 0].clone().applySettings(inst)
						.toLocation();
				spawn.add(s.getXOff() + MapPieceInstance.X_FIGHT_OFFSET, MapPieceInstance.Y_OFFSET,
						MapPieceInstance.Z_FIGHT_OFFSET + s.getZOff());
				spawn.setX(-spawn.getX());

				for (Player p : s.getOnlinePlayers()) {
					p.teleport(spawn);
				}

				for (FightRunnable runnable : initialTasks) {
					runnable.run(fi, fdata);
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20L);

		new BukkitRunnable() {
			public void run() {
				activateSpawner(2 + (s.getNodesVisited() / 5) + s.getParty().size());
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

	private PlayerFightData setup(Player p, PlayerSessionData data) {
		UUID uuid = p.getUniqueId();
		PlayerFightData fd = new PlayerFightData(this, data);
		fightData.put(uuid, fd);
		userData.put(uuid, fd);
		return fd;
	}

	@Override
	public void cleanup() {
		for (UUID uuid : s.getParty().keySet()) {
			PlayerFightData pdata = userData.remove(uuid);
			if (pdata != null) pdata.cleanup();
			FightData fdata = fightData.remove(uuid);
			if (fdata != null) fdata.cleanup();
			PlayerSessionData data = s.getParty().get(uuid);
			if (!data.isDead()) {
				data.updateHealth();
			}
			else {
				data.setDeath(false);
			}

			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				data.syncHealth();
				p.setFoodLevel(20);
				data.revertMaxHealth();
				data.updateCoinsBar();
			}
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

	public Location getMythicLocation(String key) {
		return mythicLocations.get(key);
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

	public void addInitialSpawn(MapSpawnerInstance spawner) {
		initialSpawns.add(spawner);
	}

	public void addMythicLocation(String key, Location loc) {
		mythicLocations.put(key, loc);
	}

	protected void activateSpawner(int num) {
		if (spawners.isEmpty()) return;
		for (int i = 0; i < num; i++) {
			MapSpawnerInstance spawner = spawners.get(NeoRogue.gen.nextInt(spawners.size()));
			if (!spawner.canSpawn()) {
				spawner = unlimitedSpawners.get(NeoRogue.gen.nextInt(unlimitedSpawners.size()));
			}
			spawner.spawnMob(4 + s.getNodesVisited());
		}
	}

	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] FightInstance attempted to save, this should never happen");
		return null;
	}

	public abstract String serializeInstanceData();

	public static FightInstance deserializeInstanceData(HashMap<UUID, PlayerSessionData> party, String str) {
		if (str.startsWith("STANDARD")) {
			return new StandardFightInstance(party.keySet(), Map.deserialize(str.substring("STANDARD:".length())));
		}
		else if (str.startsWith("MINIBOSS")) {
			return new MinibossFightInstance(party.keySet(), Map.deserialize(str.substring("MINIBOSS:".length())));
		}
		else {
			return new BossFightInstance(party.keySet(), Map.deserialize(str.substring("BOSS:".length())));
		}
	}

	public interface FightRunnable {
		public void run(FightInstance inst, ArrayList<PlayerFightData> fdata);
	}
}
