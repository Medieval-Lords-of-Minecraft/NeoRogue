package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class DevouringWildfire extends Equipment implements Power {
	private static final String ID = "DevouringWildfire";
	private static final int ACTIVATION_DAMAGE = 500;
	private static final int PROJECTILE_RADIUS = 4;
	private static final int PULSE_RADIUS = 5;
	private static final int PULSE_COUNT = 3;
	private static final long WILDFIRE_COOLDOWN_MILLIS = 1000;
	private static final long PULSE_DELAY = 20;
	private static final long PULSE_PERIOD = 20;
	private static final TargetProperties PROJECTILE_TARGETS = TargetProperties.radius(PROJECTILE_RADIUS, false,
			TargetType.ENEMY);
	private static final TargetProperties PULSE_TARGETS = TargetProperties.radius(PULSE_RADIUS, false, TargetType.ENEMY);
	private static final ParticleContainer PROJECTILE_PARTICLE = new ParticleContainer(Particle.FLAME);
	private static final ParticleContainer PROJECTILE_FILL = new ParticleContainer(Particle.LAVA);
	private static final ParticleContainer PULSE_PARTICLE = new ParticleContainer(Particle.FLAME).offsetY(0.3)
			.spread(0.2, 0.2).count(5);
	private static final Circle PROJECTILE_CIRCLE = new Circle(PROJECTILE_RADIUS);
	private static final Circle PULSE_CIRCLE = new Circle(PULSE_RADIUS);
	private int projectileDamage, pulseDamage, burnThreshold;

	public DevouringWildfire(boolean isUpgraded) {
		super(ID, "Devouring Wildfire", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		projectileDamage = isUpgraded ? 120 : 90;
		pulseDamage = isUpgraded ? 60 : 45;
		burnThreshold = isUpgraded ? 12 : 15;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta damageDealt = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			damageDealt.addDouble(event.getMeta().getPostMitigationDamage().getOrDefault(DamageType.FIRE, 0D));
			if (damageDealt.getDouble() < ACTIVATION_DAMAGE) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta wildfireCooldown = new ActionMeta();
		ActionMeta burnApplied = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.BURN)) return TriggerResult.keep();

			if (System.currentTimeMillis() - wildfireCooldown.getTime() >= WILDFIRE_COOLDOWN_MILLIS) {
				wildfireCooldown.setTime(System.currentTimeMillis());
				new ProjectileGroup(new DevouringWildfireProjectile(data, slot)).start(data);
			}

			burnApplied.addCount(event.getStacks());
			while (burnApplied.getCount() >= burnThreshold) {
				burnApplied.addCount(-burnThreshold);
				startEngulf(data, slot);
			}
			return TriggerResult.keep();
		});
	}

	private void startEngulf(PlayerFightData data, int slot) {
		data.addTask(new BukkitRunnable() {
			private int pulses;

			@Override
			public void run() {
				Player player = data.getPlayer();
				Sounds.fire.play(player, player);
				PULSE_CIRCLE.play(PULSE_PARTICLE, player.getLocation(), LocalAxes.xz(), null);
				for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, PULSE_TARGETS)) {
					FightInstance.dealDamage(new DamageMeta(data, pulseDamage, DamageType.FIRE,
							DamageStatTracker.of(id + slot, DevouringWildfire.this)), target);
				}
				if (++pulses >= PULSE_COUNT) cancel();
			}
		}.runTaskTimer(NeoRogue.inst(), PULSE_DELAY, PULSE_PERIOD));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing "
						+ GlossaryTag.FIRE.tag(this, ACTIVATION_DAMAGE) + " damage. When you apply "
						+ GlossaryTag.BURN.tag(this) + ", launch a fireball randomly in front of you that arcs, dealing "
						+ GlossaryTag.FIRE.tag(this, projectileDamage) + " damage in a " + DescUtil.val(PROJECTILE_RADIUS)
						+ " block radius upon hitting an enemy or block. Every "
						+ GlossaryTag.BURN.tag(this, burnThreshold) + " you apply, deal "
						+ GlossaryTag.FIRE.tag(this, pulseDamage) + " damage to all enemies within "
						+ DescUtil.val(PULSE_RADIUS) + " blocks " + DescUtil.val(PULSE_COUNT) + " times over "
						+ DescUtil.val("3s") + ".");
	}

	private class DevouringWildfireProjectile extends Projectile {
		private static final int RANGE = 10;
		private static final int BLOCKS_PER_TICK = 2;
		private static final double GRAVITY = 0.05;
		private static final double ARC = 0.5;
		private static final double MAX_ROTATION = 30;
		private final PlayerFightData data;
		private final int slot;

		public DevouringWildfireProjectile(PlayerFightData data, int slot) {
			super(1, RANGE, BLOCKS_PER_TICK);
			gravity(GRAVITY);
			rotation(NeoRogue.gen.nextDouble(-MAX_ROTATION, MAX_ROTATION));
			arc(ARC);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			PROJECTILE_PARTICLE.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			dealDamageArea(hit.getEntity().getLocation());
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block block) {
			dealDamageArea(block.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player player = data.getPlayer();
			Sounds.fire.play(player, player);
		}

		private void dealDamageArea(Location location) {
			while (location.getBlock().getType().isAir()) {
				location.add(0, -1, 0);
			}
			Player player = data.getPlayer();
			Sounds.explode.play(player, location);
			PROJECTILE_CIRCLE.play(player, PROJECTILE_PARTICLE, location, LocalAxes.xz(), PROJECTILE_FILL);
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, location, PROJECTILE_TARGETS)) {
				DamageMeta damage = new DamageMeta(data, projectileDamage, DamageType.FIRE,
						DamageStatTracker.of(id + slot, DevouringWildfire.this));
				FightInstance.dealDamage(damage, target);
			}
		}
	}
}