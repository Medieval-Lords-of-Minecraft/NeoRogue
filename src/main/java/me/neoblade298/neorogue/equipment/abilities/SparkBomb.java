package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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

public class SparkBomb extends Equipment {
	private static final String ID = "SparkBomb";
	private static final int DETONATION_DELAY = 20, SHIELD_DURATION = 100;
	private static final double AREA_OF_EFFECT = 5;
	private static final TargetProperties TARGETS = TargetProperties.radius(AREA_OF_EFFECT, true, TargetType.ENEMY);
	private static final Circle DETONATION_RING = new Circle(AREA_OF_EFFECT), SHIELD_RING = new Circle(1.15);
	private static final ParticleContainer BOMB_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(80, 210, 255), 1F));
	private static final ParticleContainer BOLT_PARTICLE = new ParticleContainer(Particle.FIREWORK).count(1)
			.spread(0.03, 0.03).speed(0);
	private static final ParticleContainer EXPLOSION_PARTICLE = new ParticleContainer(Particle.FIREWORK).count(35)
			.spread(1, 0.6).offsetY(0.5).speed(0.12);
	private static final ParticleContainer RING_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(90, 190, 255), 0.8F));
	private static final ParticleContainer SHIELD_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(255, 230, 120), 1F));
	private static final ParticleContainer SHIELD_SPARK = new ParticleContainer(Particle.FIREWORK).count(10)
			.spread(0.1, 0.6).offsetY(1).speed(0.01);
	private static final ParticleContainer STATUS_IMPACT = new ParticleContainer(Particle.FIREWORK).count(5)
			.spread(0.1, 0.35).offsetY(1).speed(0.01);
	private static final ParticleContainer DAMAGE_DISCHARGE = new ParticleContainer(Particle.CRIT).count(18)
			.spread(0.8, 0.35).offsetY(0.5).speed(0.08);
	private static final ParticleContainer DAMAGE_IMPACT = new ParticleContainer(Particle.ENCHANTED_HIT).count(9)
			.spread(0.1, 0.4).offsetY(1).speed(0.01);
	private static final SoundContainer PLACE_SOUND = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED, 0.55F, 1.6F);
	private static final SoundContainer SHIELD_SOUND = new SoundContainer(Sound.ITEM_SHIELD_BLOCK, 0.65F, 1.35F);
	private static final SoundContainer DAMAGE_SOUND = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.65F, 0.8F);
	private static final ParticleAnimation BOMB_TELEGRAPH;

	static {
		BOMB_TELEGRAPH = new ParticleAnimation(BOMB_PARTICLE, (loc, tick) -> {
			LinkedList<Location> locations = new LinkedList<>();
			locations.add(loc.clone().add(0, 0.18, 0));
			double rotation = tick * Math.PI / 10;
			for (int i = 0; i < 6; i++) {
				double angle = rotation + i * Math.PI / 3;
				locations.add(loc.clone().add(Math.cos(angle) * 0.32, 0.2, Math.sin(angle) * 0.32));
			}
			if (tick % 4 == 0) {
				for (int i = 0; i < 32; i++) {
					double angle = i * Math.PI / 16;
					locations.add(loc.clone().add(Math.cos(angle) * AREA_OF_EFFECT, 0.08,
							Math.sin(angle) * AREA_OF_EFFECT));
				}
			}
			return locations;
		}, DETONATION_DELAY);
	}
	private int shields, electrified, damage;

	public SparkBomb(boolean isUpgraded) {
		super(ID, "Spark Bomb", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 12, 0, AREA_OF_EFFECT));
		shields = isUpgraded ? 9 : 6;
		electrified = isUpgraded ? 6 : 4;
		damage = isUpgraded ? 150 : 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Location bombLocation = p.getLocation().clone();
			PLACE_SOUND.play(p, bombLocation);
			data.runAnimation(id + "-telegraph", p, BOMB_TELEGRAPH, bombLocation);
			data.addTask(new BukkitRunnable() {
				@Override
				public void run() {
					Player p = data.getPlayer();
					playLightningStrike(p, bombLocation);
					EXPLOSION_PARTICLE.play(p, bombLocation);
					DETONATION_RING.play(p, RING_PARTICLE, bombLocation, LocalAxes.xz(), null);
					Sounds.thunder.play(p, bombLocation);
					Sounds.explode.play(p, bombLocation);
					boolean casterInside = p.getWorld() == bombLocation.getWorld()
							&& p.getLocation().distanceSquared(bombLocation) <= AREA_OF_EFFECT * AREA_OF_EFFECT;
					if (!casterInside) {
						DAMAGE_DISCHARGE.play(p, bombLocation);
						DAMAGE_SOUND.play(p, bombLocation);
					}

					for (LivingEntity target : TargetHelper.getEntitiesInRadius(p, bombLocation, TARGETS)) {
						if (casterInside) {
							STATUS_IMPACT.play(p, target);
							FightInstance.applyStatus(target, StatusType.ELECTRIFIED, data, electrified, -1,
									SparkBomb.this);
						}
						else {
							DAMAGE_IMPACT.play(p, target);
							FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING,
									DamageStatTracker.of(id + slot, SparkBomb.this)), target);
						}
					}

					if (casterInside) {
						SHIELD_RING.play(p, SHIELD_PARTICLE, p.getLocation().add(0, 1, 0), LocalAxes.xz(), null);
						SHIELD_SPARK.play(p, p);
						SHIELD_SOUND.play(p, p);
						data.addSimpleShield(p.getUniqueId(), shields, SHIELD_DURATION, SparkBomb.this);
					}
				}
			}.runTaskLater(NeoRogue.inst(), DETONATION_DELAY));
			return TriggerResult.keep();
		}));
	}

	private static void playLightningStrike(Player player, Location impact) {
		Location from = impact.clone().add(0, 9, 0);
		for (int i = 1; i <= 6; i++) {
			double height = 9 - i * 1.5;
			Location to = impact.clone().add(i == 6 ? 0 : (i % 2 == 0 ? -0.28 : 0.28), height,
					i == 6 ? 0 : (i % 3 - 1) * 0.2);
			ParticleUtil.drawLine(player, BOLT_PARTICLE, from, to, 0.22);
			from = to;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIREWORK_STAR,
				"On cast, drop a bomb at your feet that explodes after " + DescUtil.val("1s")
				+ ". If you are within its radius, gain " + GlossaryTag.SHIELDS.tag(this, shields) + " "
				+ DescUtil.duration(5) + " and apply " + GlossaryTag.ELECTRIFIED.tag(this, electrified)
				+ " to enemies. Otherwise, deal " + GlossaryTag.LIGHTNING.tag(this, damage)
				+ " damage to enemies.");
	}
}