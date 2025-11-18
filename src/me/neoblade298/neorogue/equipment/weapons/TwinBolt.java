package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
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

public class TwinBolt extends Equipment {
	private static final String ID = "TwinBolt";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int damage, burn;

	public TwinBolt(boolean isUpgraded) {
		super(ID, "Twin Bolt", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 0, 9, 10));
		damage = isUpgraded ? 150 : 100;
		burn = isUpgraded ? 120 : 80;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new TwinBoltProjectile(data, true, slot, this),
				new TwinBoltProjectile(data, false, slot, this));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20).then(new Runnable() {
				public void run() {
					Sounds.fire.play(p, p);
					proj.start(data);
				}
			});
			return TriggerResult.keep();
		}));
	}

	private class TwinBoltProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private boolean left;
		private int slot;
		private Equipment eq;
		private static double ANGLE = Math.toRadians(1.6);

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public TwinBoltProjectile(PlayerFightData data, boolean left, int slot, Equipment eq) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.data = data;
			this.p = data.getPlayer();
			this.left = left;
			this.slot = slot;
			this.eq = eq;

			if (left) {
				this.rotation(-30);
			} else {
				this.rotation(30);
			}
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
			int tick = proj.getTick();
			if (tick <= 6) {
				proj.getVelocity().rotateAroundY(left ? ANGLE : -ANGLE);
			} else if (tick > 6 && tick <= 9) {
				proj.getVelocity().rotateAroundY(left ? ANGLE : -ANGLE);
			}
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (hit.hasStatus("TWINBOLT-" + p.getName())) {
				// If the hit already has a twinbolt status, apply burn
				hit.applyStatus(StatusType.BURN, data, burn, -1);
				Sounds.extinguish.play(p, hit.getEntity().getLocation());
			} else {
				// If the hit does not have a twinbolt status, create it
				hit.applyStatus(
						Status.createByGenericType(GenericStatusType.BASIC, "TWINBOLT-" + p.getName(), hit, true), data,
						1, 20);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CAMPFIRE,
				"On cast, " + DescUtil.charge(this, 1, 1)
						+ " before firing two projectiles that split and then curve back inwards. They deal "
						+ GlossaryTag.FIRE.tag(this, damage, true)
						+ " damage. Additionally, if both projectiles hit the same enemy, apply "
						+ GlossaryTag.BURN.tag(this, burn, false) + ".");
	}
}
