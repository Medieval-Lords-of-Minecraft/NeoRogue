package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TrinityForce extends Equipment {
	private static final String ID = "TrinityForce";
	private static final int DAMAGE = 60;
	private static final TargetProperties tp = TargetProperties.radius(20, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 215, 0), 1.2F))
			.count(5).spread(0.2, 0.2);
	
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
			proj.start(data);
			Sounds.fire.play(p, p);
			return TriggerResult.keep();
		}));
	}
	
	private class TrinityForceProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private HashMap<UUID, Integer> hitCounts = new HashMap<>();

		public TrinityForceProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot) {
			super(1.5, 12, 1);
			this.rotation(angleOffset);
			this.size(0.4, 0.4);
			this.homing(0.02);
			this.pierce(-1); // Infinite pierce to allow multiple hits
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			UUID targetId = target.getUniqueId();
			
			// Track how many times we've hit this target
			int currentHits = hitCounts.getOrDefault(targetId, 0);
			
			// If we've hit max times, don't deal damage
			if (currentHits >= hits) {
				return;
			}
			
			// Increment hit count
			hitCounts.put(targetId, currentHits + 1);
			
			// Deal basic attack damage
			meta.addDamageSlice(new DamageSlice(data, DAMAGE, DamageType.PIERCING, 
					DamageStatTracker.of(ID + slot, eq)));
			meta.isBasicAttack(eq, false);
			
			Sounds.anvil.play(p, target.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			// Set homing target to nearest enemy
			LivingEntity nearest = TargetHelper.getNearest(p, proj.getLocation(), tp);
			if (nearest != null) {
				proj.setHomingTarget(nearest);
			}
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPECTRAL_ARROW,
				"On cast, fire <white>3</white> homing projectiles in a cone that each deal " + 
				DescUtil.yellow(DAMAGE) + " basic attack damage. Each projectile can hit an enemy up to " +
				DescUtil.white(hits) + " times.");
	}
}
