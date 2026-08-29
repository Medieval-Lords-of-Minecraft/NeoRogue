package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Shockwave extends Equipment {
	private static final String ID = "Shockwave";
	private static final int ATTACKS_REQUIRED = 5;
	private static final double PROJECTILE_SPEED = 0.5;
	private static final double PROJECTILE_RANGE = 8;
	private static final int PROJECTILE_TICK_SPEED = 1;
	private static final double PROJECTILE_WIDTH = 1.5;
	private static final double PROJECTILE_HEIGHT = 0.4;
	private static final double PROJECTILE_Y = 0.6;
	private static final ParticleContainer WAVE_EDGE = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.PACKED_MUD.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer WAVE_DUST = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.05, 0.03).speed(0);
	private static final ParticleContainer LAUNCH_BURST = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DIRT.createBlockData()).count(10).spread(0.1, 0.05).speed(0.01);
	private static final ParticleContainer HIT_DEBRIS = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(16).spread(0.35, 0.25).speed(0.06);
	private static final ParticleContainer HIT_DUST = new ParticleContainer(Particle.DUST_PLUME)
			.count(8).spread(0.3, 0.15).speed(0.03);
	private static final SoundContainer LAUNCH_SOUND = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK, 0.75F, 0.65F);
	private static final SoundContainer HIT_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.65F, 0.7F);
	private final int damage;

	public Shockwave(boolean isUpgraded) {
		super(ID, "Shockwave", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 60 : 40;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta attacks = new ActionMeta();
		ProjectileGroup projectiles = new ProjectileGroup(new ShockwaveProjectile(data, slot));
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (attacks.addCount(1) < ATTACKS_REQUIRED) return TriggerResult.keep();
			attacks.setCount(0);

			Player player = data.getPlayer();
			Vector direction = player.getLocation().getDirection().setY(0);
			if (direction.lengthSquared() == 0) return TriggerResult.keep();
			LAUNCH_BURST.play(player, player.getLocation().add(0, 0.15, 0));
			LAUNCH_SOUND.play(player, player);
			projectiles.start(data, player.getLocation().add(0, PROJECTILE_Y, 0), direction.normalize());
			return TriggerResult.keep();
		});
	}

	private class ShockwaveProjectile extends Projectile {
		private final PlayerFightData data;
		private final int slot;

		private ShockwaveProjectile(PlayerFightData data, int slot) {
			super(PROJECTILE_SPEED, PROJECTILE_RANGE, PROJECTILE_TICK_SPEED);
			size(PROJECTILE_WIDTH, PROJECTILE_HEIGHT).pierce(-1);
			this.data = data;
			this.slot = slot;
		}

		@Override
		public void onStart(ProjectileInstance projectile) {
			projectile.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, Shockwave.this)));
		}

		@Override
		public void onTick(ProjectileInstance projectile, int interpolation) {
			Player player = data.getPlayer();
			Vector cross = projectile.getVelocity().clone().setY(0).normalize()
					.rotateAroundY(Math.toRadians(90)).multiply(PROJECTILE_WIDTH / 2);
			Location ground = projectile.getLocation().clone().add(0, 0.12 - PROJECTILE_Y, 0);
			ParticleUtil.drawLine(player, WAVE_EDGE, ground.clone().add(cross), ground.clone().subtract(cross), 0.25);
			ParticleUtil.drawLine(player, WAVE_DUST, ground.clone().add(cross), ground.clone().subtract(cross), 0.4);
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance projectile) {
			if (hitBarrier != null) return;
			Player player = data.getPlayer();
			HIT_DEBRIS.play(player, hit.getEntity().getLocation().add(0, 0.35, 0));
			HIT_DUST.play(player, hit.getEntity().getLocation().add(0, 0.45, 0));
			HIT_SOUND.play(player, hit.getEntity());
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MUD_BRICKS,
				GlossaryTag.PASSIVE.tag(this) + ". Every " + DescUtil.white(ATTACKS_REQUIRED)
						+ " basic attacks, launch a ground shockwave that deals "
						+ GlossaryTag.EARTHEN.tag(this, damage) + " damage to all enemies it hits.");
	}
}