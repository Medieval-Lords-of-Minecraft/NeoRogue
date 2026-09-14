package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class FirestormRod extends Equipment {
	private static final String ID = "FirestormRod";
	private static final int RANGE = 5, PULSE_INTERVAL = 10, PULSE_COUNT = 5, USES_PER_CORRUPTION = 10,
			MAX_SCALING_STACKS = 5;
	private static final double BASE_AOE = 2, PROJECTILE_SPEED = 0.1, SCALING_PER_CORRUPTION = 0.2;
	private static final ParticleContainer CLOUD = new ParticleContainer(Particle.SMOKE).count(5).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer FLAME_EDGE = new ParticleContainer(Particle.FLAME).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer FLAME_FILL = new ParticleContainer(Particle.FLAME).count(1).spread(0.1, 0).speed(0);
	private static final SoundContainer PULSE_SOUND = new SoundContainer(Sound.ENTITY_BLAZE_SHOOT, 0.45F, 1.35F);

	private final int damage;

	public FirestormRod(boolean isUpgraded) {
		super(ID, "Firestorm Rod", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(isUpgraded ? 60 : 40, 0.5, 0, 1, RANGE, DamageType.FIRE,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP)
						.add(PropertyType.MANA_COST, 10)
						.add(PropertyType.AREA_OF_EFFECT, BASE_AOE));
		damage = isUpgraded ? 60 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta uses = new ActionMeta();
		ProjectileGroup cloud = new ProjectileGroup(new FirestormProjectile(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();

			weaponSwing(data.getPlayer(), data);
			if (uses.addCount(1) >= USES_PER_CORRUPTION) {
				uses.setCount(0);
				data.applyStatus(StatusType.CORRUPTION, data, 1, -1, FirestormRod.this);
			}
			data.wandDelaySecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> cloud.start(data));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREEZE_ROD,
				"Fire a slow-moving cloud that deals " + GlossaryTag.FIRE.tag(this, damage)
						+ " damage to enemies within " + DescUtil.val((int) BASE_AOE)
						+ " blocks twice per second, " + DescUtil.val(PULSE_COUNT) + " times. Damage and area of effect "
						+ "increase by " + DescUtil.val("20%") + " per " + GlossaryTag.CORRUPTION.tag(this)
						+ ", up to " + DescUtil.val(MAX_SCALING_STACKS) + " times. Every "
						+ DescUtil.val(USES_PER_CORRUPTION) + " uses, gain " + GlossaryTag.CORRUPTION.tag(this, 1) + ".");
	}

	private double getScaling(PlayerFightData data) {
		int corruption = data.hasStatus(StatusType.CORRUPTION)
				? Math.min(data.getStatus(StatusType.CORRUPTION).getStacks(), MAX_SCALING_STACKS) : 0;
		return 1 + corruption * SCALING_PER_CORRUPTION;
	}

	private class FirestormProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public FirestormProjectile(PlayerFightData data, int slot) {
			super(PROJECTILE_SPEED, RANGE, 1);
			this.data = data;
			this.slot = slot;
			ignore(false, false, true);
			size(1, 1);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			CLOUD.play(data.getPlayer(), proj.getLocation());
			if (proj.getTick() % PULSE_INTERVAL != 0)
				return;

			double scaling = getScaling(data);
			double areaOfEffect = BASE_AOE * scaling;
			PULSE_SOUND.play(data.getPlayer(), proj.getLocation());
			new Circle(areaOfEffect).play(FLAME_EDGE, proj.getLocation(), LocalAxes.xz(), FLAME_FILL);
			TargetProperties targets = TargetProperties.radius(areaOfEffect, false, TargetType.ENEMY);
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(data.getPlayer(), proj.getLocation(), targets)) {
				DamageMeta meta = new DamageMeta(data, damage * scaling, DamageType.FIRE,
						DamageStatTracker.of(id + slot, FirestormRod.this));
				meta.setProjectileInstance(proj);
				meta.isBasicAttack(FirestormRod.this, true);
				FightInstance.dealDamage(meta, target);
			}

			if (proj.getTick() / PULSE_INTERVAL >= PULSE_COUNT)
				proj.cancel();
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
		}

		@Override
		public void onStart(ProjectileInstance proj) {
		}
	}
}