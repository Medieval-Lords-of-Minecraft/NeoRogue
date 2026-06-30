package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class CorpseExplosion extends Equipment implements Power {
	private static final String ID = "CorpseExplosion";
	private int poisonPerSecond, duration, poisonDuration;
	private static final int radius = 4;
	private static final ParticleContainer edge = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.GREEN, 1F))
		.count(1).spread(0, 0).speed(0.1);
	private static final ParticleContainer fill = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.GREEN, 1F))
		.count(1).spread(0.1, 0).speed(0.1);
	private static final ParticleContainer shootParticle = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.GREEN, 1F))
		.count(3).spread(0.2, 0.2).speed(0.05);
	private static final Circle circ = new Circle(radius);
	private static final TargetProperties tp = TargetProperties.radius(radius, false, TargetType.ENEMY);
	
	public CorpseExplosion(boolean isUpgraded) {
		super(ID, "Corpse Explosion", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0, radius));
		poisonPerSecond = isUpgraded ? 50 : 40;
		duration = 3;
		poisonDuration = 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 5) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
	
	private void animatePoisonArc(PlayerFightData data, Location start, Location end) {
		data.addTask(new BukkitRunnable() {
			private int ticks = 0;
			private static final int TOTAL_TICKS = 15; // Animation duration
			
			public void run() {
				Player p = data.getPlayer();
				double progress = (double) ticks / TOTAL_TICKS;
				
				// Linear interpolation for horizontal movement
				Vector direction = end.toVector().subtract(start.toVector());
				Location current = start.clone().add(direction.clone().multiply(progress));
				
				// Parabolic arc for vertical movement (peaks at midpoint)
				double arcHeight = 2.0; // Height of arc
				double yOffset = 4 * arcHeight * progress * (1 - progress); // Parabola formula
				current.add(0, yOffset, 0);
				
				// Display particles
				shootParticle.play(p, current);
				
				ticks++;
				if (ticks > TOTAL_TICKS) {
					this.cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 1L));
	}
	
	private void scheduleCircleEffects(Player p, PlayerFightData data, Location center) {
		// Apply poison every second for duration
		for (int second = 0; second < duration; second++) {
			final int currentSecond = second;
			data.addTask(new BukkitRunnable() {
				public void run() {
					
					// Show particles in circle shape
					circ.play(edge, center, LocalAxes.xz(), fill);
					
					// Play sound on first circle appearance
					if (currentSecond == 0) {
						Sounds.water.play(p, center);
					}
					
					// Apply poison to all enemies in radius using TargetHelper
					for (LivingEntity le : TargetHelper.getEntitiesInRadius(p, center, tp)) {
						FightInstance.applyStatus(le, StatusType.POISON, data, poisonPerSecond, poisonDuration, CorpseExplosion.this);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20L * (second + 1))); // Schedule for each second
		}
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.KILL, (pdata2, inputs2) -> {
			KillEvent killEv = (KillEvent) inputs2;
			LivingEntity killed = killEv.getTarget();

			// Check if the enemy was killed by poison damage
			if (killEv.getDamageMeta() == null || !killEv.getDamageMeta().containsType(DamageType.POISON)) {
				return TriggerResult.keep();
			}

			Location killedLoc = killed.getLocation().add(0, 1, 0);

			// Create two poison circles near the killed enemy
			for (int i = 0; i < 2; i++) {
				// Random offset for circle placement (within 2 blocks of the killed enemy)
				double angle = Math.random() * 2 * Math.PI;
				double distance = 2 + Math.random() * 4; // 2-6 blocks away
				double offsetX = Math.cos(angle) * distance;
				double offsetZ = Math.sin(angle) * distance;

				Location circleCenter = killedLoc.clone().add(offsetX, 0, offsetZ);

				// Animate poison shooting out in a parabolic arc
				Player p2 = data.getPlayer();
				animatePoisonArc(data, killedLoc.clone(), circleCenter);
				scheduleCircleEffects(p2, data, circleCenter);
			}

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE, 
			GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.POISON.tag(this) + " " + DescUtil.white(5) + " times. When you kill an enemy with " + GlossaryTag.POISON.tag(this) + " damage, spawn " + DescUtil.white(2) + " poison circles near the corpse. " +
			"Each circle lasts " + DescUtil.white(duration) + " seconds and applies " + 
			GlossaryTag.POISON.tag(this, poisonPerSecond, true) + " [" + DescUtil.white(poisonDuration / 20 + "s") + "] per second to nearby enemies.");
	}
}
