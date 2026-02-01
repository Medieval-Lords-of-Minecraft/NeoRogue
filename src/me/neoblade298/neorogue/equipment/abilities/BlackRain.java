package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BlackRain extends Equipment {
	private static final String ID = "BlackRain";
	private int damage, bonusDamage, thres;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.BLACK, 1F));
	private static final ParticleContainer bonusPC = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.PURPLE, 1.2F))
		.count(30).spread(0.2, 1).speed(0.3);
	
	public BlackRain(boolean isUpgraded) {
		super(ID, "Black Rain", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 15, 10, 8));
		damage = isUpgraded ? 350 : 250;
		bonusDamage = isUpgraded ? 200 : 150;
		thres = isUpgraded ? 150 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup();
		List<LivingEntity> hitEntities = new ArrayList<>();
		
		// Create 3 projectiles in a cone spread
		for (int angle : new int[] { -15, 0, 15 }) {
			proj.add(new BlackRainProjectile(data, angle, this, slot, hitEntities));
		}
		
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			hitEntities.clear(); // Reset for new cast
			Sounds.attackSweep.play(p, p);
			proj.start(data);
			
			// After initial projectiles complete, calculate total insanity and drop bonus projectiles
			data.addTask(new BukkitRunnable() {
				public void run() {
					int totalInsanity = 0;
					for (LivingEntity ent : hitEntities) {
						FightData fd = FightInstance.getFightData(ent);
						if (fd.hasStatus(StatusType.INSANITY)) {
							totalInsanity += fd.getStatus(StatusType.INSANITY).getStacks();
						}
					}
					
					int bonusProjectiles = totalInsanity / 40;
					if (bonusProjectiles > 0 && !hitEntities.isEmpty()) {
						dropBonusProjectiles(p, data, hitEntities, bonusProjectiles, slot);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 10L)); // Wait for projectiles to hit
			
			return TriggerResult.keep();
		}));
	}
	
	private void dropBonusProjectiles(Player p, PlayerFightData data, List<LivingEntity> targets, int count, int slot) {
		for (int i = 0; i < count && !targets.isEmpty(); i++) {
			// Select random target from hit entities
			LivingEntity target = targets.get((int) (Math.random() * targets.size()));
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (target.isDead()) return;
					
					// Visual effect above target
					bonusPC.play(p, target.getLocation().add(0, 3, 0));
					Sounds.extinguish.play(p, target.getLocation());
					
					// Deal bonus damage
					FightInstance.dealDamage(data, DamageType.DARK, bonusDamage, target, 
						DamageStatTracker.of(id + "-bonus" + slot, BlackRain.this));
				}
			}.runTaskLater(NeoRogue.inst(), 2L * (i + 1))); // 2-tick delay between each bonus projectile
		}
	}
	
	private class BlackRainProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private List<LivingEntity> hitEntities;

		public BlackRainProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot, List<LivingEntity> hitEntities) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.rotation(angleOffset);
			this.size(0.3, 0.3);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.hitEntities = hitEntities;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (hit.getEntity() instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) hit.getEntity();
				if (!hitEntities.contains(entity)) {
					hitEntities.add(entity);
				}
			}
			Sounds.extinguish.play(p, hit.getEntity().getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, 
				DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SCRAP,
			"On cast, throw <white>3</white> projectiles in a cone that each deal " + 
			GlossaryTag.DARK.tag(this, damage, true) + " damage. For every " +
			GlossaryTag.INSANITY.tag(this, thres, true) + " on hit enemies combined, drop another projectile dealing " +
			GlossaryTag.DARK.tag(this, bonusDamage, true) + " damage.");
	}
}