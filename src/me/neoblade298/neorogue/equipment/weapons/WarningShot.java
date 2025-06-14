package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WarningShot extends Equipment {
	private static final String ID = "warningShot";
	private static ParticleContainer pc = new ParticleContainer(Particle.END_ROD);
	private static final TargetProperties tp = TargetProperties.radius(6, false, TargetType.ENEMY);
	private static Circle circ = new Circle(tp.range);
	private int damage, focus;
	
	public WarningShot(boolean isUpgraded) {
		super(ID, "Warning Shot", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 15, 14, 20).add(PropertyType.AREA_OF_EFFECT, tp.range));
				focus = isUpgraded ? 2 : 1;
		damage = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new WarningShotProjectile(data, this));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (d, inputs) -> {
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		}, Bow.needsAmmo));
	}
	
	private class WarningShotProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public WarningShotProjectile(PlayerFightData data, Equipment eq) {
			super(properties.get(PropertyType.RANGE), 1);
			this.blocksPerTick(2);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
		}

		@Override
		public void onHitBlock(ProjectileInstance proj, Block b) {
			Location loc = b.getLocation();
			String buffId = UUID.randomUUID().toString();
			// If projectile is above the top of a block, it's hitting a floor
			if (proj.getLocation().getY() >= b.getLocation().getY()) {
				loc = loc.add(0, 2, 0);
			}	
			Sounds.fire.play(p, loc);
			circ.play(pc, loc, LocalAxes.xz(), null);

			LinkedList<LivingEntity> ents = TargetHelper.getEntitiesInRadius(p, loc, tp);
			for (LivingEntity ent : ents) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
			}
			if (!ents.isEmpty()) data.applyStatus(StatusType.FOCUS, data, focus, -1);
			data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, damage * ents.size(), 0, StatTracker.damageBuffAlly(buffId, eq)), 160);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH, "On cast, charge <white>1s</white> before firing a projectile. If that projectile hits a block, "
			+ "apply " + DescUtil.potion("Slowness", 0, 3) + " to all enemies in a radius, increase damage by "
			+ DescUtil.yellow(damage) + " [<white>8s</white>] per enemy slowed, and gain " + GlossaryTag.FOCUS.tag(this, focus, true) + " if at least one enemy was slowed.");
	}
}
