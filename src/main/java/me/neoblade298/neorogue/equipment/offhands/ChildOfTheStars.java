package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ChildOfTheStars extends Equipment implements Power {
	private static final String ID = "ChildOfTheStars";
	private static final int RIFTS_TO_ACTIVATE = 3, RIFT_DURATION = 200, PROJECTILE_RANGE = 20;
	private static final TargetProperties TARGETS = TargetProperties.radius(PROJECTILE_RANGE, false, TargetType.ENEMY);
	private static final Circle RIFT_LAUNCH_RING = new Circle(0.75);
	private static final ParticleContainer RIFT_LAUNCH = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 55, 165), 1.1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer RIFT_SPARK = new ParticleContainer(Particle.FIREWORK)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(0.35);
	private static final ParticleContainer PROJECTILE_PARTICLE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(205, 185, 255), 0.9F)).count(2).spread(0.05, 0.05).speed(0);
	private static final ParticleContainer IMPACT_PARTICLE = new ParticleContainer(Particle.FIREWORK)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private int damage, interval;

	public ChildOfTheStars(boolean isUpgraded) {
		super(ID, "Child of the Stars", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.OFFHAND, EquipmentProperties.none());
		damage = isUpgraded ? 90 : 60;
		interval = isUpgraded ? 10 : 12;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta rifts = new ActionMeta();
		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			if (rifts.addCount(1) >= RIFTS_TO_ACTIVATE && activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ProjectileGroup projectiles = new ProjectileGroup(new StarProjectile(data, slot));
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				data.addRift(new Rift(data, data.getPlayer().getLocation(), RIFT_DURATION, ChildOfTheStars.this));
				for (Rift rift : data.getRifts().values()) {
					LivingEntity target = TargetHelper.getNearest(data.getPlayer(), rift.getLocation(), TARGETS);
					if (target == null) continue;
					Location origin = rift.getLocation();
					Vector direction = target.getEyeLocation().toVector().subtract(origin.toVector());
					RIFT_LAUNCH_RING.play(RIFT_LAUNCH, origin, LocalAxes.xz(), null);
					RIFT_SPARK.play(data.getPlayer(), origin);
					projectiles.start(data, origin, direction);
				}
				Sounds.enchant.play(data.getPlayer(), data.getPlayer());
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, interval * 20L));
	}

	private class StarProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private StarProjectile(PlayerFightData data, int slot) {
			super(1.2, PROJECTILE_RANGE, 1);
			this.data = data;
			this.slot = slot;
			homing(0.2).size(0.5, 0.5);
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK,
					DamageStatTracker.of(id + slot, ChildOfTheStars.this)));
			projectile.getMeta().addOrigin(DamageOrigin.RIFT);
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			PROJECTILE_PARTICLE.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			IMPACT_PARTICLE.play(data.getPlayer(), hit.getEntity());
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR, GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this)
				+ ". Activates after you create " + DescUtil.white(RIFTS_TO_ACTIVATE) + " "
				+ GlossaryTag.RIFT.tagPlural(this) + ". Every " + DescUtil.val(interval + "s") + ", create a "
				+ GlossaryTag.RIFT.tag(this) + " and fire a projectile from each Rift at its nearest enemy for "
				+ GlossaryTag.DARK.tag(this, damage) + " damage.");
	}
}