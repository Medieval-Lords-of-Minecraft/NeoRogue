package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DyingStar extends Equipment {
	private static final String ID = "DyingStar";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer pull = new ParticleContainer(Particle.END_ROD).count(50).spread(2.5, 2.5).offsetY(1),
		explode = new ParticleContainer(Particle.DRAGON_BREATH).count(100).spread(2.5, 2.5).offsetY(1);
	private static final SoundContainer pullSound = new SoundContainer(Sound.ENTITY_ENDER_DRAGON_GROWL),
		explodeSound = new SoundContainer(Sound.ENTITY_GENERIC_EXPLODE);
	private int damage;
	
	public DyingStar(boolean isUpgraded) {
		super(ID, "Dying Star", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 50, 30, 0));
		damage = isUpgraded ? 450 : 300;
		properties.add(PropertyType.DAMAGE, damage);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"Castable once. Create a " + GlossaryTag.RIFT.tag(this) + ". Afterwards, when any " + GlossaryTag.RIFT.tag(this) +
				" expires, pull in nearby enemies, then explode <white>1s</white> later dealing " +
				GlossaryTag.DARK.tag(this, damage, true) + " damage. If an enemy is killed, spawn a new " +
				GlossaryTag.RIFT.tag(this) + " [" + DescUtil.white("10s") + "] in the same place.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Track rift explosion locations and entities hit by them
		HashMap<UUID, Location> killedEntityToRiftLoc = new HashMap<>();
		
		DyingStarInstance inst = new DyingStarInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		
		// Handle rift expiration - pull enemies and explode
		data.addTrigger(id, Trigger.REMOVE_RIFT, (pdata, in) -> {
			Player p = data.getPlayer();
			Rift rift = (Rift) in;
			Location riftLoc = rift.getLocation();
			
			// Pull enemies toward rift
			pullSound.play(p, riftLoc);
			circ.play(pull, riftLoc, LocalAxes.xz(), null);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, riftLoc, tp)) {
				Vector direction = riftLoc.toVector().subtract(ent.getLocation().toVector());
				if (!direction.isZero()) {
					direction = direction.normalize().setY(0.3);
					ent.setVelocity(direction);
				}
			}
			
			// Explode 1 second later
			data.addTask(new BukkitRunnable() {
				public void run() {
					Player p = data.getPlayer();
					explodeSound.play(p, riftLoc);
					circ.play(explode, riftLoc, LocalAxes.xz(), null);
					Sounds.explode.play(p, riftLoc);
					
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, riftLoc, tp)) {
						FightInstance.dealDamage(data, DamageType.DARK, damage, ent,
							DamageStatTracker.of(id + slot, DyingStar.this));
						// Track this entity as potentially killed by this rift
						killedEntityToRiftLoc.put(ent.getUniqueId(), riftLoc.clone());
					}
					
					// Clean up old entries after a short delay
					data.addTask(new BukkitRunnable() {
						public void run() {
							killedEntityToRiftLoc.entrySet().removeIf(entry -> 
								entry.getValue().equals(riftLoc));
						}
					}.runTaskLater(NeoRogue.inst(), 10L));
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			
			return TriggerResult.keep();
		});
		
		// Handle kills to spawn new rifts
		data.addTrigger(id, Trigger.KILL_GLOBAL, (pdata, in) -> {
			LivingEntity ent = (LivingEntity) in;
			Location riftLoc = killedEntityToRiftLoc.remove(ent.getUniqueId());
			if (riftLoc != null) {
				// Spawn a new rift at the location where the rift exploded
				Player p = data.getPlayer();
				Sounds.equip.play(p, riftLoc);
				data.addRift(new Rift(data, riftLoc, 200)); // 10 seconds = 200 ticks
			}
			return TriggerResult.keep();
		});
	}
	
	private class DyingStarInstance extends EquipmentInstance {
		private boolean used = false;
		
		public DyingStarInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			
			action = (pdata, in) -> {
				if (used) return TriggerResult.remove();
				used = true;
				
				Player p = data.getPlayer();
				Location loc = p.getLocation();
				Sounds.equip.play(p, p);
				data.addRift(new Rift(data, loc, 100)); // 5 seconds = 100 ticks
				
				return TriggerResult.keep();
			};
		}
	}
}
