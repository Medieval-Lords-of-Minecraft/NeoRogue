package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class TheHunt extends Equipment {
	private static final String ID = "TheHunt";
	private static final int RANGE = 18, PIERCE = 6;
	private static final ParticleContainer CORE = new ParticleContainer(Particle.FIREWORK)
			.count(2).spread(0.03, 0.03).speed(0.01);
	private static final ParticleContainer SPIRAL = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(255, 196, 64), 0.9F))
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer LAUNCH = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(Color.fromRGB(90, 225, 255), 1.2F))
			.count(10).spread(0.1, 0.1).speed(0.01);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.FIREWORK)
			.count(14).spread(0.1, 0.1).speed(0.01);
	private int focusThreshold = 5, damagePerFocus;

	public TheHunt(boolean isUpgraded) {
		super(ID, "The Hunt", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damagePerFocus = isUpgraded ? 25 : 20;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			int focus = data.getStatus(StatusType.FOCUS).getStacks();
			if (!event.isBasicAttack() || focus <= focusThreshold) return TriggerResult.keep();
			data.addAftershot(new ProjectileGroup(new HuntProjectile(data, slot, focus * damagePerFocus)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TARGET, GlossaryTag.PASSIVE.tag(this) + ". While above "
				+ DescUtil.val(focusThreshold) + " " + GlossaryTag.FOCUS.tag(this)
				+ ", launching a basic attack also fires a piercing " + GlossaryTag.AFTERSHOT.tag(this) + " that deals "
				+ GlossaryTag.PIERCING.tag(this, damagePerFocus) + " damage per current Focus.");
	}

	private class HuntProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot, damage;

		private HuntProjectile(PlayerFightData data, int slot, int damage) {
			super(RANGE, 1);
			this.data = data;
			this.slot = slot;
			this.damage = damage;
			setBowDefaults();
			pierce(PIERCE);
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			Player player = data.getPlayer();
			Location location = projectile.getLocation();
			CORE.play(player, location);
			Vector forward = location.getDirection().normalize();
			Vector side = forward.clone().crossProduct(new Vector(0, 1, 0));
			if (side.lengthSquared() < 0.001) side.setX(1);
			side.normalize();
			Vector up = side.clone().crossProduct(forward).normalize();
			double angle = interpolation * 0.8;
			Vector offset = side.multiply(Math.cos(angle) * 0.18).add(up.multiply(Math.sin(angle) * 0.18));
			SPIRAL.play(player, location.clone().add(offset));
			SPIRAL.play(player, location.clone().subtract(offset));
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			Player player = data.getPlayer();
			IMPACT.play(player, projectile.getLocation());
			Sounds.crit.play(player, projectile.getLocation());
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING,
					DamageStatTracker.of(id + slot, TheHunt.this)));
			Player player = data.getPlayer();
			LAUNCH.play(player, projectile.getLocation());
			Sounds.shoot.play(player, projectile.getLocation());
		}
	}
}