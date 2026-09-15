package me.neoblade298.neorogue.equipment.weapons;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
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
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class RodOfErosion extends Equipment {
	private static final String ID = "RodOfErosion";
	private static final int RANGE = 5, PULSE_INTERVAL = 10, PULSE_COUNT = 5, DEBUFF_DURATION = 120;
	private static final double AOE = 2, PROJECTILE_SPEED = 0.1;
	private static final TargetProperties PULSE_TARGETS = TargetProperties.radius(AOE, false, TargetType.ENEMY);
	private static final Circle PULSE_CIRCLE = new Circle(AOE);
	private static final ParticleContainer CLOUD = new ParticleContainer(Particle.CLOUD)
			.count(3).spread(0.1, 0.1).speed(0.01).offsetY(0.3);
	private static final ParticleContainer EROSION_CLOUD = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.TUFF.createBlockData()).count(3).spread(0.1, 0.1).speed(0.01).offsetY(0.3);
	private static final ParticleContainer PULSE_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 135, 75), 0.8F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer PULSE_FILL = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.TUFF.createBlockData()).count(1).spread(0.1, 0).speed(0);
	private static final SoundContainer PULSE_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.55F, 0.8F);
	private static final SoundContainer CRUMBLE_SOUND = new SoundContainer(Sound.BLOCK_GRAVEL_BREAK, 0.35F, 0.7F);

	private final int damage;
	private final double defenseReduction;
	private final int defenseReductionPercent;

	public RodOfErosion(boolean isUpgraded) {
		super(ID, "Rod of Erosion", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(60, 0.5, 0, 1, RANGE, DamageType.EARTHEN,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP)
						.add(PropertyType.MANA_COST, 10)
						.add(PropertyType.AREA_OF_EFFECT, AOE));
		damage = 60;
		defenseReductionPercent = isUpgraded ? 13 : 10;
		defenseReduction = defenseReductionPercent * 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ProjectileGroup cloud = new ProjectileGroup(new ErosionProjectile(data, slot));
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
				"Fire a slow-moving cloud that deals " + GlossaryTag.EARTHEN.tag(this, damage)
						+ " damage to enemies within " + DescUtil.val((int) AOE) + " blocks twice per second, "
						+ DescUtil.val(PULSE_COUNT) + " times. Each hit applies a stacking "
						+ DescUtil.val(defenseReductionPercent + "%") + " Earthen defense reduction "
						+ DescUtil.duration(DEBUFF_DURATION / 20) + ".");
	}

	private class ErosionProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public ErosionProjectile(PlayerFightData data, int slot) {
			super(PROJECTILE_SPEED, RANGE, 1);
			this.data = data;
			this.slot = slot;
			ignore(false, false, true);
			size(1, 1);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			CLOUD.play(data.getPlayer(), proj.getLocation());
			EROSION_CLOUD.play(data.getPlayer(), proj.getLocation());
			if (proj.getTick() % PULSE_INTERVAL != 0)
				return;

			PULSE_SOUND.play(data.getPlayer(), proj.getLocation());
			CRUMBLE_SOUND.play(data.getPlayer(), proj.getLocation());
			PULSE_CIRCLE.play(PULSE_EDGE, proj.getLocation(), LocalAxes.xz(), PULSE_FILL);
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(data.getPlayer(), proj.getLocation(), PULSE_TARGETS)) {
				FightInstance.dealDamage(proj.getMeta().clone(), target);
				FightData targetData = FightInstance.getFightData(target);
				if (targetData != null) {
					targetData.addDefenseBuff(DamageBuffType.of(DamageCategory.EARTHEN),
							Buff.multiplier(data, -defenseReduction, BuffStatTracker.defenseDebuffEnemy(
									UUID.randomUUID().toString(), RodOfErosion.this)), DEBUFF_DURATION);
				}
			}

			if (proj.getTick() / PULSE_INTERVAL >= PULSE_COUNT)
				proj.cancel();
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, RodOfErosion.this, slot);
		}
	}
}