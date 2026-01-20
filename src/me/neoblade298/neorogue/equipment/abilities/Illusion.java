package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Illusion extends Equipment {
	private static final String ID = "Illusion";
	private static final ParticleContainer shadowBall = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.BLACK, 1.5F))
		.count(15).spread(0.3, 0.3);
	private static final ParticleContainer projectileParticle = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.fromRGB(50, 0, 50), 1F))
		.count(3).spread(0.2, 0.2);
	private static TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	
	private int damage, dur;

	public Illusion(boolean isUpgraded) {
		super(ID, "Illusion", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 1, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 250 : 150;
		dur = 3;
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
		
		// Trigger when applying evade
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.EVADE)) return TriggerResult.keep();
			
			// Check cooldown (1 second = 1000ms)
			if (System.currentTimeMillis() - cooldown.getTime() < 1000) {
				return TriggerResult.keep();
			}
			
			// Only fire if we have a shadow position (2 seconds have passed)
			if (locationQueue.size() < 4) return TriggerResult.keep();
			
			// Get shadow location
			Location shadowLoc = locationQueue.getFirst();
			
			// Spawn body double at shadow location
			ArmorStand as = (ArmorStand) p.getWorld().spawnEntity(shadowLoc, EntityType.ARMOR_STAND);
			PlayerDisguise dis = new PlayerDisguise(p);
			dis.setName(p.getName() + " Body Double");
			dis.setEntity(as);
			dis.startDisguise();
			
			// Taunt nearby enemies
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, shadowLoc, tp)) {
				if (!NeoRogue.mythicApi.isMythicMob(ent)) continue;
				NeoRogue.mythicApi.addThreat(ent, as, 100000);
			}
			
			// Remove body double after duration
			data.addGuaranteedTask(UUID.randomUUID(), new Runnable() {
				public void run() {
					as.remove();
				}
			}, dur * 20);
			
			// Fire projectiles at nearby enemies
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, shadowLoc, tp)) {
				Location targetLoc = ent.getEyeLocation();
				
				ProjectileGroup proj = new ProjectileGroup(new IllusionProjectile(data, slot, this));
				proj.start(data, shadowLoc, targetLoc.toVector().subtract(shadowLoc.toVector()).normalize());
			}
			
			Sounds.equip.play(p, shadowLoc);
			Sounds.fire.play(p, shadowLoc);
			cooldown.setTime(System.currentTimeMillis());
			
			return TriggerResult.keep();
		});
	}

	private class IllusionProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private int slot;
		private Equipment eq;

		public IllusionProjectile(PlayerFightData data, int slot, Equipment eq) {
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
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"Passive. Whenever you apply " + GlossaryTag.EVADE.tag(this) + ", spawn a body double at your position from " +
				"<white>2s</white> ago that fires projectiles dealing " + GlossaryTag.DARK.tag(this, damage, true) + " damage to all nearby enemies " +
				"and taunts them. The body double lasts for <white>" + dur + "s</white>.");
	}
}
