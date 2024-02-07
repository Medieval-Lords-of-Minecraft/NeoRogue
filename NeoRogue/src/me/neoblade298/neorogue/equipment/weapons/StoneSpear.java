package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneSpear extends Equipment {
	private int throwDamage, throwCooldown = 5;
	private static final ParticleContainer throwPart = new ParticleContainer(Particle.CLOUD).count(25).spread(0.1, 0.1);
	public StoneSpear(boolean isUpgraded) {
		super("stoneSpear", "Stone Spear", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 45 : 35, 1, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		properties.addUpgrades(PropertyType.DAMAGE);
		
		throwDamage = isUpgraded ? 120 : 90;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StoneSpearInstance inst = new StoneSpearInstance(data, this, slot, es);
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			if (!inst.canTrigger(p, data)) return TriggerResult.keep();
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(p, data, ev.getTarget());
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.THROW_TRIDENT, inst);
	}
	
	private class StoneSpearInstance extends EquipmentInstance {
		private ProjectileGroup projs;
		public StoneSpearInstance(PlayerFightData pdata, Equipment eq, int slot, EquipSlot es) {
			super(pdata.getPlayer(), eq, slot, es);
			projs = new ProjectileGroup(new StoneSpearProjectile(p));
			
			action = (pdata2, in) -> {
				setCooldown(throwCooldown);
				projs.start(pdata2);
				return TriggerResult.keep();
			};
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data) {
			if (nextUsable >= System.currentTimeMillis()) {
				sendCooldownMessage(p);
				return false;
			}
			return true;
		}
	}
	
	private class StoneSpearProjectile extends Projectile {
		private Player p;
		public StoneSpearProjectile(Player p) {
			super(3, 10, 2);
			this.size(0.5, 0.5).pierce();
			this.pierce();
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj) {
			throwPart.spawn(proj.getLocation());
		}

		@Override
		public void onEnd(ProjectileInstance proj) {
			
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, new DamageMeta(proj.getOwner(), throwDamage, DamageType.PIERCING), hitBarrier);
			Location loc = hit.getEntity().getLocation();
			Util.playSound(p, loc, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F, false);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Util.playSound(p, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1F, 0.5F, false);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIDENT, "Can be thrown to deal <yellow>" + throwDamage + "</yellow> " + GlossaryTag.PIERCING.tag(this) + " "
				+ "damage, but disabling the weapon for <white>5</white> seconds.");
	}
}
