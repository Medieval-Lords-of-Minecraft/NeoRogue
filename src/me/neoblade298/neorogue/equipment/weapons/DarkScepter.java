package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DarkScepter extends Equipment {
	private static ParticleContainer tick;
	
	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}
	
	public DarkScepter(boolean isUpgraded) {
		super(
				"darkScepter", "Dark Scepter", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(6, 0, isUpgraded ? 50 : 25, 0.65, DamageType.DARK, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new DarkScepterProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class DarkScepterProjectile extends Projectile {
		private Player p;
		private boolean blocked = false;

		public DarkScepterProjectile(Player p) {
			super(4, 15, 1);
			this.size(0.5, 0.5).ignore(false, false, true);
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
		}

		@Override
		public void onEnd(ProjectileInstance proj) {
			if (blocked)
				return;

			new ProjectileGroup(new DarkRayProjectile(p)).start(proj.getOwner());
			// TODO: need to start ray projectile where scepter projectile ended (i.e. at hit block)
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			if (hitBarrier != null)
				blocked = true;
		}

		@Override
		public void onStart(ProjectileInstance proj) {
		}
	}
	
	private class DarkRayProjectile extends Projectile {
		public DarkRayProjectile(Player p) {
			super(0.5, 2.5, 1);
			this.size(1.25, 1.25).pierce();
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			tick.spawn(proj.getLocation());
		}

		@Override
		public void onEnd(ProjectileInstance proj) {
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			weaponDamageProjectile(hit.getEntity(), proj, hitBarrier);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Dark rays shoot out of a surface hit.");
	}
}
