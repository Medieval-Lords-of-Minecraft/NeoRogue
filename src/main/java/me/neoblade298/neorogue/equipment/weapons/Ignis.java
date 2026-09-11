package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Ignis extends Bow {
	private static final String ID = "Ignis";
	private static final int SHOT_THRESHOLD = 3;
	private static final double CURVE_ANGLE = Math.toRadians(1.6);
	private static final ParticleContainer FLAME = new ParticleContainer(Particle.FLAME);
	private int damage, burn;

	public Ignis(boolean isUpgraded) {
		super(ID, "Ignis", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.WEAPON,
				EquipmentProperties.ofBow(isUpgraded ? 70 : 60, 1, 0, 12, 4, 1));
		damage = isUpgraded ? 50 : 30;
		burn = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta shots = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			Vector arrowVelocity = ((ProjectileLaunchEvent) in).getEntity().getVelocity();
			if (!canShoot(data, arrowVelocity)) return TriggerResult.keep();
			useBow(data);

			ProjectileGroup primary = new ProjectileGroup(new BowProjectile(data, arrowVelocity, this, id + slot));
			if (shots.addCount(1) >= SHOT_THRESHOLD) {
				shots.setCount(0);
				data.addAftershot(new ProjectileGroup(new IgnisProjectile(data, true, slot),
						new IgnisProjectile(data, false, slot)));
			}
			primary.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "Every " + DescUtil.val(SHOT_THRESHOLD)
				+ " basic attacks, also fire two " + GlossaryTag.AFTERSHOT.tagPlural(this)
				+ " that split outward and curve back inward. Each deals "
				+ GlossaryTag.FIRE.tag(this, damage)
				+ " damage, and if both hit the same enemy, apply " + GlossaryTag.BURN.tag(this, burn) + ".");
	}

	private class IgnisProjectile extends Projectile {
		private final PlayerFightData data;
		private final boolean left;
		private final int slot;

		private IgnisProjectile(PlayerFightData data, boolean left, int slot) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.left = left;
			this.slot = slot;
			rotation(left ? -30 : 30);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			FLAME.play(data.getPlayer(), proj.getLocation());
			if (proj.getTick() <= 9) {
				proj.getVelocity().rotateAroundY(left ? CURVE_ANGLE : -CURVE_ANGLE);
			}
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Player p = data.getPlayer();
			String statusId = ID.toUpperCase() + "-" + p.getName();
			if (hit.hasStatus(statusId)) {
				hit.applyStatus(StatusType.BURN, data, burn, -1, Ignis.this);
				Sounds.extinguish.play(p, hit.getEntity().getLocation());
			} else {
				hit.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, statusId, hit, true), data,
						1, 20, Ignis.this);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			if (left) Sounds.fire.play(data.getPlayer(), data.getPlayer());
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE,
					DamageStatTracker.of(id + slot, Ignis.this)));
		}
	}
}