package me.neoblade298.neorogue.equipment.weapons;

import java.util.UUID;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class InfernalScepter extends Equipment {
	private static final String ID = "InfernalScepter";
	private static final int HIT_SCAN_RANGE = 12;
	private static final int DAMAGE = 40;
	private static final int AREA_OF_EFFECT = 5;
	private static final int BURN_THRESHOLD = 10;
	private static final int USES_PER_CORRUPTION = 12;
	private static final TargetProperties TARGETS = TargetProperties.radius(AREA_OF_EFFECT, false, TargetType.ENEMY);
	private static final ParticleContainer RAY = new ParticleContainer(Particle.FLAME)
			.count(25).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer BURST_EDGE = new ParticleContainer(Particle.FLAME)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer BURST_FILL = new ParticleContainer(Particle.LAVA)
			.count(1).spread(0.1, 0).speed(0);
	private static final Circle BURST = new Circle(AREA_OF_EFFECT);

	private final double fireDamageIncrease;

	public InfernalScepter(boolean isUpgraded) {
		super(ID, "Infernal Scepter", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, DAMAGE, 0.5, DamageType.FIRE, Sound.ITEM_FIRECHARGE_USE));
		fireDamageIncrease = isUpgraded ? 0.03 : 0.02;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta uses = new ActionMeta();
		ProjectileGroup projectileGroup = new ProjectileGroup(new InfernalRay(data, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			Player player = data.getPlayer();
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(player, data);

			if (uses.addCount(1) >= USES_PER_CORRUPTION) {
				uses.setCount(0);
				data.applyStatus(StatusType.CORRUPTION, data, 1, -1, InfernalScepter.this);
			}

			RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
					player.getEyeLocation().getDirection(), HIT_SCAN_RANGE, FluidCollisionMode.NEVER, false);
			if (result != null) {
				Location spawnLocation;
				Vector spawnVector;
				if (result.getHitBlock().isPassable()) {
					spawnLocation = result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
					spawnVector = player.getEyeLocation().getDirection();
				} else {
					double yOffset = 0.5;
					spawnVector = result.getHitBlockFace().getDirection();
					if (spawnVector.getY() > 0) {
						yOffset = 0;
					} else if (spawnVector.getY() < 0) {
						yOffset = 1;
					}
					spawnLocation = result.getHitBlock().getLocation().add(0.5, yOffset, 0.5);
					spawnLocation = spawnLocation.add(spawnVector.clone().multiply(0.75));
				}
				projectileGroup.start(data, spawnLocation, spawnVector);
			}

			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_HOE,
				"Flaming rays shoot out of the targeted surface and burst on hitting an enemy, dealing "
						+ GlossaryTag.FIRE.tag(this, DAMAGE) + " damage to enemies within "
						+ DescUtil.val(AREA_OF_EFFECT) + " blocks. For each enemy hit with at least "
						+ GlossaryTag.BURN.tag(this, BURN_THRESHOLD) + ", increase " + GlossaryTag.FIRE.tag(this)
						+ " damage by " + DescUtil.val((int) (fireDamageIncrease * 100) + "%")
						+ " for the rest of the fight. Every " + DescUtil.val(USES_PER_CORRUPTION)
						+ " uses, gain " + GlossaryTag.CORRUPTION.tag(this, 1) + ".");
	}

	private class InfernalRay extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		public InfernalRay(PlayerFightData data, int slot) {
			super(0.5, 2, 1);
			size(1.25, 1.25);
			ignore(false, true, false);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			RAY.play(data.getPlayer(), projectile.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			if (hitBarrier != null)
				return;

			Player player = data.getPlayer();
			Sounds.explode.play(player, projectile.getLocation());
			BURST.play(player, BURST_EDGE, projectile.getLocation(), LocalAxes.xz(), BURST_FILL);
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, projectile.getLocation(), TARGETS)) {
				FightData targetData = FightInstance.getFightData(target);
				boolean empowers = targetData != null && targetData.hasStatus(StatusType.BURN)
						&& targetData.getStatus(StatusType.BURN).getStacks() >= BURN_THRESHOLD;
				DamageMeta damage = new DamageMeta(data, DAMAGE, DamageType.FIRE,
						DamageStatTracker.of(id + slot, InfernalScepter.this));
				damage.setProjectileInstance(projectile);
				damage.isBasicAttack(InfernalScepter.this, true);
				FightInstance.dealDamage(damage, target);
				if (empowers) {
					data.addDamageBuff(DamageBuffType.of(DamageCategory.FIRE), Buff.multiplier(data,
							fireDamageIncrease, StatTracker.damageBuffAlly(UUID.randomUUID().toString(), InfernalScepter.this)));
				}
			}
			projectile.cancel();
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
		}
	}
}