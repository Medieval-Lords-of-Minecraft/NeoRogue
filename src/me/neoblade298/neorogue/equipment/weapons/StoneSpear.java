package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneSpear extends Equipment {
	private static final String ID = "stoneSpear";
	private int damage, throwDamage, throwCooldown = 5;
	private static final ParticleContainer throwPart = new ParticleContainer(Particle.CLOUD);
	private static final TargetProperties spearHit = TargetProperties.line(4, 1, TargetType.ENEMY);

	public StoneSpear(boolean isUpgraded) {
		super(
				ID, "Stone Spear", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 50 : 40, 0.5, 0.5, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		
		damage = (int) properties.get(PropertyType.DAMAGE);
		throwDamage = isUpgraded ? 120 : 90;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StoneSpearInstance inst = new StoneSpearInstance(data, this, slot, es);
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			if (!inst.canTrigger(p, data, in))
				return TriggerResult.keep();
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwing(p, data);
			weaponDamage(p, data, ev.getTarget(), damage + data.getStatus(StatusType.STRENGTH).getStacks() * 2);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_NO_HIT, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			if (!inst.canTrigger(p, data, in))
				return TriggerResult.keep();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, spearHit);
			if (targets.isEmpty())
				return TriggerResult.keep();
			weaponSwing(p, data);
			weaponDamage(p, data, targets.getFirst(), damage + data.getStatus(StatusType.STRENGTH).getStacks() * 2);
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.THROW_TRIDENT, inst);
	}
	
	private class StoneSpearInstance extends EquipmentInstance {
		private ProjectileGroup projs;

		public StoneSpearInstance(PlayerFightData pdata, Equipment eq, int slot, EquipSlot es) {
			super(pdata, eq, slot, es);
			projs = new ProjectileGroup(new StoneSpearProjectile(p));
			
			action = (pdata2, in) -> {
				setCooldown(throwCooldown);
				projs.start(pdata2);
				return TriggerResult.keep();
			};
		}
		
		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
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
			super(1, 15, 1);
			this.size(0.5, 0.5).pierce(-1).gravity(0.02).initialY(1);
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			throwPart.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.explode.play(p, hit.getEntity().getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.threw.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), throwDamage + proj.getOwner().getStatus(StatusType.STRENGTH).getStacks() * 2, DamageType.PIERCING));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.TRIDENT,
				"Melee range +1. Can be thrown to deal <yellow>" + throwDamage + "</yellow> " + GlossaryTag.PIERCING.tag(this) + " "
						+ "damage to all enemies hit, but disabling the weapon for <white>5</white> seconds. "
						+ "Basic attacks and throw damage are affected by strength <white>3x</white>."
		);
	}
}
