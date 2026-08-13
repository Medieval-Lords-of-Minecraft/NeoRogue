package me.neoblade298.neorogue.equipment.accessories;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class UltimaBangle extends Equipment {
	private static final String ID = "UltimaBangle";
	private static final int INTERNAL_COOLDOWN = 2, FALL_TICKS = 40;
	private static final TargetProperties CAST_TARGETS = TargetProperties.radius(8, false, TargetType.ENEMY);
	private static final TargetProperties IMPACT_TARGETS = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final DamageType[] TYPES = { DamageType.FIRE, DamageType.ICE, DamageType.LIGHTNING,
			DamageType.EARTHEN, DamageType.DARK, DamageType.LIGHT };
	private static final ParticleContainer[] METEOR_PARTICLES = {
			meteorParticle(Color.fromRGB(255, 80, 35)), meteorParticle(Color.fromRGB(90, 220, 255)),
			meteorParticle(Color.fromRGB(255, 225, 55)), meteorParticle(Color.fromRGB(145, 95, 45)),
			meteorParticle(Color.fromRGB(90, 25, 135)), meteorParticle(Color.fromRGB(255, 245, 190))
	};
	private static final ParticleContainer[] IMPACT_EDGES = {
			impactEdge(Color.fromRGB(255, 80, 35)), impactEdge(Color.fromRGB(90, 220, 255)),
			impactEdge(Color.fromRGB(255, 225, 55)), impactEdge(Color.fromRGB(145, 95, 45)),
			impactEdge(Color.fromRGB(90, 25, 135)), impactEdge(Color.fromRGB(255, 245, 190))
	};
	private static final ParticleContainer[] IMPACT_BURSTS = {
			impactBurst(Color.fromRGB(255, 80, 35)), impactBurst(Color.fromRGB(90, 220, 255)),
			impactBurst(Color.fromRGB(255, 225, 55)), impactBurst(Color.fromRGB(145, 95, 45)),
			impactBurst(Color.fromRGB(90, 25, 135)), impactBurst(Color.fromRGB(255, 245, 190))
	};
	private static final Circle IMPACT_CIRCLE = new Circle(IMPACT_TARGETS.range);
	private static final ParticleContainer IMPACT_SMOKE = new ParticleContainer(Particle.CLOUD)
			.count(24).spread(1.5, 0.4).speed(0.06).offsetY(0.25);
	private int damage;

	public UltimaBangle(boolean isUpgraded) {
		super(ID, "Ultima Bangle", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		damage = isUpgraded ? 80 : 60;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta cooldown = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent event = (CastUsableEvent) in;
			if (event.getInstance().getEquipment().getType() != EquipmentType.ABILITY
					|| cooldown.getTime() > System.currentTimeMillis()) return TriggerResult.keep();
			Player player = data.getPlayer();
			List<LivingEntity> enemies = TargetHelper.getEntitiesInRadius(player, CAST_TARGETS);
			if (enemies.isEmpty()) return TriggerResult.keep();
			cooldown.setTime(System.currentTimeMillis() + INTERNAL_COOLDOWN * 1000L);
			LivingEntity target = enemies.get(ThreadLocalRandom.current().nextInt(enemies.size()));
			int typeIndex = ThreadLocalRandom.current().nextInt(TYPES.length);
			spawnMeteor(data, slot, target.getLocation().clone(), TYPES[typeIndex], METEOR_PARTICLES[typeIndex],
					IMPACT_EDGES[typeIndex], IMPACT_BURSTS[typeIndex]);
			return TriggerResult.keep();
		});
	}

	private static ParticleContainer meteorParticle(Color color) {
		return new ParticleContainer(Particle.DUST)
				.dustOptions(new DustOptions(color, 1.5F)).count(2).spread(0.08, 0.08).speed(0.01);
	}

	private static ParticleContainer impactEdge(Color color) {
		return new ParticleContainer(Particle.DUST)
				.dustOptions(new DustOptions(color, 1.1F)).count(1).spread(0, 0).speed(0);
	}

	private static ParticleContainer impactBurst(Color color) {
		return new ParticleContainer(Particle.DUST)
				.dustOptions(new DustOptions(color, 1.4F)).count(20).spread(1.2, 0.35).speed(0.05).offsetY(0.3);
	}

	private void spawnMeteor(PlayerFightData data, int slot, Location impact, DamageType type,
			ParticleContainer particle, ParticleContainer impactEdge, ParticleContainer impactBurst) {
		Location start = impact.clone().add(0, 12, 0);
		Sounds.firework.play(data.getPlayer(), start);
		data.addTask(new BukkitRunnable() {
			private int ticks;
			@Override
			public void run() {
				Player player = data.getPlayer();
				Location current = start.clone().add(0, -12.0 * ticks / FALL_TICKS, 0);
				particle.play(player, current);
				particle.play(player, current.clone().add(0, 0.4, 0));
				particle.play(player, current.clone().add(0, 0.8, 0));
				if (ticks % 5 == 0) IMPACT_CIRCLE.play(impactEdge, impact, LocalAxes.xz(), null);
				if (++ticks < FALL_TICKS) return;
				Sounds.explode.play(player, impact);
				IMPACT_CIRCLE.play(impactEdge, impact, LocalAxes.xz(), null);
				impactBurst.play(player, impact);
				IMPACT_SMOKE.play(player, impact);
				for (LivingEntity enemy : TargetHelper.getEntitiesInRadius(player, impact, IMPACT_TARGETS)) {
					FightInstance.dealDamage(new DamageMeta(data, damage, type,
							DamageStatTracker.of(id + slot, UltimaBangle.this)), enemy);
				}
				cancel();
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 1L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CLOCK, GlossaryTag.PASSIVE.tag(this) + ". Casting an ability drops a meteor on a random enemy within "
				+ DescUtil.white(8) + " blocks. After " + DescUtil.white("2s") + ", it deals "
				+ DescUtil.val(damage) + " damage of a random magical type in a " + DescUtil.white(5)
				+ " block radius. Internal cooldown: " + DescUtil.white(INTERNAL_COOLDOWN + "s") + ".");
	}
}