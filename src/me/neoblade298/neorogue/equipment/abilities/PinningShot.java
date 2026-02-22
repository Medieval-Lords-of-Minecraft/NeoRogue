package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class PinningShot extends Equipment {
	private static final String ID = "PinningShot";
	private static final int BASE_DAMAGE = 50;
	private static final int CHARGE_TIME = 20; // 1 second
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(180, 180, 180), 1.5F))
			.count(8).spread(0.15, 0.15);
	private static final int range = 12;
	
	private int bonusDamage;

	public PinningShot(boolean isUpgraded) {
		super(ID, "Pinning Shot", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, range));
		bonusDamage = isUpgraded ? 300 : 200;
		properties.addUpgrades(PropertyType.COOLDOWN);
	}

    @Override
    public void setupReforges() {
        addReforge(Saboteur.get(), SadisticNature.get());
    }

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		data.addTrigger(id, bind, inst);
		inst.setAction((pdata, in) -> {
			// Start charging
			data.charge(CHARGE_TIME).then(new Runnable() {
				public void run() {
					// Fire projectile after charge completes
					ProjectileGroup proj = new ProjectileGroup(new PinningShotProjectile(data, PinningShot.this, slot));
					proj.start(data);
				}
			});
			
			return TriggerResult.keep();
		});
	}

	private class PinningShotProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private HashSet<LivingEntity> hitEntities = new HashSet<>();

		public PinningShotProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1, range, 1);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.pierce(-1);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Track hit entities
			hitEntities.add(target);
			
			// Deal base damage
			meta.addDamageSlice(new DamageSlice(data, BASE_DAMAGE, DamageType.PIERCING, 
					DamageStatTracker.of(ID + slot, eq)));
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location blockLoc = b.getLocation().add(0.5, 0.5, 0.5); // Center of block
			
			// Pull all hit enemies to the block and deal bonus damage
			for (LivingEntity target : hitEntities) {
				if (target.isDead()) continue;
				
				// Pull towards block
				Vector direction = blockLoc.toVector().subtract(target.getLocation().toVector()).normalize();
				target.setVelocity(direction.multiply(2));
				
				// Apply slowness 3 (level 2 in API) for 3 seconds
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
				
				// Deal bonus damage
				FightData fd = FightInstance.getFightData(target);
				if (fd != null) {
					DamageSlice slice = new DamageSlice(data, bonusDamage, DamageType.PIERCING,
							DamageStatTracker.of(ID + slot, eq));
					DamageMeta meta = new DamageMeta(data);
					meta.addDamageSlice(slice);
					FightInstance.dealDamage(meta, target);
				}
			}
			
			Sounds.anvil.play(p, blockLoc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TARGET,
				GlossaryTag.CHARGE.tag(this) + " for <white>" + (CHARGE_TIME / 20) + "s</white>, then fire a projectile that deals " +
				DescUtil.white(BASE_DAMAGE) + " damage and pierces infinitely. " +
				"If the projectile hits a block, all enemies hit are pulled towards that block, " +
				"given <white>Slowness III</white>, and dealt an additional " + 
				DescUtil.yellow(bonusDamage) + " damage.");
	}
}
