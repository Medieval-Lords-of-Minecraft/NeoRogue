package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Quickfire extends Equipment {
	private static final String ID = "quickfire";
	private int damage;
	
	public Quickfire(boolean isUpgraded) {
		super(ID, "Quickfire", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(5, 5, 8, 6));
		properties.addUpgrades(PropertyType.RANGE);
		damage = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (d, inputs) -> {
			ProjectileGroup proj = new ProjectileGroup(new QuickfireProjectile(data));
			proj.start(data);
			return TriggerResult.keep();
		}), Bow.needsAmmo);
	}
	
	private class QuickfireProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private AmmunitionInstance ammo;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public QuickfireProjectile(PlayerFightData data) {
			super(properties.get(PropertyType.RANGE), 1);
			this.gravity(0.03);
			this.size(1, 1);
			this.data = data;
			this.p = data.getPlayer();
			this.ammo = data.getAmmoInstance();

			blocksPerTick(2);
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, hitBarrier);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = ammoProps.get(PropertyType.DAMAGE) + damage;
			dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
			ammo.onStart(proj);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIREWORK_ROCKET, "Instantly fire a projectile that deals " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage using "
			+ "your current ammunition.");
	}
}
