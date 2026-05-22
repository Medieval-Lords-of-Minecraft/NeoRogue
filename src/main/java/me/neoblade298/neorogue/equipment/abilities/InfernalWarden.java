package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmoEquipmentInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class InfernalWarden extends Equipment {
	private static final String ID = "InfernalWarden";
	private static final double ORBIT_RADIUS = 4;
	private static final double HIT_RADIUS = 1.5;
	private static final int DURATION_TICKS = 120; // 6 seconds at 20 ticks/s
	private static final int HIT_COOLDOWN_TICKS = 20; // 1 second between hits on same target
	private static final TargetProperties hitTp = TargetProperties.radius(HIT_RADIUS, false, TargetType.ENEMY);
	private static final ParticleContainer orbPc = new ParticleContainer(Particle.FLAME)
			.count(3).spread(0.1, 0.1);
	private static final ParticleContainer trailPc = new ParticleContainer(Particle.SMOKE)
			.count(1).spread(0.05, 0.05);
	private static final ParticleContainer hitPc = new ParticleContainer(Particle.FLAME)
			.count(20).spread(0.5, 0.5);
	private static final ParticleContainer launchFlamePc = new ParticleContainer(Particle.FLAME)
			.count(1).spread(0.05, 0.05).speed(0);
	private static final ParticleContainer launchSmokePc = new ParticleContainer(Particle.SMOKE)
			.count(1).spread(0.03, 0.03).speed(0);
	private static final ParticleAnimation launchFlameAnim, launchSmokeAnim;

	static {
		launchFlameAnim = new ParticleAnimation(launchFlamePc, (loc, tick) -> {
			LinkedList<Location> locs = new LinkedList<>();
			double height = 1.5 + (1.5 * tick / 9.0);
			int points = 2 + (tick * 3 / 9);
			double spread = 0.1 + (tick * 0.15 / 9.0);
			for (int i = 0; i < points; i++) {
				double angle = 2 * Math.PI * i / points + tick * 0.5;
				locs.add(loc.clone().add(Math.cos(angle) * spread, height, Math.sin(angle) * spread));
			}
			if (tick == 9) {
				for (int i = 0; i < 8; i++) {
					double angle = 2 * Math.PI * i / 8;
					locs.add(loc.clone().add(Math.cos(angle) * 0.6, height, Math.sin(angle) * 0.6));
				}
			}
			return locs;
		}, 10);

		launchSmokeAnim = new ParticleAnimation(launchSmokePc, (loc, tick) -> {
			LinkedList<Location> locs = new LinkedList<>();
			double height = 1.5 + (1.5 * tick / 9.0) - 0.3;
			int points = 2;
			double spread = 0.05 + (tick * 0.05 / 9.0);
			for (int i = 0; i < points; i++) {
				double angle = 2 * Math.PI * i / points + tick * 0.3;
				locs.add(loc.clone().add(Math.cos(angle) * spread, height, Math.sin(angle) * spread));
			}
			return locs;
		}, 10);
	}

	private int damage, burn;

	public InfernalWarden(boolean isUpgraded) {
		super(ID, "Infernal Warden", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		damage = isUpgraded ? 150 : 100;
		burn = isUpgraded ? 10 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		InfernalWardenInstance inst = new InfernalWardenInstance(data, this, slot, es);
		data.addTrigger(id, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, inst);
	}

	private class InfernalWardenInstance extends AmmoEquipmentInstance {
		public InfernalWardenInstance(PlayerFightData data, Equipment equip, int slot, EquipSlot es) {
			super(data, equip, slot, es);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				Sounds.fire.play(p, p);
				Location loc = p.getLocation();
				data.runAnimation(id, p, launchFlameAnim, loc);
				data.runAnimation(id, p, launchSmokeAnim, loc);
				new BukkitRunnable() {
					public void run() {
						startOrbit(data.getPlayer(), data, slot);
					}
				}.runTaskLater(NeoRogue.inst(), 10L);
				return TriggerResult.of(false, true);
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			PreLaunchProjectileGroupEvent ev = (PreLaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return false;
			if (p.getEyeLocation().getDirection().getY() < 0.9) return false;
			return super.canTrigger(p, data, in);
		}

		private void startOrbit(Player p, PlayerFightData data, int slot) {
			data.addTask(new BukkitRunnable() {
				private int tick = 0;
				private HashMap<LivingEntity, Integer> hitCooldowns = new HashMap<>();

				public void run() {
					if (tick >= DURATION_TICKS) {
						this.cancel();
						return;
					}

					Player p = data.getPlayer();
					double angle = tick * 2 * Math.PI / 40; // Full revolution every 2 seconds (40 ticks)
					Location orbLoc = p.getLocation().add(
							Math.cos(angle) * ORBIT_RADIUS,
							1.0,
							Math.sin(angle) * ORBIT_RADIUS
					);

					orbPc.play(p, orbLoc);
					trailPc.play(p, orbLoc);

					// Check for nearby enemies at the orb location
					LivingEntity target = TargetHelper.getNearest(p, orbLoc, hitTp);
					if (target != null && (!hitCooldowns.containsKey(target) || tick - hitCooldowns.get(target) >= HIT_COOLDOWN_TICKS)) {
						hitCooldowns.put(target, tick);
						FightInstance.dealDamage(data, DamageType.FIRE, damage, target,
								DamageStatTracker.of(ID + slot, InfernalWarden.this));
						FightInstance.applyStatus(target, StatusType.BURN, data, burn, -1);
						Sounds.explode.play(p, target.getLocation());
						hitPc.play(p, target.getLocation());
					}

					tick++;
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 1L));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"Passive. Firing a basic attack straight up summons a fire projectile that orbits you at " +
				DescUtil.white((int) ORBIT_RADIUS) + " blocks for " + DescUtil.duration(6, false) + ". " +
				"On contact with an enemy, it deals " + GlossaryTag.FIRE.tag(this, damage, true) + " damage " +
				"and applies " + GlossaryTag.BURN.tag(this, burn, true) + ".");
	}
}
