package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
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

public class LightningWand extends Equipment {
	private static ParticleContainer tick;

	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(1).spread(0.1, 0.1).speed(0.01);
	}

	public LightningWand(boolean isUpgraded) {
		super(
				"lightningWand", "Lightning Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(5, 0, isUpgraded ? 30 : 20, 0.4, DamageType.LIGHTNING, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new LightningWandProjectile(p));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	private class LightningWandProjectile extends Projectile {
		private Player p;
		private int piercesLeft;
		
		public LightningWandProjectile(Player p) {
			super(4, 12, 1);
			this.size(0.5, 0.5).pierce();
			this.p = p;
			this.piercesLeft = 3;
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
			Location loc = hit.getEntity().getLocation();
			if (--piercesLeft > 0) {
				Util.playSound(p, loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1F, 1F, true);
			} else {
				Util.playSound(p, loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1F, 1F, true);
				proj.cancel(true);
			}
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Pierces the first 3 enemies hit.");
	}
}
