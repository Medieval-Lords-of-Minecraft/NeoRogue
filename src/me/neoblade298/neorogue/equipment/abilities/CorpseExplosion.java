package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class CorpseExplosion extends Equipment {
	private static final String ID = "CorpseExplosion";
	private int poisonPerSecond, duration;
	private static final int radius = 4;
	private static final ParticleContainer pc = new ParticleContainer(Particle.SPORE_BLOSSOM_AIR)
		.count(20).spread(1, 0.5).speed(0.1);
	private static final Circle circ = new Circle(radius);
	private static final TargetProperties tp = TargetProperties.radius(radius, false, TargetType.ENEMY);
	
	public CorpseExplosion(boolean isUpgraded) {
		super(ID, "Corpse Explosion", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0, tp.range));
		poisonPerSecond = isUpgraded ? 75 : 50;
		duration = 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.KILL, (pdata, inputs) -> {
			KillEvent ev = (KillEvent) inputs;
			LivingEntity killed = ev.getTarget();
			
			// Check if the enemy was killed by poison damage
			if (ev.getDamageMeta() == null || !ev.getDamageMeta().containsType(DamageCategory.POISON)) {
				return TriggerResult.keep();
			}
			
			Location killedLoc = killed.getLocation();
			
			// Create two poison circles near the killed enemy
			for (int i = 0; i < 2; i++) {
				// Random offset for circle placement (within 2 blocks of the killed enemy)
				double angle = Math.random() * 2 * Math.PI;
				double distance = 1 + Math.random() * 2; // 1-3 blocks away
				double offsetX = Math.cos(angle) * distance;
				double offsetZ = Math.sin(angle) * distance;
				
				Location circleCenter = killedLoc.clone().add(offsetX, 0, offsetZ);
				
				// Schedule poison application for duration
				scheduleCircleEffects(p, data, circleCenter);
			}
			
			return TriggerResult.keep();
		});
	}
	
	private void scheduleCircleEffects(Player p, PlayerFightData data, Location center) {
		// Apply poison every second for duration
		for (int second = 0; second < duration; second++) {
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (!p.isOnline() || data.getInstance() == null) return;
					
					// Show particles in circle shape
					circ.play(pc, center, LocalAxes.xz(), null);
					
					// Apply poison to all enemies in radius using TargetHelper
					for (LivingEntity le : TargetHelper.getEntitiesInRadius(p, center, tp)) {
						FightInstance.applyStatus(le, StatusType.POISON, data, poisonPerSecond, 20); // 1 second duration
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20L * (second + 1))); // Schedule for each second
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE, 
			"When you kill an enemy with " + GlossaryTag.POISON.tag(this) + ", spawn <white>2</white> poison circles near the corpse. " +
			"Each circle lasts <white>" + duration + "</white> seconds and applies " + 
			GlossaryTag.POISON.tag(this, poisonPerSecond, true) + " per second to nearby enemies.");
	}
}