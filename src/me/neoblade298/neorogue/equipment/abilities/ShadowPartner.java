package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class ShadowPartner extends Equipment {
	private static final String ID = "ShadowPartner";
	private static final ParticleContainer shadowBall = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.BLACK, 1.5F))
		.count(15).spread(0.3, 0.3);
	private static final ParticleContainer projectileParticle = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.fromRGB(50, 0, 50), 1F))
		.count(3).spread(0.2, 0.2);
	
	private int damage;

	public ShadowPartner(boolean isUpgraded) {
		super(ID, "Shadow Partner", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 1, 0));
		damage = isUpgraded ? 250 : 150;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Queue to track player positions - stores last 4 positions (2 seconds worth at 0.5s intervals)
		LinkedList<Location> locationQueue = new LinkedList<Location>();
		ActionMeta cooldown = new ActionMeta();
		
		// Task to track player position every half second and display shadow ball
		data.addTask(new BukkitRunnable() {
			public void run() {
				// Add current location to queue
				locationQueue.add(p.getLocation().clone());
				
				// Keep queue at exactly 4 positions (2 seconds)
				if (locationQueue.size() > 4) {
					locationQueue.removeFirst();
				}
				
				// Display shadow ball at the position from 2 seconds ago
				if (locationQueue.size() == 4) {
					Location shadowLoc = locationQueue.getFirst();
					shadowBall.play(p, shadowLoc);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L)); // Run every half second (10 ticks)
		
		// Trigger when applying insanity
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY)) return TriggerResult.keep();
			
			// Check cooldown (1 second = 1000ms)
			if (System.currentTimeMillis() - cooldown.getTime() < 1000) {
				return TriggerResult.keep();
			}
			
			// Only fire if we have a shadow position (2 seconds have passed)
			if (locationQueue.size() < 4) return TriggerResult.keep();
			
			// Get the target that received insanity
			FightData target = ev.getTarget();
			if (!(target.getEntity() instanceof LivingEntity)) return TriggerResult.keep();
			LivingEntity targetEntity = (LivingEntity) target.getEntity();
			
			// Fire projectile from shadow ball location
			Location shadowLoc = locationQueue.getFirst();
			Location targetLoc = targetEntity.getEyeLocation();
			
			ProjectileGroup proj = new ProjectileGroup(new ShadowProjectile(data, slot, this));
			proj.start(data, shadowLoc, targetLoc.toVector().subtract(shadowLoc.toVector()).normalize());
			
			Sounds.fire.play(p, shadowLoc);
			cooldown.setTime(System.currentTimeMillis());
			
			return TriggerResult.keep();
		});
	}

	private class ShadowProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int slot;
		private Equipment eq;

		public ShadowProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(1.5, 20, 1); // Speed, range, piercing
			this.size(0.4, 0.4);
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
	public void setupReforges() {
		addReforge(Obfuscation.get(), ShadowPartner2.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_PEARL,
				"A ball of darkness follows <white>2s</white> behind you. Anytime you apply " +
				GlossaryTag.INSANITY.tag(this) + " <white>(1s cooldown)</white>, the ball fires a projectile at them, dealing " +
				GlossaryTag.DARK.tag(this, damage, true) + " damage on hit.");
	}
}
