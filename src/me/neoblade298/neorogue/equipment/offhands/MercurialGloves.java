package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class MercurialGloves extends Equipment {
	private static final String ID = "MercurialGloves";
	private static final int MAX_LOCATIONS = 3;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT)
			.count(10).spread(0.5, 0.5);
	
	public MercurialGloves(boolean isUpgraded) {
		super(ID, "Mercurial Gloves", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, 12, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		LinkedList<Location> hitLocations = new LinkedList<>();
		
		// Track last 3 basic attack locations where damage was dealt
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().isBasicAttack()) return TriggerResult.keep();
			
			Location hitLoc = ev.getTarget().getLocation().clone();
			hitLocations.addFirst(hitLoc);
			
			// Keep only the last 3 locations
			if (hitLocations.size() > MAX_LOCATIONS) {
				hitLocations.removeLast();
			}
			
			return TriggerResult.keep();
		});
		
		// Right click to fire projectiles from saved locations
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			if (hitLocations.isEmpty()) {
				Sounds.error.play(p, p);
				return TriggerResult.keep();
			}
			
			Sounds.fire.play(p, p);
			pc.play(p, p);
			
			// Fire projectile from each saved location toward the player
			for (Location loc : hitLocations) {
				Vector direction = p.getLocation().subtract(loc).toVector().normalize();
				ProjectileGroup proj = new ProjectileGroup(new MercurialProjectile(data, this));
				proj.start(data, loc.clone().add(0, 1, 0), direction);
			}
			
			// Clear the locations after use
			hitLocations.clear();
			
			return TriggerResult.keep();
		}));
	}
	
	private class MercurialProjectile extends Projectile {
		private PlayerFightData data;
		private Equipment eq;
		
		public MercurialProjectile(PlayerFightData data, Equipment eq) {
			super(0.5, 12, 1);
			this.data = data;
			this.eq = eq;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(data.getPlayer(), proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Use ammunition if equipped
			if (data.getAmmoInstance() != null) {
				data.getAmmoInstance().onHit(proj, meta, hit.getEntity());
			}
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			// Mark as basic attack
			proj.getMeta().isBasicAttack(eq, true);
			
			// Apply ammunition properties
			if (data.getAmmoInstance() != null) {
				proj.applyAmmo(data, eq, data.getAmmoInstance());
				data.getAmmoInstance().use();
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER,
				"Passive. Save the location of your last <white>" + MAX_LOCATIONS + "</white> basic attacks. " +
				"On right click, fire your current ammunition from those locations toward you. " +
				"All projectiles count as basic attacks.");
	}
}
