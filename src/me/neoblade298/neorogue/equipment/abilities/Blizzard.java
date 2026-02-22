package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Marker;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Blizzard extends Equipment {
	private static final String ID = "Blizzard";
	private static final int DURATION = 10;
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static final ParticleContainer blizzardParticle = new ParticleContainer(Particle.SNOWFLAKE).count(50).spread(4, 1);
	private static final ParticleContainer damageParticle = new ParticleContainer(Particle.BLOCK).blockData(Material.ICE.createBlockData()).count(30).spread(2, 1);
	private int threshold, damage, frost;
	
	public Blizzard(boolean isUpgraded) {
		super(ID, "Blizzard", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		
		threshold = isUpgraded ? 700 : 1000;
		damage = isUpgraded ? 90 : 60;
		frost = isUpgraded ? 150 : 90;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta frostCounter = new ActionMeta();
		ActionMeta blizzardMarker = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		ItemStack trackingIcon = item.clone();
		ItemStack activeIcon = item.clone().withType(Material.PACKED_ICE);
		
		// Track frost applications
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			if (blizzardMarker.getTrap() != null && blizzardMarker.getTrap().isActive()) return TriggerResult.keep();
			
			frostCounter.addCount(ev.getStacks());
			
			// Update icon countdown (10 to 1, representing percentage progress)
			int percentage = Math.min(100, (frostCounter.getCount() * 100) / threshold);
			int displayCount = Math.max(1, 10 - (percentage / 10));
			trackingIcon.setAmount(displayCount);
			inst.setIcon(trackingIcon);
			
			// Summon blizzard when threshold reached
			if (frostCounter.getCount() >= threshold) {
				Player p = data.getPlayer();
				Sounds.wind.play(p, p);
				frostCounter.setCount(0);
				
				// Find nearest enemy to follow
				LivingEntity target = TargetHelper.getNearest(p, TargetProperties.radius(15, false, TargetType.ENEMY));
				Location spawnLoc = target != null ? target.getLocation() : p.getLocation();
				
				// Create the blizzard marker
				Marker blizzard = new Marker(data, spawnLoc, DURATION * 20) {
					@Override
					public void tick() {
						Player p = data.getPlayer();
						Location loc = getLocation();
						
						// Follow nearest enemy
						LivingEntity nearest = TargetHelper.getNearest(p, loc, TargetProperties.radius(20, false, TargetType.ENEMY));
						if (nearest != null) {
							// Move towards enemy
							Location targetLoc = nearest.getLocation();
							Location newLoc = loc.clone().add(targetLoc.toVector().subtract(loc.toVector()).normalize().multiply(0.5));
							setLocation(newLoc);
							loc = newLoc;
						}
						
						// Visual effects
						blizzardParticle.play(p, loc);
						
						// Deal damage and apply frost
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
							DamageMeta dm = new DamageMeta(data, damage, DamageType.ICE, DamageStatTracker.of(id + slot, Blizzard.this));
							FightInstance.dealDamage(dm, ent);
							FightInstance.applyStatus(ent, StatusType.FROST, data, frost, -1);
						}
						damageParticle.play(p, loc);
					}
					
					@Override
					public void onDeactivate() {
						// Reset to tracking icon when blizzard ends
						trackingIcon.setAmount(10);
						inst.setIcon(trackingIcon);
						blizzardMarker.setTrap(null);
					}
				};
				
				data.addMarker(blizzard);
				blizzardMarker.setTrap(blizzard);
				
				// Change to active icon
				inst.setIcon(activeIcon);
			}
			
			return TriggerResult.keep();
		});
		
		// Teleport blizzard to player on left-click (only when active)
		inst.setAction((pdata, in) -> {
			if (blizzardMarker.getTrap() == null || !blizzardMarker.getTrap().isActive()) return TriggerResult.keep();
			
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (blizzardMarker.getTrap() != null && blizzardMarker.getTrap().isActive()) {
						Marker marker = blizzardMarker.getTrap();
						marker.setLocation(p.getLocation());
						Sounds.teleport.play(p, p);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LEFT_CLICK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SNOWBALL,
				"After applying " + DescUtil.yellow(threshold) + " " + GlossaryTag.FROST.tag(this) + 
				", summon a blizzard that follows enemies for <white>10s</white>. " +
				"It deals " + GlossaryTag.ICE.tag(this, damage, true) + " damage and applies " + 
				GlossaryTag.FROST.tag(this, frost, true) + " per second. " +
				"On cast (only after summoned), " + DescUtil.charge(this, 1, 20) + " to teleport the blizzard to you.");
	}
}
