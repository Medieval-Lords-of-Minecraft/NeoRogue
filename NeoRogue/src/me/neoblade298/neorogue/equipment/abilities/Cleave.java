package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileSettings;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Cleave extends Equipment {
	private static final TargetProperties tp = new TargetProperties(7, true, TargetType.ENEMY);
	private int amount, damage;
	private ProjectileSettings[] projs;
	
	public Cleave(boolean isUpgraded) {
		super("cleave", "Cleave", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 10, tp.range));
		
		amount = isUpgraded ? 5 : 3;
		damage = isUpgraded ? 60 : 40;
		projs = new ProjectileSettings[amount];
		for (int i = 0; i < amount; i++) {
			projs[i] = new CleaveProjectile(i);
		}
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, (pd, in) -> {
			Util.playSound(p, Sound.ENTITY_PLAYER_ATTACK_SWEEP, false);
			for (int i = 0; i < projs.length; i++) {
				projs[i].start(data.getInstance(), data);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, fire <yellow>" + amount + " </yellow>projectiles in a cone in front of you that deal "
						+ "<yellow>" + damage + " </yellow>.");
	}
	
	private class CleaveProjectile extends ProjectileSettings {
		private static ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK);
		public CleaveProjectile(int i) {
			super(0.5, tp.range, 2);
			this.rotation(-45 + (22.5 * i));
		}

		@Override
		public void onTick(Projectile proj) {
			Util.playSound((Player) proj.getOwner().getEntity(), proj.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, false);
			part.spawn(proj.getLocation());
		}

		@Override
		public void onEnd(Projectile proj) {}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, Projectile proj) {
			weaponDamageProjectile(hit.getEntity(), proj);
		}
	}
}
