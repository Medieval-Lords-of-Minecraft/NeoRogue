package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleUtil;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightData;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class WoodenWand extends Weapon {
	public WoodenWand(boolean isUpgraded) {
		super("woodenWand", isUpgraded, Rarity.COMMON);
		display = "Wooden Wand";
		damage = isUpgraded ? 10 : 7;
		type = DamageType.FIRE;
		attackSpeed = 0.5;
		item = Weapon.createItem(this, Material.WOODEN_HOE, null, null);
	}

	@Override
	public void initialize(Player p, FightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, (inputs) -> {
			new WoodenWandProjectile(p, 0.5, 10, 3, false, false, false, false, 0, 0, data.getInstance(), data, 0.2, 0.2, 0.2);
			return true;
		});

		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_NO_HIT, (inputs) -> {
			new WoodenWandProjectile(p, 1, 10, 2, false, false, false, false, 0, 0, data.getInstance(), data, 0.5, 0.2, 0.5);
			return true;
		});
	}
	
	private class WoodenWandProjectile extends Projectile {
		FightData data;
		Player p;
		public WoodenWandProjectile(LivingEntity origin, double blocksPerTick, double maxRange, int tickSpeed,
				boolean pierce, boolean ignoreBarriers, boolean ignoreBlocks, boolean ignoreEntities, double yRotate, double gravity,
				FightInstance inst, FightData owner, double x, double y, double z) {
			super(origin, blocksPerTick, maxRange, tickSpeed, pierce, ignoreBarriers, ignoreBlocks, ignoreEntities, yRotate, gravity, inst, owner,
					x, y, z);
			this.data = owner;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick() {
			ParticleUtil.spawnParticle(p, true, loc, Particle.FLAME, 10, 0.1, 0.1, 0.1, 0.01);
			Util.playSound(p, loc, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, true);
		}

		@Override
		public void onEnd() {
			
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier) {
			double finalDamage = damage;
			if (hitBarrier != null) {
				finalDamage = hitBarrier.applyDefenseBuffs(finalDamage, type);
			}
			FightInstance.dealDamage(p, type, finalDamage, hit.getEntity());
			data.runActions(Trigger.BASIC_ATTACK, new Object[] { p, hit.getEntity() });
			Util.playSound(p, hit.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, true);
			ParticleUtil.spawnParticle(p, true, hit.getEntity().getLocation(), Particle.EXPLOSION_NORMAL, 2, 0.1, 0.1, 0.1, 0);
		}
	}
}
