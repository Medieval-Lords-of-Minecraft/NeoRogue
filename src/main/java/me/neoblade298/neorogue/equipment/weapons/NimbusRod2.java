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

public class NimbusRod2 extends Equipment {
	private static final String ID = "NimbusRod2";
	private static final int RANGE = 5, PULSE_INTERVAL = 10, PULSE_COUNT = 5;
	private static final double AOE = 2, PROJECTILE_SPEED = 0.1;
	private static final TargetProperties PULSE_TARGETS = TargetProperties.radius(AOE, false, TargetType.ENEMY);
	private static final Circle PULSE_CIRCLE = new Circle(AOE);
	private static final ParticleContainer CLOUD = new ParticleContainer(Particle.CLOUD).count(5).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer SPARK = new ParticleContainer(Particle.FIREWORK).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer ELECTRIFIED_SPARK = new ParticleContainer(Particle.SOUL_FIRE_FLAME)
			.count(1).spread(0.1, 0).speed(0);
	private static final SoundContainer PULSE_SOUND = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.6F, 1.5F);

	private final int damage;
	private final int electrified;

	public NimbusRod2(boolean isUpgraded) {
		super(ID, "Nimbus Rod II", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(isUpgraded ? 80 : 60, 0.5, 0, 1, RANGE, DamageType.LIGHTNING,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP)
						.add(PropertyType.MANA_COST, 10)
						.add(PropertyType.AREA_OF_EFFECT, AOE));
		damage = isUpgraded ? 80 : 60;
		electrified = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup cloud = new ProjectileGroup(new NimbusProjectile(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();

			weaponSwing(data.getPlayer(), data);
			data.wandDelaySecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> cloud.start(data));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREEZE_ROD,
				"Fire a slow-moving cloud that deals " + GlossaryTag.LIGHTNING.tag(this, damage)
						+ " damage and applies " + GlossaryTag.ELECTRIFIED.tag(this, electrified)
						+ " to enemies within " + DescUtil.val((int) AOE) + " blocks twice per second, "
						+ DescUtil.val(PULSE_COUNT) + " times.");
	}

	private class NimbusProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public NimbusProjectile(PlayerFightData data, int slot) {
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

			PULSE_SOUND.play(data.getPlayer(), proj.getLocation());
			PULSE_CIRCLE.play(SPARK, proj.getLocation(), LocalAxes.xz(), ELECTRIFIED_SPARK);
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(data.getPlayer(), proj.getLocation(), PULSE_TARGETS)) {
				FightInstance.dealDamage(proj.getMeta().clone(), target);
				FightInstance.applyStatus(target, StatusType.ELECTRIFIED, data, electrified, -1, NimbusRod2.this);
			}

			if (proj.getTick() / PULSE_INTERVAL >= PULSE_COUNT)
				proj.cancel();
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, NimbusRod2.this, slot);
		}
	}
}