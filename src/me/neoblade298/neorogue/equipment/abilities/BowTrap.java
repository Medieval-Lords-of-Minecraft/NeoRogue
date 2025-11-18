package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.BowProjectile;
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
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BowTrap extends Equipment {
	private static final String ID = "BowTrap";
	private static TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.CRIT).count(50).spread(1, 0.2),
		hit = new ParticleContainer(Particle.CRIT).count(50).spread(1, 1);
	private int damage;
	
	public BowTrap(boolean isUpgraded) {
		super(ID, "Bow Trap", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 5, 8, tp.range));
		
		damage = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Sounds.equip.play(p, p);
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					initTrap(p, data, slot);
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		}));
	}

	private void initTrap(Player p, PlayerFightData data, int slot) {
		Location loc = p.getLocation();
		ProjectileGroup proj = new ProjectileGroup(new BowTrapProjectile(data, ID + slot, this));
		data.addTrap(new Trap(data, loc, 100) {
			@Override
			public void tick() {
				trap.play(p, loc);
				LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
				if (trg != null) {
					Sounds.shoot.play(p, loc);
					Location future = trg.getEyeLocation().add(trg.getVelocity());
					Vector btwn = future.toVector().subtract(loc.toVector()).normalize();
					proj.start(data, loc, btwn);
					
					hit.play(p, trg);
				}
			}
		});
	}

	private class BowTrapProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private BowTrap eq;
		private String id;
		
		public BowTrapProjectile(PlayerFightData data, String id, BowTrap eq) {
			super(tp.range, 1);
			setBowDefaults();
			this.gravity(0);
			blocksPerTick(3);
			this.data = data;
			this.p = data.getPlayer();
			this.id = id;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id, eq)));
			proj.getMeta().addOrigin(DamageOrigin.TRAP);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARCHER_POTTERY_SHERD,
				"On cast, " + DescUtil.charge(this, 1, 1) + ". Afterwards, drop a " + GlossaryTag.TRAP.tag(this) + 
				" that lasts for " + DescUtil.white("5s") +
				". It fires a projectile that deals " + GlossaryTag.PIERCING.tag(this, damage, true) +
				" damage at the nearest enemy every second.");
	}
}
