package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SunderingShot extends Equipment {
	private static final String ID = "SunderingShot";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).count(50).spread(0.5, 0.5);
	private int damage;
	private static final int MAX = 3;
	
	public SunderingShot(boolean isUpgraded) {
		super(ID, "Sundering Shot", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 15, 10));
				damage = isUpgraded ? 160 : 120;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		Equipment eq = this;
		inst.setAction((pdata, in) -> {
			Sounds.piano.play(p, p);
			data.charge(40);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Vector v = p.getEyeLocation().getDirection();
					p.setVelocity(v.setY(0).setX(-v.getX()).setZ(-v.getZ()).normalize().multiply(0.4).setY(0.1));
					ProjectileGroup projs = new ProjectileGroup(new SunderingShotProjectile(data, eq, slot));
					projs.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	private class SunderingShotProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private AmmunitionInstance ammo;
		private Equipment eq;
		private int slot;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public SunderingShotProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(properties.get(PropertyType.RANGE), 1);
			this.size(1.5, 1.5);
			this.pierce(-1);
			this.data = data;
			this.p = data.getPlayer();
			this.ammo = data.getAmmoInstance();
			this.eq = eq;
			this.slot = slot;

			blocksPerTick(2);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.explode.play(p, hit.getEntity());
			ammo.onHit(proj, meta, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			Sounds.explode.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			int stacks = Math.min(MAX, data.getStatus(StatusType.FOCUS).getStacks());
			dm.addDamageSlice(new DamageSlice(data, damage * stacks, DamageType.PIERCING, DamageStatTracker.of(ID + slot, eq)));
			dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, ammoProps.get(PropertyType.DAMAGE),
				BuffStatTracker.damageBuffAlly(ammo.getAmmo().getId(), ammo.getAmmo())));
			ammo.onStart(proj);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, " + DescUtil.charge(this, 1, 2) + " before firing a piercing projectile that deals " +
					GlossaryTag.PIERCING.tag(this, damage, true) + " damage for every stack of " + GlossaryTag.FOCUS.tag(this) + " you have, up to " +
					DescUtil.white(MAX) + ". Uses your current ammunition.");
	}
}
