package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class MercurialGloves extends Equipment {
	private static final String ID = "MercurialGloves";
	private int damage, maxLocations;
	
	public MercurialGloves(boolean isUpgraded) {
		super(ID, "Mercurial Gloves", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, 12, 0));
		damage = isUpgraded ? 100 : 60;
		maxLocations = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		LinkedList<Location> hitLocations = new LinkedList<>();
		
		// Track last 3 basic attack hit locations via projectile hit block actions
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			
			for (IProjectileInstance inst : ev.getInstances()) {
				if (inst instanceof ProjectileInstance) {
					ProjectileInstance pi = (ProjectileInstance) inst;
					pi.addHitBlockAction((proj, b) -> {
						hitLocations.addFirst(b.getLocation().clone());
						if (hitLocations.size() > maxLocations) {
							hitLocations.removeLast();
						}
					});
					pi.addHitAction((hit, hitBarrier, meta, proj) -> {
						hitLocations.addFirst(hit.getEntity().getLocation().clone());
						if (hitLocations.size() > maxLocations) {
							hitLocations.removeLast();
						}
					});
				}
			}
			
			return TriggerResult.keep();
		});
		
		// Left click to fire projectiles from saved locations
		data.addTrigger(id, Trigger.LEFT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			if (hitLocations.isEmpty()) {
				Sounds.error.play(p, p);
				return TriggerResult.keep();
			}
			
			Sounds.fire.play(p, p);
			BowProjectile.tick.play(p, p);
			
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
			super(1, 12, 1);
			this.data = data;
			this.eq = eq;
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(data.getPlayer(), proj.getLocation());
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
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(ID, eq)));
			
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
				"Passive. Save the location of your last " + DescUtil.yellow(maxLocations) + " basic attacks. " +
				"On left click, fire your current ammunition from those locations toward you. " +
				"All projectiles count as basic attacks and deal " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage.");
	}
}
