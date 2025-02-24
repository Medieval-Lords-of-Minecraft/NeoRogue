package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmoEquipmentInstance;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
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
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreLaunchProjectileGroupEvent;

public class PreySeeker extends Equipment {
	private static final String ID = "preySeeker";
	private static TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.CLOUD).count(20).spread(2, 0.2);
	private int damage, dur = 8;
	
	public PreySeeker(boolean isUpgraded) {
		super(ID, "Prey Seeker", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 2, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		
		damage = isUpgraded ? 225 : 150;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		PreySeekerInstance inst = new PreySeekerInstance(data, this, slot, es);
		data.addTrigger(id, Trigger.PRE_LAUNCH_PROJECTILE_GROUP, inst);
	}

	private class PreySeekerInstance extends AmmoEquipmentInstance {
		public PreySeekerInstance(PlayerFightData data, Equipment equip, int slot, EquipSlot es) {
			super(data, equip, slot, es);
			action = (pdata, in) -> {
				Sounds.equip.play(p, p);
				initTrap(p, data);
				return TriggerResult.of(false, true);
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			PreLaunchProjectileGroupEvent ev = (PreLaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return false;
			if (p.getEyeLocation().getDirection().getY() < 0.9) return false; // Looking straight up
			return super.canTrigger(p, data, in);
		}

		private void initTrap(Player p, PlayerFightData data) {
			Location loc = p.getLocation();
			AmmunitionInstance ammo = data.getAmmoInstance();
			data.addTrap(new Trap(data, loc, dur * 20) {
				@Override
				public void tick() {
					trap.play(p, loc);
					LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
					if (trg != null) {
						ProjectileGroup projs = new ProjectileGroup(new PreySeekerProjectile(p, loc, data, ammo));
						Location up = loc.clone().add(0, 4, 0);
						projs.start(data, up, new Vector(0, -1, 0));
						data.removeTrap(this);
					}
				}
			});
		}
	}
	
	private class PreySeekerProjectile extends Projectile {
		private static ParticleContainer hit = new ParticleContainer(Particle.EXPLOSION).count(50).spread(1, 0.1).speed(0.1);
		private Player p;
		private PlayerFightData data;
		private AmmunitionInstance ammo;
		private Location src;

		public PreySeekerProjectile(Player p, Location src, PlayerFightData data, AmmunitionInstance ammo) {
			super(0.5, 6, 1);
			this.p = p;
			this.data = data;
			this.ammo = ammo;
			this.src = src;
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.setOrigin(src);
			DamageMeta dm = proj.getMeta();
			dm.addOrigin(DamageOrigin.TRAP);
			EquipmentProperties ammoProps = ammo.getProperties();
			double dmg = damage + ammoProps.get(PropertyType.DAMAGE);
			dm.addDamageSlice(new DamageSlice(data, dmg, ammoProps.getType()));
			ammo.onStart(proj, false);
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location block = b.getLocation().add(0, 1, 0);
			hitAnimation(block);
			LivingEntity trg = TargetHelper.getNearest(p, block, tp);
			if (trg == null) return;
			DamageMeta dm = proj.getMeta();
			ammo.onHit(proj, dm, trg);
			FightInstance.dealDamage(dm, trg);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			hitAnimation(hit.getEntity().getLocation());
			ammo.onHit(proj, meta, hit.getEntity());
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		private void hitAnimation(Location loc) {
			Sounds.explode.play(p, loc);
			hit.play(p, loc);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"Passive. Upon firing a basic attack straight up, cancel the basic attack and drop a " + GlossaryTag.TRAP.tag(this) + 
				" that lasts for " + DescUtil.duration(dur, false) +
				". If an enemy steps on the trap, they take " + DescUtil.yellow(damage) +
				" damage using the current ammunition and deactivate the trap.");
	}
}
