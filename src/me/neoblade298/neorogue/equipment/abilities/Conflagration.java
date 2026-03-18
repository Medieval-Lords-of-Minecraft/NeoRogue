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
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreKillEvent;

public class Conflagration extends Equipment {
	private static final String ID = "Conflagration";
	private static final TargetProperties tp = TargetProperties.radius(12, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 100, 0), 1F))
			.count(5).spread(0.1, 0.1);
	
	private int damage;
	private double burnMult;

	public Conflagration(boolean isUpgraded) {
		super(ID, "Conflagration", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none().add(PropertyType.RANGE, tp.range));
		damage = 150;
		burnMult = isUpgraded ? 1.5 : 1.0;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_KILL, (pdata, in) -> {
			PreKillEvent ev = (PreKillEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			
			// Check if killed by burn damage
			if (!fd.hasStatus(StatusType.BURN)) {
				return TriggerResult.keep();
			}
			
			Player p = data.getPlayer();
			LivingEntity killed = ev.getTarget();
			Location killedLoc = killed.getLocation().add(0, 1, 0);
			
			// Get the burn stacks the killed enemy had
			int burnStacks = fd.getStatus(StatusType.BURN).getStacks();
			
			// Find nearest enemy from killed location
			LivingEntity nearest = TargetHelper.getNearest(killed, tp);
			if (nearest == null) return TriggerResult.keep();
			
			// Calculate direction from killed enemy to nearest enemy
			Vector direction = nearest.getLocation().toVector()
					.subtract(killedLoc.toVector()).normalize();
			
			// Fire projectile
					ProjectileGroup proj = new ProjectileGroup(
new ConflagrationProjectile(data, Conflagration.this, slot, burnStacks, nearest, killed));
					proj.start(data, killedLoc, direction);
					
					Sounds.fire.play(p, killedLoc);
			
			return TriggerResult.keep();
		});
	}

	private class ConflagrationProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private int burnToApply;
		private LivingEntity homingTarget;
		private LivingEntity source;

		public ConflagrationProjectile(PlayerFightData data, Equipment eq, int slot, int originalBurn, LivingEntity homingTarget, LivingEntity source) {
			super(1, 15, 1);
			this.size(0.2, 0.2);
			this.homing(0.015);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.burnToApply = (int) (originalBurn * burnMult);
			this.homingTarget = homingTarget;
			this.source = source;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			if (target.equals(source)) {
				proj.addPierce(1);
				return;
			}
			
			// Deal damage
			meta.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, 
					DamageStatTracker.of(ID + slot, eq)));
			
			// Apply burn
			FightInstance.applyStatus(target, StatusType.BURN, data, burnToApply, -1);
			
			Sounds.explode.play(p, target.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.setHomingTarget(homingTarget);
		}
	}

	@Override
	public void setupItem() {
		String burnMultStr = burnMult == 1.0 ? "<yellow>1x</yellow>" : "<yellow>1.5x</yellow>";
		item = createItem(Material.FIRE_CHARGE,
				"Passive. Killing an enemy with " + GlossaryTag.BURN.tag(this) + " causes that enemy to fire a projectile " +
				"at the nearest enemy that deals " + DescUtil.yellow(damage) + " " + GlossaryTag.FIRE.tag(this) + 
				" damage and applies " + burnMultStr + " of its " + GlossaryTag.BURN.tag(this) + ".");
	}
}
