package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class SunderingShot extends Equipment {
	private static final String ID = "sunderingShot";
	private static final ParticleContainer pc = new ParticleContainer(Particle.REDSTONE).count(50).spread(0.5, 0.5);
	private int damage;
	
	public SunderingShot(boolean isUpgraded) {
		super(ID, "Sundering Shot", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 15, 10));
				damage = isUpgraded ? 160 : 120;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			data.charge(40);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Vector v = p.getEyeLocation().getDirection();
					p.setVelocity(v.setY(0).setX(-v.getX()).setZ(-v.getZ()).normalize().multiply(0.4).setY(0.1));
					ProjectileGroup projs = new ProjectileGroup(new SunderingShotProjectile(data, inst.getCount()));
					inst.setCount(0);
					icon.setAmount(1);
					inst.setIcon(icon);
					projs.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FOCUS)) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			Sounds.fire.play(p, p);
			icon.setAmount(inst.getCount());
			inst.setIcon(icon);
			return TriggerResult.keep();
		});
	}

	private class SunderingShotProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private AmmunitionInstance ammo;
		private int stacks;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public SunderingShotProjectile(PlayerFightData data, int stacks) {
			super(properties.get(PropertyType.RANGE), 1);
			this.size(1.5, 1.5);
			this.pierce(-1);
			this.data = data;
			this.p = data.getPlayer();
			this.ammo = data.getAmmoInstance();
			this.stacks = stacks;

			blocksPerTick(2);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
			ammo.onTick(p, proj, interpolation);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			Sounds.explode.play(p, hit.getEntity());
			damageProjectile(hit.getEntity(), proj, hitBarrier); 
			ammo.onHit(proj, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			Sounds.explode.play(p, p);
			DamageMeta dm = proj.getMeta();
			EquipmentProperties ammoProps = ammo.getProperties();
			dm.addDamageSlice(new DamageSlice(data, damage * stacks, DamageType.PIERCING));
			dm.addDamageSlice(new DamageSlice(data, ammoProps.get(PropertyType.DAMAGE), ammoProps.getType()));
			ammo.onStart(proj);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>2s</white> before firing a piercing projectile that deals " +
					GlossaryTag.PIERCING.tag(this, damage, true) + " damage for every stack of " + GlossaryTag.FOCUS.tag(this) + " you gained since the last " +
					"time you used this ability. Uses your current ammunition.");
	}
}
