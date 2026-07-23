package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class FlashSpark extends Equipment {
	private static final String ID = "FlashSpark";
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	// Crisp ground ring telegraphing the strike's area of effect
	private static final ParticleContainer ring = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(2).spread(0, 0).speed(0);
	private static final ParticleContainer ringFill = new ParticleContainer(Particle.END_ROD)
			.count(1).spread(0.1, 0).speed(0);
	// Glowing charge marker at the target location
	private static final ParticleContainer marker = new ParticleContainer(Particle.END_ROD)
			.count(20)
			.spread(0.2, 0.5)
			.speed(0.01);
	// Bright, thin bolt column drawn from the sky down to the marker
	private static final ParticleContainer bolt = new ParticleContainer(Particle.END_ROD)
			.count(3).spread(0.08, 0.08).speed(0);
	private static final ParticleContainer boltSpark = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(3).spread(0.12, 0.12).speed(0);
	// Heavy impact flash at the strike point
	private static final ParticleContainer strike = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(180)
			.spread(tp.range, 1.5)
			.offsetY(1)
			.speed(0.4);
	private static final ParticleContainer flash = new ParticleContainer(Particle.FIREWORK)
			.count(60)
			.spread(1, 1)
			.offsetY(1)
			.speed(0.2);
	private int damage, electrified;
	
	public FlashSpark(boolean isUpgraded) {
		super(ID, "Flash Spark", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 12, 0));
		properties.add(PropertyType.AREA_OF_EFFECT, tp.range);
		damage = isUpgraded ? 190 : 125;
		electrified = isUpgraded ? 7 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			Location markerLoc = p.getLocation().clone();
			marker.play(p, markerLoc);
			circ.play(p, ring, markerLoc, LocalAxes.xz(), ringFill);
			Sounds.equip.play(p, p);
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					// Lightning bolt strikes down from the sky into the marker
					Location sky = markerLoc.clone().add(0, 12, 0);
					ParticleUtil.drawLine(p, bolt, sky, markerLoc, 0.4);
					ParticleUtil.drawLine(p, boltSpark, sky, markerLoc, 0.4);
					strike.play(p, markerLoc.clone().add(0, 0.5, 0));
					flash.play(p, markerLoc.clone().add(0, 1, 0));
					circ.play(p, ring, markerLoc, LocalAxes.xz(), ringFill);
					Sounds.thunder.play(p, markerLoc);
					Sounds.explode.play(p, markerLoc);
					
					boolean playerInRange = p.getLocation().distance(markerLoc) <= tp.range;
					
					// Deal damage to enemies in range
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, markerLoc, tp)) {
						FightInstance.dealDamage(pdata, DamageType.LIGHTNING, damage, ent, 
								DamageStatTracker.of(id + slot, FlashSpark.this));
					}
					
					// Grant buffs to player if they're in range
					if (playerInRange) {
                        Sounds.levelup.play(p, p);
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1)); // 3 seconds, Speed 2
						am.setTime(System.currentTimeMillis() + 10000); // 10 seconds from now
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20L)); // 1 second delay
			
			return TriggerResult.keep();
		}));
		
		// Apply electrified on basic attacks for 10 seconds after lightning
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (am.getTime() == 0 || System.currentTimeMillis() > am.getTime()) {
				return TriggerResult.keep();
			}
			
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, electrified, -1, this);
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				"On cast, drop a marker at your location. After " + DescUtil.val("1s") + ", a lightning bolt strikes the marker, " +
				"dealing " + GlossaryTag.LIGHTNING.tag(this, damage) + " damage to enemies " +
				"nearby. If you're also in range, gain " + 
				DescUtil.potion("Speed", 1, 3) + " and your basic attacks apply " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified) + " for the next " + DescUtil.val("10s") + ".");
	}
}
