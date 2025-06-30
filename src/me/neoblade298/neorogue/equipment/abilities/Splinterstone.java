package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
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
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Splinterstone extends Equipment {
	private static final String ID = "splinterstone";
	private static final ParticleContainer tick = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(5).spread(0.3, 0.3);
	private static final TargetProperties tp = TargetProperties.cone(60, 4, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ANCIENT_DEBRIS_BREAK);

	private int damage, pierce, conc;

	public Splinterstone(boolean isUpgraded) {
		super(ID, "Splinterstone", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 5, 13, 10));
		damage = isUpgraded ? 240 : 160;
		properties.add(PropertyType.DAMAGE, damage);
		pierce = isUpgraded ? 90 : 60;
		conc = isUpgraded ? 60 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new SplinterstoneProjectile(data, this, slot));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			data.channel(20).then(new Runnable() {
				public void run() {
					proj.start(data);
				}
			});
			return TriggerResult.keep();
		}));
	}

	private class SplinterstoneProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private Equipment eq;
		private int slot;

		public SplinterstoneProjectile(PlayerFightData data, Equipment eq, int slot) {
			super(1, properties.get(PropertyType.RANGE), 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.p = data.getPlayer();
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Vector forward = proj.getVelocity().clone().setY(0).normalize();
			Vector up = new Vector(0, 1, 0);
			Vector left = forward.clone().crossProduct(up);
			LivingEntity ent = hit.getEntity();
			hit.applyStatus(StatusType.CONCUSSED, data, conc, -1);
			cone.play(tick, ent.getLocation(), new LocalAxes(left, up, forward), tick);
			sc.play(p, ent.getLocation());
			for (LivingEntity tmp : TargetHelper.getEntitiesInCone(p, ent.getLocation(), forward, tp)) {
				if (tmp == hit.getEntity())
					continue;
				FightInstance.dealDamage(new DamageMeta(data, pierce, DamageType.PIERCING,
						DamageStatTracker.of(id + slot, eq)), tmp);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			proj.addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN, DamageStatTracker.of(ID + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DRIPSTONE_BLOCK,
				GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before launching a projectile that deals "
						+ GlossaryTag.EARTHEN.tag(this, damage, true) + " damage and applies "
						+ GlossaryTag.CONCUSSED.tag(this, conc, true) + ". If an enemy is hit, " + "deal "
						+ GlossaryTag.PIERCING.tag(this, pierce, false)
						+ " damage to all enemies in a cone behind them.");
	}
}
