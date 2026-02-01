package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Depletion extends Equipment {
	private static final String ID = "Depletion";
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).dustOptions(new DustOptions(Color.BLACK, 1F));
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private int damage, shields;
	
	public Depletion(boolean isUpgraded) {
		super(ID, "Depletion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 0, 12, 10));
		damage = 250;
		shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		ProjectileGroup proj = new ProjectileGroup(new DarkTorrentProjectile(data, slot, this));
		inst.setAction((pdata, in) -> {
			data.charge(20);
			data.addTask(new BukkitRunnable() {
				public void run() {
					proj.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 20));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}
	
	private class DarkTorrentProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private int slot;
		private Equipment eq;

		public DarkTorrentProjectile(PlayerFightData data, int slot, Equipment eq) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
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
			Location loc = hit.getEntity().getLocation();
			data.addRift(new Rift(data, loc, 200));
			Sounds.infect.play(p, loc);
			circ.play(pc, loc, LocalAxes.xz(), null);
			LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInRadius(p, loc, tp);
			data.addSimpleShield(p.getUniqueId(), shields * trgs.size(), 160);
			for (LivingEntity ent : trgs) {
				Vector v = loc.toVector().subtract(ent.getLocation().toVector());
				if (v.isZero()) continue;
				v = v.normalize();
				FightInstance.knockback(ent, v);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.fire.play(p, p);
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COAL, "On cast, " + DescUtil.charge(this, 1, 1) + " before firing a projectile that deals "
				+ GlossaryTag.DARK.tag(this, damage, true) + " damage. If you hit an enemy, create a " + GlossaryTag.RIFT.tag(this) + " [<white>10s</white>] at their location. " +
				"The new " + GlossaryTag.RIFT.tag(this) + " pulls in nearby enemies and grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>8s</white>] for every enemy pulled.");
	}
}
