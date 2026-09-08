package me.neoblade298.neorogue.map;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;

public class MapSpawnerInstance {
	private static final int SPAWN_DELAY = 14;
	// Hardcoded cooldown (ms) applied after a spawner fires, so it can't be selected again
	// immediately. Covers the telegraph window and prevents clumping at one spawner.
	private static final long SPAWN_COOLDOWN = 1000L;
	private static final Circle circ = new Circle(0.75);
	private static final ParticleContainer ring = new ParticleContainer(Particle.SOUL_FIRE_FLAME)
			.count(1).spread(0, 0).speed(0).forceVisible(Audience.ALL);
	private static final ParticleContainer swirl = new ParticleContainer(Particle.WITCH)
			.count(6).spread(0.35, 0.5).speed(0.01).offsetY(0.7).forceVisible(Audience.ALL);
	private Session s;
	private FightInstance fight;
	private MapPieceInstance piece;
	private MapSpawner origin;
	private Location loc;
	private int maxMobs, activeMobs;
	private long cooldownUntil;
	
	public MapSpawnerInstance(Session s, FightInstance fight, MapSpawner original, MapPieceInstance inst, int xOff, int zOff) {
		this.s = s;
		this.fight = fight;
		this.origin = original;
		this.loc = original.getCoordinates().clone().applySettings(inst).toLocation();
		this.loc.add(
				MapPieceInstance.X_FIGHT_OFFSET + xOff, MapPieceInstance.Y_OFFSET,
				MapPieceInstance.Z_FIGHT_OFFSET + zOff
		);
		this.loc.setX(-this.loc.getX() + (this.loc.getX() % 1 != 0 ? 1 : 0));
		this.maxMobs = original.getMaxMobs();
		this.piece = inst;
	}
	
	public Location getLocation() {
		return loc;
	}

	public MapPieceInstance getPiece() {
		return piece;
	}
	
	public boolean canSpawn() {
		return maxMobs == -1 || activeMobs < maxMobs;
	}
	
	public boolean isOnCooldown() {
		return System.currentTimeMillis() < cooldownUntil;
	}
	
	// Whether this spawner may be picked right now: not at capacity and not on cooldown.
	public boolean isSelectable() {
		return canSpawn() && !isOnCooldown();
	}
	
	public int getMaxMobs() {
		return maxMobs;
	}
	
	public MythicMob getMythicMob() {
		return origin.getMythicMob();
	}
	
	public Mob getMob() {
		return origin.getMob();
	}

	public String getMobId() {
		return origin.getMobId();
	}

	public boolean isResolved() {
		return origin.isResolved();
	}
	
	public void spawnMob() {
		if (origin.getMob() == null || origin.getMythicMob() == null) {
			Bukkit.getLogger().severe("[NeoRogue Spawn] Cannot spawn map mob '" + origin.getMobId()
					+ "': " + (origin.getMob() == null ? "NeoRogue mob is unresolved" : "MythicMob is unresolved"));
			return;
		}
		if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] spawnMob() called for " + origin.getMob().getId()
				+ " amount=" + origin.getMob().getAmount() + " activeMobs=" + activeMobs + "/" + maxMobs
				+ " at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
		// Put the spawner on cooldown so it isn't re-selected during/right after this spawn
		cooldownUntil = System.currentTimeMillis() + SPAWN_COOLDOWN;
		for (int i = 0; i < origin.getMob().getAmount(); i++) {
			Location spawnLoc = this.loc;
			if (origin.getRadius() > 0) {
				double radius = origin.getRadius();
				spawnLoc = spawnLoc.clone()
						.add(NeoRogue.gen.nextDouble(-radius, radius), 0, NeoRogue.gen.nextDouble(-radius, radius));
			}
			final Location fLoc = spawnLoc;
			// Reserve one slot per mob up front so canSpawn() stays accurate during the spawn delay
			activeMobs++;
			// Show a summoning telegraph for 1 second, then spawn the mob
			new BukkitRunnable() {
				int ticks = 0;
				@Override
				public void run() {
					// Abort if the fight ended during the telegraph
					if (fight == null || !fight.isActive() || s.getInstance() != fight) {
						activeMobs--;
						this.cancel();
						return;
					}
					if (ticks >= SPAWN_DELAY) {
						this.cancel();
						boolean spawned = false;
						try {
							spawned = spawnMobAt(fLoc);
						} catch (RuntimeException ex) {
							Bukkit.getLogger().severe("[NeoRogue Spawn] Failed to spawn map mob '" + origin.getMobId()
									+ "' at " + formatLocation(fLoc));
							ex.printStackTrace();
						} finally {
							if (!spawned) activeMobs--;
						}
						return;
					}
					circ.play(ring, fLoc, LocalAxes.xz(), null);
					swirl.play(fLoc);
					ticks += 2;
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 2L);
		}
	}

	// Actually spawns the mob at the telegraphed location. Returns false if the spawn failed
	// (e.g. the fight ended between the telegraph and the spawn).
	private boolean spawnMobAt(Location loc) {
		MythicMob mythicMob = origin.getMythicMob();
		if (mythicMob == null) return false;
		ActiveMob am = FightInstance.spawnScaledMob(s, loc, mythicMob);
		if (am == null || am.getEntity() == null || !(am.getEntity().getBukkitEntity() instanceof LivingEntity entity)) {
			Bukkit.getLogger().severe("[NeoRogue Spawn] MythicMobs returned no living entity for '"
					+ origin.getMobId() + "' at " + formatLocation(loc));
			return false;
		}
		UUID uuid = entity.getUniqueId();
		FightData fd = new FightData(entity, am, origin.getMob(), this);
		if (fd.getEntity() == null) {
			entity.remove();
			Bukkit.getLogger().severe("[NeoRogue Spawn] Spawned mob '" + origin.getMobId()
					+ "' could not be attached to a fight at " + formatLocation(loc));
			return false;
		}
		fd.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
		FightInstance.putFightData(uuid, fd);
		return true;
	}

	private static String formatLocation(Location loc) {
		return (loc.getWorld() == null ? "null" : loc.getWorld().getName()) + " "
				+ loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
	}
	
	public void subtractActiveMobs() {
		activeMobs--;
	}
}
