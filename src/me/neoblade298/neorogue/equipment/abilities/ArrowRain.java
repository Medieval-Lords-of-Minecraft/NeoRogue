package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmoEquipmentInstance;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ArrowRain extends Equipment {
	private static final String ID = "arrowRain";
	private static TargetProperties tp = TargetProperties.block(7, true),
			hitTp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static ParticleContainer hit = new ParticleContainer(Particle.EXPLOSION_NORMAL).count(50).spread(1, 0.1).speed(0.1),
			targeter = new ParticleContainer(Particle.REDSTONE).count(10).offsetY(1).spread(0.3, 0);
	private int damage, reps;
	
	public ArrowRain(boolean isUpgraded) {
		super(ID, "Arrow Rain", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 5, 1, tp.range));
				reps = isUpgraded ? 4 : 3;
				damage = 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new AmmoEquipmentInstance(p, this, slot, es, (pd, in) -> {
			Sounds.equip.play(p, p);
			data.charge(20);
			ProjectileGroup projs = new ProjectileGroup(new ArrowRainProjectile(p, p.getLocation(), data));
			data.addTask(new BukkitRunnable() {
				public void run() {
					initRain(p, data, projs);
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			return TriggerResult.keep();
		}));
	}

	private void initRain(Player p, PlayerFightData data, ProjectileGroup projs) {
		Ammunition ammo = data.getAmmunition();
		data.addTask(new BukkitRunnable() {
			private int tick = 0;
			public void run() {
				Sounds.shoot.play(p, p);
				Location block = TargetHelper.getSightLocation(p, tp);
				targeter.play(p, block);
				if (block != null) {
					dropRain(p, block, data, projs, ammo);
				}
				if (++tick >= reps) {
					this.cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 5L));
	}

	private void dropRain(Player p, Location block, PlayerFightData data, ProjectileGroup projs, Ammunition ammo) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				Location loc = block.add(0, 4, 0);
				Vector btwn = p.getLocation().toVector().subtract(loc.toVector()).setY(0).normalize().multiply(2);
				Vector dir = btwn.clone().add(new Vector(0,-4,0)).normalize();
				loc = loc.add(btwn);
				projs.start(data, loc, dir);
			}
		}.runTaskLater(NeoRogue.inst(), 20L));
	}
	
	private class ArrowRainProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private Ammunition ammo;

		public ArrowRainProjectile(Player p, Location trg, PlayerFightData data) {
			super(0.5, 6, 1);
			this.p = p;
			this.data = data;
			this.ammo = data.getAmmunition();
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.setOrigin(p.getLocation());
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = damage + ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location block = b.getLocation().add(0, 1, 0);
			hitAnimation(block);
			LivingEntity trg = TargetHelper.getNearest(p, block, hitTp);
			if (trg == null) return;
			DamageMeta dm = proj.getMeta();
			ammo.onHit(proj, trg);
			FightInstance.dealDamage(dm, trg);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			hitAnimation(hit.getEntity().getLocation());
			ammo.onHit(proj, hit.getEntity());
			FightInstance.dealDamage(proj.getMeta(), hit.getEntity());
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
		}

		private void hitAnimation(Location loc) {
			Sounds.explode.play(p, loc);
			hit.play(p, loc);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>1s</white>. Afterwards, shoot " + DescUtil.yellow(reps) + " of your equipped arrow at the blocks you're "
				+ "looking at. They land after <white>1s</white> and deal " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage.");
	}
}
