package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Sear extends Equipment {
	private static final String ID = "sear";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(10).spread(0.5, 0.2).offsetY(-0.3);
	private int damage, burn;
	
	public Sear(boolean isUpgraded) {
		super(ID, "Sear", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 13, 6));
		damage = isUpgraded ? 60 : 40;
		burn = isUpgraded ? 75 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new SearProjectile(data));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		}));
	}
	
	private class SearProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public SearProjectile(PlayerFightData data) {
			super(properties.get(PropertyType.RANGE), 2);
			this.size(4, 1);
			this.pierce(-1);
			this.data = data;
			this.p = data.getPlayer();

			blocksPerTick(2);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
			Vector v = proj.getVelocity().clone().normalize();
			Location left = proj.getLocation().clone().add(v.clone().rotateAroundY(Math.PI / 2).multiply(2));
			Location right = proj.getLocation().clone().add(v.clone().rotateAroundY(-Math.PI / 2).multiply(2));
			pc.play(p, left);
			pc.play(p, right);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			Sounds.extinguish.play(p, hit.getEntity());
			damageProjectile(hit.getEntity(), proj, hitBarrier);
			if (!hit.hasStatus(StatusType.BURN)) {
				hit.applyStatus(StatusType.BURN, data, burn, -1);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			DamageMeta dm = proj.getMeta();
			dm.addDamageSlice(new DamageSlice(data, damage, DamageType.FIRE));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER, "On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>1s</white> before firing a <white>4</white> block wide piercing projectile that deals "
			+ GlossaryTag.FIRE.tag(this, damage, true) + " damage. Any enemies that do not have any stacks of " + GlossaryTag.BURN.tag(this) +
			" will receive " + GlossaryTag.BURN.tag(this, burn, true) + ".");
	}
}
