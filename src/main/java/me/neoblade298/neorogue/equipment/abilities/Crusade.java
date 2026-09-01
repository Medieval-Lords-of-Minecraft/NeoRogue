package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Crusade extends Equipment implements Power {
	private static final String ID = "Crusade";
	private static final int ACTIVATION_THRES = 5, RANGE = 20;
	private static final long FORMATION_TICKS = 10L;
	private static final ParticleContainer blade = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(255, 238, 150), 0.8F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer glow = new ParticleContainer(Particle.FIREWORK).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer launch = new ParticleContainer(Particle.FIREWORK).count(3).spread(0.05, 0.05).speed(0.01);
	private static final SoundContainer formSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7F, 1.2F);
	private int damage;

	public Crusade(boolean isUpgraded) {
		super(ID, "Crusade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 90 : 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
			if (am.addCount(1) < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ProjectileGroup projectiles = new ProjectileGroup(new CrusadeProjectile(slot, this));
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata, in) -> {
					ApplyStatusEvent ev = (ApplyStatusEvent) in;
					if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
					queueSwords(data, projectiles, ev.getTarget().getUniqueId(), 1);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	private void queueSwords(PlayerFightData data, ProjectileGroup projectiles, UUID targetId, int count) {
		for (int index = 0; index < count; index++) {
			int swordIndex = index;
			ThreadLocalRandom random = ThreadLocalRandom.current();
			Vector swordOffset = new Vector(random.nextDouble(-1.25, 1.25), random.nextDouble(0.75, 1.75),
					random.nextDouble(-1.25, 1.25));
			data.addTask(new BukkitRunnable() {
				private int ticks;

				@Override
				public void run() {
					Entity entity = Bukkit.getEntity(targetId);
					if (!(entity instanceof LivingEntity target) || !target.isValid() || target.isDead()) {
						cancel();
						return;
					}

					Player player = data.getPlayer();
					Location origin = player.getEyeLocation().add(swordOffset);
					Vector direction = target.getEyeLocation().toVector().subtract(origin.toVector()).normalize();
					drawSword(player, origin, direction);
					if (swordIndex == 0 && ticks == 0) formSound.play(player, player);

					if (ticks < FORMATION_TICKS) {
						ticks += 2;
						return;
					}

					launch.play(player, origin);
					if (swordIndex == 0) Sounds.shoot.play(player, player);
					projectiles.start(data, origin, direction);
					cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 2L));
		}
	}

	private void drawSword(Player player, Location tip, Vector direction) {
		Vector forward = direction.clone().normalize();
		Vector right = forward.clone().crossProduct(new Vector(0, 1, 0));
		if (right.lengthSquared() == 0) right = new Vector(1, 0, 0);
		right.normalize();

		for (double distance = 0; distance <= 1; distance += 0.15) {
			blade.play(player, tip.clone().subtract(forward.clone().multiply(distance)));
		}
		Location hilt = tip.clone().subtract(forward.clone().multiply(0.75));
		for (double offset = -0.25; offset <= 0.25; offset += 0.1) {
			glow.play(player, hilt.clone().add(right.clone().multiply(offset)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying "
				+ GlossaryTag.SANCTIFIED.tag(this) + " " + DescUtil.val(ACTIVATION_THRES) + " times. After activation, applying "
				+ GlossaryTag.SANCTIFIED.tag(this) + " forms a sword that fires after " + DescUtil.val("0.5s")
				+ ", dealing " + GlossaryTag.LIGHT.tag(this, damage) + " damage each.");
	}

	private class CrusadeProjectile extends Projectile {
		private final int slot;
		private final Equipment eq;

		public CrusadeProjectile(int slot, Equipment eq) {
			super(2, RANGE, 1);
			this.size(0.4, 0.4);
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			drawSword((Player) proj.getOwner().getEntity(), proj.getLocation(), proj.getVelocity());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.glass.play((Player) proj.getOwner().getEntity(), hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.LIGHT,
					DamageStatTracker.of(id + slot, eq)));
		}
	}
}