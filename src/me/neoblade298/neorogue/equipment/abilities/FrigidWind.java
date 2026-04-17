package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class FrigidWind extends Equipment {
	private static final String ID = "FrigidWind";
	private static final ParticleContainer wind = new ParticleContainer(Particle.CLOUD).count(15).spread(1, 0.5);
	private static final ParticleContainer ice = new ParticleContainer(Particle.SNOWFLAKE).count(30).spread(0.5, 0.5);
	private int frost, damage, frostPerDamage;
	
	public FrigidWind(boolean isUpgraded) {
		super(ID, "Frigid Wind", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 10, 15));
		frost = isUpgraded ? 15 : 10;
		damage = isUpgraded ? 150 : 100;
		frostPerDamage = 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.addTrigger(ID, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in2) -> {
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in2;
				if (!ev.isBasicAttack()) return TriggerResult.keep();
				
				// Launch the wind projectile
				ProjectileGroup windProj = new ProjectileGroup(new FrigidWindProjectile(data, this, slot));
				windProj.start(data);
				Sounds.wind.play(p, p);
				
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	private class FrigidWindProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private HashSet<UUID> hitEntities = new HashSet<>();

		public FrigidWindProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(15, 1);
			this.size(2, 1);
			this.pierce(-1);
			this.blocksPerTick(0.4);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			wind.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LivingEntity target = hit.getEntity();
			
			// Apply frost
			hit.applyStatus(StatusType.FROST, data, frost, -1);
			
			// Track entity for delayed damage
			hitEntities.add(target.getUniqueId());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.wind.play(p, p);
            data.addTask(new BukkitRunnable() {
				public void run() {
					Player p = data.getPlayer();
					for (UUID uuid : hitEntities) {
						LivingEntity target = (LivingEntity) Bukkit.getEntity(uuid);
						if (target == null || !target.isValid())
							continue;

						FightData fd = FightInstance.getFightData(target);
						if (fd == null)
							continue;

						// Get frost stacks at this point (5 seconds later)
						int frozenStacks = fd.getStatus(StatusType.FROST).getStacks();
						double totalDamage = (frozenStacks / (double) frostPerDamage) * damage;
                        if (totalDamage > 0) {
                            ice.play(p, target.getLocation());
                            Sounds.glass.play(p, target.getLocation());
                            DamageMeta dm = new DamageMeta(data, totalDamage, DamageType.ICE,
                                    DamageStatTracker.of(ID + slot, eq));
                            FightInstance.dealDamage(dm, target);
                        }
                    }
                }
            }.runTaskLater(NeoRogue.inst(), 100));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"On cast, your next basic attack also launches a slow-moving <white>2</white> block wide " +
				"piercing wind projectile that applies " + GlossaryTag.FROST.tag(this, frost, true) + " to enemies hit. " +
				"<white>5s</white> later, enemies take " + GlossaryTag.ICE.tag(this, damage, true) + " damage per " +
				DescUtil.white(frostPerDamage) + " " + GlossaryTag.FROST.tag(this) + " they have.");
	}
}
