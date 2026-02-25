package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
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

public class Equalizer extends Bow {
	private static final String ID = "Equalizer";
	private static final TargetProperties tp = TargetProperties.radius(3, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION);
	private int damage, arrowDamage;
	
	public Equalizer(boolean isUpgraded) {
		super(ID, "Equalizer", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(80, 1, 0, 12, 0, 0.4));
		damage = isUpgraded ? 150 : 100;
		arrowDamage = isUpgraded ? 100 : 70;
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
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new EqualizerProjectile(data, ev.getEntity().getVelocity(), this, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CROSSBOW,
				"Projectiles have infinite " + GlossaryTag.PIERCING.tag(this) + ". " +
				"When projectiles hit a block, they explode, dealing " + 
				GlossaryTag.BLUNT.tag(this, damage, true) + " damage to nearby enemies. " +
				"On hit, rains <white>1</white> arrow dealing " + GlossaryTag.PIERCING.tag(this, arrowDamage, true) + 
				" damage for every <white>100</white> " + GlossaryTag.INJURY.tag(this) + " the enemy has " +
				"[<white>10 tick initial delay, 3 tick delay between arrows</white>].");
	}
	
	private class EqualizerProjectile extends BowProjectile {
		private PlayerFightData data;
		private Player p;
		private int slot;
		private Equalizer bow;

		public EqualizerProjectile(PlayerFightData data, Vector v, Equalizer bow, String id) {
			super(data, v, bow, id);
			this.pierce(-1); // Infinite piercing
			this.data = data;
			this.p = data.getPlayer();
			this.bow = bow;
			// Extract slot from id (format is "equalizer" + slot)
			this.slot = Integer.parseInt(id.replace(ID, ""));
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
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT, 
						DamageStatTracker.of(ID + slot, bow)), ent);
			}
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			super.onHit(hit, hitBarrier, meta, proj);
			
			// Check injury stacks on hit enemy
			int injuryStacks = hit.getStatus(StatusType.INJURY).getStacks();
			if (injuryStacks >= 100) {
				int numArrows = injuryStacks / 100;
				Location targetLoc = hit.getEntity().getLocation();
				
				// Schedule arrow rain with 10 tick initial delay, 3 tick spacing
				new BukkitRunnable() {
					int arrowsFired = 0;
					
					@Override
					public void run() {
						if (arrowsFired >= numArrows || !hit.getEntity().isValid()) {
							this.cancel();
							return;
						}
						
						// Rain one arrow - get fresh Player reference
						Player freshP = data.getPlayer();
						rainArrow(freshP, targetLoc, hit.getEntity());
						arrowsFired++;
					}
				}.runTaskTimer(NeoRogue.inst(), 10L, 3L);
			}
		}
		
		private void rainArrow(Player p, Location targetLoc, LivingEntity target) {
			// Calculate a location above the target for arrow to spawn
			Location startLoc = targetLoc.clone().add(0, 10, 0);
			
			// Create projectile that rains down
			BowProjectile rainProj = new BowProjectile(data, startLoc.toVector().subtract(targetLoc.toVector()).normalize().multiply(2), bow, ID + slot + "rain") {
				@Override
				public void onHitBlock(ProjectileInstance proj, Block b) {
					Location block = b.getLocation().add(0, 1, 0);
					Sounds.explode.play(p, block);
					LivingEntity trg = TargetHelper.getNearest(p, block, TargetProperties.radius(1.5, false, TargetType.ENEMY));
					if (trg == null) return;
					
					FightInstance.dealDamage(new DamageMeta(data, arrowDamage, DamageType.PIERCING, 
							DamageStatTracker.of(ID + slot, bow)), trg);
				}
				
				@Override
				public void onTick(ProjectileInstance proj, int interpolation) {
					BowProjectile.tick.play(p, proj.getLocation());
				}
			};
			
			ProjectileGroup rainGroup = new ProjectileGroup(rainProj);
			rainGroup.start(data, startLoc, new Vector(0, -1, 0));
		}
	}
}
