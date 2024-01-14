package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
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
				EquipmentProperties.ofWeapon(isUpgraded ? 75 : 50, 0.5, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.setManaCost(5);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, new EquipmentInstance(this, slot, (d, inputs) -> {
			weaponSwing(p, data);
			new WoodenWandProjectile(p, 0.5, 10, 3, false, false, false, false, 0, 0, data.getInstance(), data, 0.2, 0.2, 0.2);
			return TriggerResult.keep();
		}));
	}
	
	private class WoodenWandProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		public WoodenWandProjectile(LivingEntity origin, double blocksPerTick, double maxRange, int tickSpeed,
				boolean pierce, boolean ignoreBarriers, boolean ignoreBlocks, boolean ignoreEntities, double yRotate, double gravity,
				FightInstance inst, PlayerFightData owner, double x, double y, double z) {
			super(origin, blocksPerTick, maxRange, tickSpeed, pierce, ignoreBarriers, ignoreBlocks, ignoreEntities, yRotate, gravity, inst, owner,
					x, y, z);
			this.data = owner;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick() {
			tick.spawn(loc);
			Util.playSound(p, loc, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, true);
		}

		@Override
		public void onEnd() {
			
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier) {
			double finalDamage = properties.getDamage();
			if (hitBarrier != null) {
				finalDamage = hitBarrier.applyDefenseBuffs(finalDamage, properties.getType());
			}
			weaponDamage(p, data, p, finalDamage);
			Location loc = hit.getEntity().getLocation();
			Util.playSound(p, loc, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, true);
			explode.spawn(loc);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_HOE);
	}
}
