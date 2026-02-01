package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
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
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class NightSurge extends Equipment {
	private static final String ID = "NightSurge";
	private static final ParticleContainer projectileParticle = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.fromRGB(40, 0, 80), 1.2F))
		.count(5).spread(0.2, 0.2);
	
	private int damage;

	public NightSurge(boolean isUpgraded) {
		super(ID, "Night Surge", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 200 : 150;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		String cooldownStatus = p.getName() + "-nightsurge";
		
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			
			// Check if damage contains dark damage type
			if (!ev.getMeta().containsType(DamageType.DARK)) return TriggerResult.keep();
			
			// Get target fight data
			FightData targetFd = FightInstance.getFightData(ev.getTarget());
			if (targetFd == null) return TriggerResult.keep();
			
			// Check if target has insanity
			if (!targetFd.hasStatus(StatusType.INSANITY)) return TriggerResult.keep();
			
			// Check cooldown (2 seconds per enemy)
			if (targetFd.hasStatus(cooldownStatus)) return TriggerResult.keep();
			
			// Apply cooldown marker
			Status s = Status.createByGenericType(GenericStatusType.BASIC, cooldownStatus, targetFd, true);
			targetFd.applyStatus(s, data, 1, 40); // 40 ticks = 2 seconds
			
			// Get target entity and location
			if (!(ev.getTarget() instanceof LivingEntity)) return TriggerResult.keep();
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(ID + slot, this)));
			LivingEntity targetEntity = (LivingEntity) ev.getTarget();
			Location targetLoc = targetEntity.getLocation();
			
			// Fire projectile from the target, opposite direction they're facing
			Vector targetDirection = targetEntity.getLocation().getDirection();
			Vector projectileDirection = targetDirection.clone().multiply(-1).normalize();
			
			// Spawn location slightly behind
			Location spawnLoc = targetLoc.clone().add(projectileDirection.clone().multiply(1.5));
			
			ProjectileGroup proj = new ProjectileGroup(new NightSurgeProjectile(data, slot, this));
			proj.start(data, spawnLoc, projectileDirection);
			
			Sounds.fire.play(p, spawnLoc);
			
			return TriggerResult.keep();
		});
	}

	private class NightSurgeProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int slot;
		private Equipment eq;

		public NightSurgeProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(2.0, 15, 1); // Speed, range, piercing
			this.size(0.4, 0.4);
			this.initialY(1);
			this.data = data;
			this.slot = slot;
			this.p = data.getPlayer();
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			projectileParticle.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Damage already applied via onStart
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(ID + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				"Passive. Dealing " + GlossaryTag.DARK.tag(this) + " damage to an enemy with " +
				GlossaryTag.INSANITY.tag(this) + " spawns a projectile from them that fires " +
				"opposite the direction they're facing, dealing " + GlossaryTag.DARK.tag(this, damage, true) + 
				" damage. The target hit also takes this damage. <white>2s</white> cooldown per enemy.");
	}
}
