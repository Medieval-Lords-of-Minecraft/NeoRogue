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
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class Conflagration extends Equipment {
	private static final String ID = "Conflagration";
	private static final TargetProperties tp = TargetProperties.radius(20, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 100, 0), 1.5F))
			.count(8).spread(0.2, 0.2);
	
	private int damage;
	private double burnMult;

	public Conflagration(boolean isUpgraded) {
		super(ID, "Conflagration", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = 150;
		burnMult = isUpgraded ? 1.5 : 1.0;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			
			// Check if killed by burn damage
			if (ev.getDamageMeta() == null || !FightInstance.getFightData(ev.getTarget()).hasStatus(StatusType.BURN)) {
				return TriggerResult.keep();
			}
			
			Player p = data.getPlayer();
			LivingEntity killed = ev.getTarget();
			Location killedLoc = killed.getLocation().add(0, 1, 0);
			
			// Get the burn stacks the killed enemy had
			int burnStacks = FightInstance.getFightData(killed).getStatus(StatusType.BURN).getStacks();
			if (burnStacks <= 0) return TriggerResult.keep();
			
			// Find nearest enemy from killed location
			LivingEntity nearest = TargetHelper.getNearest(p, killedLoc, tp);
			if (nearest == null) return TriggerResult.keep();
			
			// Calculate direction from killed enemy to nearest enemy
			Vector direction = nearest.getEyeLocation().toVector()
					.subtract(killedLoc.toVector()).normalize();
			
			// Fire projectile
			ProjectileGroup proj = new ProjectileGroup(
					new ConflagrationProjectile(data, this, slot, burnStacks));
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

		public ConflagrationProjectile(PlayerFightData data, Equipment eq, int slot, int originalBurn) {
			super(1, 15, 2);
			this.size(0.5, 0.5);
			this.homing(0.015);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.burnToApply = (int) (originalBurn * burnMult);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Deal damage
			meta.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE, 
					DamageStatTracker.of(ID + slot, eq)));
			
			// Apply burn
			FightInstance.applyStatus(target, StatusType.BURN, data, burnToApply, -1);
			
			Sounds.explode.play(p, target.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			// Set homing target to nearest enemy
			LivingEntity nearest = TargetHelper.getNearest(p, proj.getLocation(), tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
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
