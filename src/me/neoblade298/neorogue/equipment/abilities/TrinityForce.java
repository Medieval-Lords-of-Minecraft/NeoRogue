package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TrinityForce extends Equipment {
	private static final String ID = "TrinityForce";
	private static final int DAMAGE = 30;
	private static final TargetProperties tp = TargetProperties.radius(20, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 215, 0), 0.8F))
			.count(3).spread(0.1, 0.1);
	
	private int hits;

	public TrinityForce(boolean isUpgraded) {
		super(ID, "Trinity Force", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 15, 10, 12));
		hits = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup();
		
		// Create 3 projectiles in a cone spread
		for (int angle : new int[] { -20, 0, 20 }) {
			proj.add(new TrinityForceProjectile(data, angle, this, slot));
		}
		
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.charge(20).then(new Runnable() {
				@Override
				public void run() {
					proj.start(data);
					Player p = data.getPlayer();
					Sounds.fire.play(p, p);
				}
			});
			return TriggerResult.keep();
		}));
	}
	
	private class TrinityForceProjectile extends Projectile {
		private PlayerFightData data;
		private Equipment eq;
		private int slot;

		public TrinityForceProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot) {
			super(0.7, 12, 1);
			this.rotation(angleOffset);
			this.size(0.4, 0.4);
			this.homing(0.02);
			this.data = data;
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = data.getPlayer();
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Deal damage multiple times with a 3 tick period
			data.addTask(new BukkitRunnable() {
				int hitCount = 0;
				
				public void run() {
					if (!target.isValid() || target.isDead() || hitCount >= hits) {
						cancel();
						return;
					}
					
					// Deal basic attack damage
					DamageMeta dm = new DamageMeta(data, DAMAGE, DamageType.PIERCING,
							DamageStatTracker.of(id + slot, eq));
					dm.isBasicAttack(eq, false);
					FightInstance.dealDamage(dm, target);
					
					Player p = data.getPlayer();
					Sounds.anvil.play(p, target.getLocation());
					
					hitCount++;
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 3));
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			// Set homing target to nearest enemy
			Player p = data.getPlayer();
			LivingEntity nearest = TargetHelper.getNearest(p, proj.getLocation(), tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPECTRAL_ARROW,
				"On cast, " + DescUtil.charge(this, 0, 1) + " before firing <white>3</white> homing projectiles in a cone that each deal " + 
				GlossaryTag.PIERCING.tag(this, DAMAGE, true) + " damage as basic attack damage " + DescUtil.yellow(hits) + " times.");
	}
}
