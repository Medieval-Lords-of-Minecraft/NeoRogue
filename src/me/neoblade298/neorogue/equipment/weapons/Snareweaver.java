package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Snareweaver extends Bow {
	private static final String ID = "Snareweaver";
	private static final TargetProperties tp = TargetProperties.radius(3, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION);
	private int explosionDamage, bonusDamagePerTrap;
	
	public Snareweaver(boolean isUpgraded) {
		super(ID, "Snareweaver", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(80, 1, 0, 12, 0, 0.4));
		explosionDamage = isUpgraded ? 150 : 100;
		bonusDamagePerTrap = isUpgraded ? 15 : 10;
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta trapMeta = new ActionMeta(); // Tracks trap count and last trap time
		
		// Track trap placements
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			trapMeta.addCount(1); // Increment trap count
			trapMeta.setTime(System.currentTimeMillis()); // Set last trap time
			return TriggerResult.keep();
		});
		
		// Shoot projectile(s)
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			Vector velocity = ev.getEntity().getVelocity();
			
			// Check if trap was placed in last 5 seconds (5000 milliseconds)
			long timeSinceLastTrap = System.currentTimeMillis() - trapMeta.getTime();
			boolean recentTrap = timeSinceLastTrap <= 5000;
			
			ProjectileGroup proj = new ProjectileGroup();
			
			if (recentTrap) {
				// Fire 5 projectiles in a cone
				for (int angle : new int[] { -30, -15, 0, 15, 30 }) {
					proj.add(new SnareweaverProjectile(data, velocity, this, id + slot, trapMeta, angle));
				}
			} else {
				// Fire normal single projectile
				proj.add(new SnareweaverProjectile(data, velocity, this, id + slot, trapMeta, 0));
			}
			
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Projectiles have infinite " + GlossaryTag.PIERCING.tag(this) + ". " +
				"When projectiles hit a block, they explode, dealing " + 
				GlossaryTag.BLUNT.tag(this, explosionDamage, true) + " damage to nearby enemies. " +
				"Deals " + GlossaryTag.BLUNT.tag(this, bonusDamagePerTrap, true) + " bonus damage per trap you've set. " +
				"If you've set a trap in the last <white>5s</white>, fires <white>5</white> projectiles in a cone.");
	}
	
	private class SnareweaverProjectile extends BowProjectile {
		private PlayerFightData data;
		private Player p;
		private int slot;
		private Snareweaver bow;
		private ActionMeta trapMeta;

		public SnareweaverProjectile(PlayerFightData data, Vector v, Snareweaver bow, String id, ActionMeta trapMeta, int angle) {
			super(data, v, bow, id);
			this.pierce(-1); // Infinite piercing
			this.rotation(angle); // Apply cone spread
			this.data = data;
			this.p = data.getPlayer();
			this.bow = bow;
			this.trapMeta = trapMeta;
			// Extract slot from id (format is "snareweaver" + slot)
			this.slot = Integer.parseInt(id.replace(ID, ""));
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			super.onStart(proj);
			
			// Add bonus damage based on trap count
			int trapCount = trapMeta.getCount();
			if (trapCount > 0) {
				int bonusDamage = trapCount * bonusDamagePerTrap;
				DamageMeta meta = proj.getMeta();
				meta.addDamageSlice(new DamageSlice(data, bonusDamage, DamageType.BLUNT, 
						DamageStatTracker.of(ID + slot + "bonus", bow)));
			}
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			// Call parent implementation for ammunition handling
			super.onHitBlock(proj, b);
			
			// Explosion effect
			Sounds.explode.play(p, proj.getLocation());
			pc.play(p, proj.getLocation());
			
			// Deal damage to nearby enemies
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, proj.getLocation(), tp)) {
				FightInstance.dealDamage(new DamageMeta(data, explosionDamage, DamageType.BLUNT, 
						DamageStatTracker.of(ID + slot, bow)), ent);
			}
		}
	}
}
