package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class BowDuplicator extends Equipment {
	private static final String ID = "BowDuplicator";
	private static final int TRAP_DURATION_TICKS = 120, FIRE_INTERVAL_TICKS = 60, TARGET_RANGE = 12;
	private static final TargetProperties TARGETS = TargetProperties.radius(TARGET_RANGE, false, TargetType.ENEMY);
	private static final Circle PLACEMENT_RING = new Circle(0.75);
	private static final ParticleContainer TRAP_EDGE = new ParticleContainer(Particle.CRIT).count(1)
			.spread(0, 0).speed(0);
	private static final ParticleContainer SHOT_ORIGIN = new ParticleContainer(Particle.FIREWORK).count(4)
			.spread(0.08, 0.08).offsetY(0.5).speed(0.01);
	private static final ParticleContainer EXPIRE = new ParticleContainer(Particle.CLOUD).count(5)
			.spread(0.1, 0.05).offsetY(0.2).speed(0.01);
	private int placementInterval;

	public BowDuplicator(boolean isUpgraded) {
		super(ID, "Bow Duplicator", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		placementInterval = isUpgraded ? 8 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta timer = new ActionMeta();
		Bow[] lastBow = new Bow[1];
		AmmunitionInstance[] lastAmmo = new AmmunitionInstance[1];

		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			if (!event.isBasicAttack() || !(event.getGroup().getFirst() instanceof BowProjectile projectile)) {
				return TriggerResult.keep();
			}
			lastBow[0] = projectile.getBow();
			lastAmmo[0] = projectile.getAmmunition();
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (timer.addCount(1) < placementInterval) return TriggerResult.keep();
			timer.setCount(0);
			if (lastBow[0] != null && lastAmmo[0] != null) {
				placeTrap(data, slot, lastBow[0], lastAmmo[0]);
			}
			return TriggerResult.keep();
		});
	}

	private void placeTrap(PlayerFightData data, int slot, Bow bow, AmmunitionInstance ammo) {
		Location location = data.getPlayer().getLocation().clone();
		PLACEMENT_RING.play(TRAP_EDGE, location, LocalAxes.xz(), null);
		Sounds.equip.play(data.getPlayer(), location);
		data.addTrap(new Trap(data, location, TRAP_DURATION_TICKS, FIRE_INTERVAL_TICKS, this) {
			@Override
			public void tick() {
				Player player = data.getPlayer();
				LivingEntity target = TargetHelper.getNearest(player, location, TARGETS);
				if (target == null) return;
				Vector direction = target.getEyeLocation().toVector().subtract(location.toVector()).normalize();
				SHOT_ORIGIN.play(player, location);
				new ProjectileGroup(new DuplicatedBowProjectile(data, bow, ammo, slot))
						.start(data, location.clone().add(0, 0.5, 0), direction);
			}

			@Override
			public void onDeactivate() {
				super.onDeactivate();
				Player player = data.getPlayer();
				EXPIRE.play(player, location);
				Sounds.extinguish.play(player, location);
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CRAFTER, "Every " + DescUtil.val(placementInterval + "s") + ", drop a "
				+ GlossaryTag.TRAP.tag(this) + " [" + DescUtil.val("6s")
				+ "] that copies your last used weapon and ammunition and fires it every "
				+ DescUtil.val("3s") + ". These shots are not basic attacks.");
	}

	private class DuplicatedBowProjectile extends Projectile {
		private final PlayerFightData data;
		private final Bow bow;
		private final AmmunitionInstance ammo;
		private final int slot;

		private DuplicatedBowProjectile(PlayerFightData data, Bow bow, AmmunitionInstance ammo, int slot) {
			super(bow.getProperties().get(PropertyType.RANGE), 1);
			setBowDefaults();
			this.data = data;
			this.bow = bow;
			this.ammo = ammo;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			Player player = data.getPlayer();
			bow.onTick(player, projectile, interpolation);
			ammo.onTick(player, projectile, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			ammo.onHit(projectile, meta, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			Sounds.shoot.play(data.getPlayer(), projectile.getLocation());
			EquipmentProperties ammoProperties = ammo.getProperties();
			DamageMeta meta = projectile.getMeta();
			meta.addDamageSlice(new DamageSlice(data, bow.getProperties().get(PropertyType.DAMAGE), ammoProperties.getType(),
					DamageStatTracker.of(id + slot, BowDuplicator.this)));
			meta.addDamageSlice(new DamageSlice(data, ammoProperties.get(PropertyType.DAMAGE), ammoProperties.getType(),
					DamageStatTracker.of(id + slot, ammo.getAmmo())));
			meta.addOrigin(DamageOrigin.TRAP);
			ammo.onStart(projectile, false);
		}
	}
}