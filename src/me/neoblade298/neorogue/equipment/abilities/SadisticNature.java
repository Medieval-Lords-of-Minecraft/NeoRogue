package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

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
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SadisticNature extends Equipment {
	private static final String ID = "SadisticNature";
	private static final int BASE_DAMAGE = 50;
	private static final int CHARGE_TIME = 20; // 1 second
	private static final int TRAP_DURATION = 100; // 5 seconds
	private static final double TRAP_DAMAGE_INC = 0.05; // 5%
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(100, 20, 20), 1.5F))
			.count(8).spread(0.15, 0.15);
	private static final ParticleContainer trapParticle = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(150, 0, 0), 1.0F))
			.count(50).spread(1, 0.2);
	private static final ParticleContainer hitParticle = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(200, 0, 0), 1.5F))
			.count(50).spread(1, 1);
	private static final TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	
	private int bonusDamage;
	private int trapDamage;

	public SadisticNature(boolean isUpgraded) {
		super(ID, "Sadistic Nature", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, 12, 0));
		bonusDamage = isUpgraded ? 300 : 200;
		trapDamage = isUpgraded ? 500 : 300;
		properties.addUpgrades(PropertyType.COOLDOWN);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		data.addTrigger(id, bind, inst);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			
			// Start charging
			data.charge(CHARGE_TIME).then(new Runnable() {
				public void run() {
					// Fire projectile after charge completes
					ProjectileGroup proj = new ProjectileGroup(new SadisticNatureProjectile(data, SadisticNature.this, slot));
					proj.start(data);
					
					Sounds.shoot.play(p, p);
				}
			});
			
			return TriggerResult.keep();
		});
	}

	private class SadisticNatureProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private HashSet<LivingEntity> hitEntities = new HashSet<>();

		public SadisticNatureProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(0.5, 20, -1); // -1 = infinite pierce
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
			
			// Spawn trap at block location
			initTrap(blockLoc, data, eq, slot);
			
			Sounds.anvil.play(p, blockLoc);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
		}
	}
	
	private void initTrap(Location loc, PlayerFightData data, Equipment eq, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrap(new Trap(data, loc, TRAP_DURATION) {
			@Override
			public void tick() {
				trapParticle.play(data.getPlayer(), loc);
				LivingEntity trg = TargetHelper.getNearest(data.getPlayer(), loc, tp);
				if (trg != null) {
					Sounds.breaks.play(data.getPlayer(), trg);
					hitParticle.play(data.getPlayer(), trg);
					
					// Deal trap damage
					DamageMeta dm = new DamageMeta(data, trapDamage, DamageType.PIERCING, 
							DamageStatTracker.of(ID + slot, eq), DamageOrigin.TRAP);
					FightInstance.dealDamage(dm, trg);
					
					// Increase trap damage by 5%
					data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL, DamageOrigin.TRAP), 
							new Buff(data, 0, TRAP_DAMAGE_INC, BuffStatTracker.damageBuffAlly(buffId, eq)));
					
					data.removeTrap(this);
				}
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPECTRAL_ARROW,
				"After " + GlossaryTag.CHARGE.tag(this) + " for <white>" + (CHARGE_TIME / 20) + "s</white>, fire a projectile that deals " +
				DescUtil.white(BASE_DAMAGE) + " damage and pierces infinitely. " +
				"If the projectile hits a block, all enemies hit are pulled towards that block, " +
				"given <white>Slowness 3</white>, and dealt an additional " + 
				DescUtil.yellow(bonusDamage) + " damage. " +
				"Additionally, spawn a " + GlossaryTag.TRAP.tag(this) + " [<white>5s</white>] on the block that deals " +
				DescUtil.yellow(trapDamage) + " damage and increases your " + GlossaryTag.TRAP.tag(this) + 
				" damage by " + DescUtil.yellow((int)(TRAP_DAMAGE_INC * 100) + "%") + " when it's set off.");
	}
}
