package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Ricochet extends Equipment {
	private static final String ID = "Ricochet";
	private static final int DISTANCE = 5;
	private static final TargetProperties tp = TargetProperties.radius(12, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 215, 0), 1.2F))
			.count(6).spread(0.15, 0.15);
	
	private int damage;

	public Ricochet(boolean isUpgraded) {
		super(ID, "Ricochet", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 10, 0, 0));
		damage = isUpgraded ? 180 : 120;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in2;
				
				if (!ev.isProjectile()) return TriggerResult.keep();
				
				Player p2 = data.getPlayer();
				LivingEntity target = ev.getTarget();
				if (target == null) return TriggerResult.keep();
				
				// Check if target is within 5 blocks of the projectile origin
				if (ev.getProjectile().getOrigin().distance(target.getLocation()) > DISTANCE) {
					return TriggerResult.keep();
				}
				
				// Find nearest enemy from the target's location
				Location targetLoc = target.getEyeLocation();
				LivingEntity nearest = TargetHelper.getNearest(target, tp);
				
				// Don't ricochet if there's no other enemy
				if (nearest == null || nearest == target) return TriggerResult.keep();
				
				// Calculate direction from hit target to nearest enemy
				Vector direction = nearest.getEyeLocation().toVector()
						.subtract(targetLoc.toVector()).normalize();
				
				// Fire ricochet projectile
				ProjectileGroup proj = new ProjectileGroup(
						new RicochetProjectile(data, Ricochet.this, slot, target));
				proj.start(data, targetLoc, direction);
				
				Sounds.shoot.play(p2, targetLoc);
				
				return TriggerResult.keep();
			});
			
			return TriggerResult.remove();
		}));
	}

	private class RicochetProjectile extends Projectile {
		private PlayerFightData data;
		private Equipment eq;
		private int slot;
		private LivingEntity originalTarget;

		public RicochetProjectile(PlayerFightData data, Equipment eq, int slot, LivingEntity originalTarget) {
			super(1, 12, 1);
			this.size(0.4, 0.4);
			this.homing(0.02);
			this.data = data;
			this.eq = eq;
			this.slot = slot;
			this.originalTarget = originalTarget;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Ignore the original target by piercing through it
			if (target.equals(originalTarget)) {
				proj.addPierce(1);
				return;
			}
			
			// Deal damage to other targets
			meta.addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, 
					DamageStatTracker.of(ID + slot, eq)));
			
			Sounds.anvil.play(data.getPlayer(), target.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			// Set homing target to nearest enemy
			LivingEntity nearest = TargetHelper.getNearest(data.getPlayer(), proj.getLocation(), tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW,
				GlossaryTag.POWER.tag(this) + ". Dealing basic attack damage to an enemy within " + DescUtil.white(DISTANCE) + " blocks " +
				"fires a projectile from that target to the nearest enemy, dealing " + 
				DescUtil.yellow(damage) + " damage.");
	}
}
