package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WoodenWand extends Equipment {
	private static ParticleContainer tick, explode;
	
	static {
		tick = new ParticleContainer(Particle.FLAME);
		tick.count(10).spread(0.1, 0.1).speed(0.01);
		explode = new ParticleContainer(Particle.EXPLOSION_NORMAL);
	}
	
	public WoodenWand(boolean isUpgraded) {
		super("woodenWand", "Wooden Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(5, 0, isUpgraded ? 75 : 50, 0.5, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new WoodenWandProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class WoodenWandProjectile extends Projectile {
		private Player p;
		public WoodenWandProjectile(Player p) {
			super(2, 10, 3);
			this.size(0.5, 0.5);
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj) {
			tick.spawn(proj.getLocation());
			Util.playSound(p, proj.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, true);
		}

		@Override
		public void onEnd(ProjectileInstance proj) {
			
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			weaponDamageProjectile(hit.getEntity(), proj, hitBarrier);
			Location loc = hit.getEntity().getLocation();
			Util.playSound(p, loc, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, true);
			explode.spawn(loc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_HOE);
	}
}
