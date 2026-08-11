package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Judgment extends Equipment {
	private static final String ID = "Judgment";
	private static final int CENTRAL_RADIUS = 4;
	private static final int WAVE_DISTANCE = 10;
	private static final TargetProperties CENTRAL_TARGETS = TargetProperties.radius(CENTRAL_RADIUS, false, TargetType.ENEMY);
	private static final TargetProperties WAVE_TARGETS = TargetProperties.radius(1.5, false, TargetType.ENEMY);
	private static final ParticleContainer DIVINE = new ParticleContainer(Particle.DUST).count(1).spread(0, 0)
			.dustOptions(new DustOptions(Color.fromRGB(255, 232, 130), 1.2F));
	private static final ParticleContainer ASCENT = new ParticleContainer(Particle.DUST).count(4).spread(0.1, 0.1)
			.speed(0.01).dustOptions(new DustOptions(Color.fromRGB(255, 232, 130), 1F));
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.FLASH).count(3)
			.color(Color.fromRGB(255, 232, 130));
	private static final ParticleContainer IMPACT_EDGE = DIVINE.clone().count(1).spread(0, 0);
	private static final ParticleContainer IMPACT_FILL = new ParticleContainer(Particle.DUST).count(1).spread(0.1, 0)
			.speed(0).dustOptions(new DustOptions(Color.fromRGB(255, 232, 130), 1F));
	private static final ParticleContainer WAVE = new ParticleContainer(Particle.DUST_PLUME).count(2).spread(0.05, 0.05)
			.speed(0);
	private static final ParticleContainer WAVE_HIT = new ParticleContainer(Particle.BLOCK).count(8).spread(0.1, 0.1)
			.speed(0.01).blockData(Material.PACKED_MUD.createBlockData());
	private static final Circle IMPACT_AREA = new Circle(CENTRAL_RADIUS);
	private static final Vector[] DIRECTIONS = {
			new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1),
			new Vector(1, 0, 1).normalize(), new Vector(1, 0, -1).normalize(),
			new Vector(-1, 0, 1).normalize(), new Vector(-1, 0, -1).normalize()
	};
	private int damage, status;

	public Judgment(boolean isUpgraded) {
		super(ID, "Judgment", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(40, 100, 30, 0, CENTRAL_RADIUS));
		damage = isUpgraded ? 900 : 600;
		status = isUpgraded ? 18 : 12;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			p.setVelocity(new Vector(0, 1, 0));
			Sounds.flap.play(p, p);
			data.addTask(new BukkitRunnable() {
				private int ticks;
				@Override
				public void run() {
					Player current = data.getPlayer();
					ASCENT.play(current, current.getLocation().add(0, 0.2, 0));
					if (++ticks >= 10) cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 1));
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					data.getPlayer().setVelocity(new Vector(0, -2, 0));
				}
			}.runTaskLater(NeoRogue.inst(), 10));
			data.addTask(new BukkitRunnable() {
				private int ticks;
				@Override
				@SuppressWarnings("deprecation")
				public void run() {
					Player current = data.getPlayer();
					if (current.isOnGround()) {
						cancel();
						impact(data, slot);
						return;
					} else {
						showHammer(current);
					}
					if (++ticks >= 22) {
						cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 10, 1));
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			inst.addCooldown(-1);
			return TriggerResult.keep();
		});
	}

	private void impact(PlayerFightData data, int slot) {
		Player p = data.getPlayer();
		Location origin = p.getLocation();
		IMPACT_AREA.play(IMPACT_EDGE, origin, LocalAxes.xz(), IMPACT_FILL);
		IMPACT.play(p, origin);
		Sounds.explode.play(p, p);
		Set<UUID> waveHits = new HashSet<>();
		for (Vector direction : DIRECTIONS) launchWave(data, origin, direction, slot, waveHits);
		for (LivingEntity target : TargetHelper.getEntitiesInRadius(p, origin, CENTRAL_TARGETS)) {
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT,
					DamageStatTracker.of(id + slot, this)), target);
			FightInstance.applyStatus(target, StatusType.SANCTIFIED, data, status, -1, this);
		}
	}

	private void showHammer(Player p) {
		Location base = p.getLocation().add(0, 0.3, 0);
		Vector facing = p.getEyeLocation().getDirection().setY(0).normalize();
		Vector cross = facing.clone().rotateAroundY(Math.toRadians(90));
		Location head = base.clone().add(0, 2.2, 0).add(facing.clone().multiply(0.6));
		ParticleUtil.drawLine(p, DIVINE, base, head, 0.25);
		ParticleUtil.drawLine(p, DIVINE, head.clone().add(cross.clone().multiply(0.8)),
				head.clone().subtract(cross.clone().multiply(0.8)), 0.2);
	}

	private void launchWave(PlayerFightData data, Location origin, Vector direction, int slot, Set<UUID> waveHits) {
		data.addTask(new BukkitRunnable() {
			private int distance;
			@Override
			public void run() {
				if (++distance > WAVE_DISTANCE) {
					cancel();
					return;
				}
				Player p = data.getPlayer();
				Location point = origin.clone().add(direction.clone().multiply(distance));
				Vector cross = direction.clone().rotateAroundY(Math.toRadians(90)).multiply(0.8);
				ParticleUtil.drawLine(p, WAVE, point.clone().add(cross), point.clone().subtract(cross), 0.2);
				for (LivingEntity target : TargetHelper.getEntitiesInRadius(p, point, WAVE_TARGETS)) {
					if (!waveHits.add(target.getUniqueId())) continue;
					WAVE_HIT.play(p, target.getLocation().add(0, 0.2, 0));
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
							DamageStatTracker.of(id + slot, Judgment.this)), target);
					FightInstance.applyStatus(target, StatusType.CONCUSSED, data, status, -1, Judgment.this);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 1, 1));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HEAVY_CORE,
				"On cast, leap up and slam down with a divine hammer. Enemies within " + CENTRAL_RADIUS
						+ " blocks take " + GlossaryTag.LIGHT.tag(this, damage) + " damage and "
						+ GlossaryTag.SANCTIFIED.tag(this, status) + ". Eight traveling shockwaves each deal "
						+ GlossaryTag.EARTHEN.tag(this, damage) + " damage and apply "
						+ GlossaryTag.CONCUSSED.tag(this, status) + ". Basic attacks reduce the cooldown by 1s.");
	}
}