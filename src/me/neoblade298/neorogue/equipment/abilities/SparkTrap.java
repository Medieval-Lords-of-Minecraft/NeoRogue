package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
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

public class SparkTrap extends Equipment {
	private static final String ID = "SparkTrap";
	private static final ParticleContainer trapParticle = new ParticleContainer(Particle.CLOUD)
		.count(10).spread(0.5, 0.5);
	private static final ParticleContainer explosionParticle = new ParticleContainer(Particle.FIREWORK)
		.count(30).spread(2, 1);
	private static final ParticleContainer lineParticle = new ParticleContainer(Particle.ELECTRIC_SPARK)
		.count(5).spread(0.2, 0.2);
	private static final TargetProperties radiusProps = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final TargetProperties lineProps = TargetProperties.line(8, 2, TargetType.ENEMY);
	
	private int explosionDamage = 100;
	private int lineDamage;

	public SparkTrap(boolean isUpgraded) {
		super(ID, "Spark Trap", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0, radiusProps.range));
		lineDamage = isUpgraded ? 300 : 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		SparkTrapInstance inst = new SparkTrapInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
	}

	private class SparkTrapInstance extends EquipmentInstance {
		private Location trapLocation = null;
		private boolean isInitialCast = true;
		
		public SparkTrapInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
				SparkTrapInstance inst = this;
				
				// Recast: teleport to bomb and deal line damage
				if (!isInitialCast && trapLocation != null) {
					Location playerLoc = p.getLocation();
					Location teleportLoc = trapLocation.clone();
					
					// Draw particle line between player and trap before teleporting
					ParticleUtil.drawLine(p, lineParticle, playerLoc.clone().add(0, 1, 0), trapLocation.clone().add(0, 1, 0), 0.3);
					
					// Teleport player
					p.teleport(teleportLoc);
					Sounds.teleport.play(p, p);
					
					// Deal line damage from teleport location to old player location
					Location start = teleportLoc.add(0, 1, 0);
					Location end = playerLoc.add(0, 1, 0);
					Vector direction = end.toVector().subtract(start.toVector()).normalize();
					Location lineEnd = start.clone().add(direction.clone().multiply(lineProps.range));
					Sounds.firework.play(p, p);
					
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, lineEnd, lineProps)) {
						FightInstance.dealDamage(new DamageMeta(data, lineDamage, DamageType.LIGHTNING, 
							DamageStatTracker.of(id + slot, eq)), ent);
					}
					
					// Reset state
					isInitialCast = true;
					trapLocation = null;
					setIcon(item);
					
					return TriggerResult.keep();
				}
				
				// Initial cast: drop trap
				trapLocation = p.getLocation().clone();
				Sounds.equip.play(p, p);
				
				// Manual trap with particles and explosion
				data.addTask(new BukkitRunnable() {
					private int tickCount = 0;
					
					public void run() {
						trapParticle.play(p, trapLocation);
						tickCount++;
						
						// Explode after 2 seconds (40 ticks)
						if (tickCount >= 40) {
							Sounds.explode.play(p, trapLocation);
							explosionParticle.play(p, trapLocation);
							
							boolean hitElectrified = false;
							
							for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, trapLocation, radiusProps)) {
								FightInstance.dealDamage(new DamageMeta(data, explosionDamage, DamageType.LIGHTNING, 
									DamageStatTracker.of(id + slot, eq)), ent);
								
								// Check if enemy is electrified
								FightData fd = FightInstance.getFightData(ent);
								if (fd.hasStatus(StatusType.ELECTRIFIED)) {
									hitElectrified = true;
								}
							}
							
							// Enable recast if we hit an electrified enemy
							if (hitElectrified) {
								isInitialCast = false;
								ItemStack recastIcon = item.clone().withType(Material.LIGHTNING_ROD);
								setIcon(recastIcon);
								inst.setCooldown(-1);
							}
							else {
								trapLocation = null;
							}
							
							this.cancel();
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 0L, 1L));
				
				return TriggerResult.keep();
			};
			
			// Only consume resources on initial cast, not on recast
			resourceUsageCondition = (pl, pdata, in) -> {
				return isInitialCast;
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"Drop a marker that explodes after <white>2s</white>, dealing " + 
				GlossaryTag.LIGHTNING.tag(this, explosionDamage, false) + " damage to nearby enemies. " +
				"If any damaged enemy is " + GlossaryTag.ELECTRIFIED.tag(this) + ", recast to teleport to the bomb and deal " +
				GlossaryTag.LIGHTNING.tag(this, lineDamage, true) + " damage in a line.");
	}
}
