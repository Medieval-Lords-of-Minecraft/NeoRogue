package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MeteorShower extends Equipment {
	private static final String ID = "MeteorShower";
	private static final TargetProperties IMPACT = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static final Circle IMPACT_CIRCLE = new Circle(IMPACT.range);
	private static final Circle SHOWER_CIRCLE = new Circle(8);
	private static final ParticleContainer SHOWER_EDGE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT_EDGE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer DESCENT = new ParticleContainer(Particle.DUST)
			.dustOptions(new Particle.DustOptions(org.bukkit.Color.fromRGB(125, 55, 175), 1.2F))
			.count(1).spread(0.03, 0.03).speed(0);
	private static final ParticleContainer DESCENT_SPARK = new ParticleContainer(Particle.FIREWORK)
			.count(1).spread(0.04, 0.04).speed(0.01);
	private static final ParticleContainer IMPACT_PARTICLES = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(24).spread(0.1, 0.1).speed(0.01).offsetY(0.3);
	private static final ParticleContainer IMPACT_SMOKE = new ParticleContainer(Particle.LARGE_SMOKE)
			.count(10).spread(0.1, 0.1).speed(0.01).offsetY(0.35);
	private static final SoundContainer CAST_SOUND = new SoundContainer(Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.7F, 1.1F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.ENTITY_GENERIC_EXPLODE, 0.7F, 0.7F);
	private int channelTicks, meteorCount, showerRadius, damage, riftDurationTicks;

	public MeteorShower(boolean isUpgraded) {
		super(ID, "Meteor Shower", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(40, 0, 12, 0, IMPACT.range));
		channelTicks = 20;
		meteorCount = isUpgraded ? 5 : 4;
		showerRadius = 8;
		damage = 100;
		riftDurationTicks = 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player caster = data.getPlayer();
			SHOWER_CIRCLE.play(SHOWER_EDGE, caster.getLocation().clone().add(0, 0.08, 0), LocalAxes.xz(), null);
			CAST_SOUND.play(caster, caster);
			data.channel(channelTicks).then(() -> {
				Player p = data.getPlayer();
				Location center = p.getLocation();
				for (int i = 0; i < meteorCount; i++) impact(data, slot, randomLocation(center));
			});
			return TriggerResult.keep();
		}));
	}

	private Location randomLocation(Location center) {
		double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
		double radius = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * showerRadius;
		return center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
	}

	private void impact(PlayerFightData data, int slot, Location location) {
		Player p = data.getPlayer();
		Location ground = location.clone().add(0, 0.08, 0);
		Location sky = location.clone().add(0, 9, 0);
		IMPACT_CIRCLE.play(IMPACT_EDGE, ground, LocalAxes.xz(), null);
		ParticleUtil.drawLine(p, DESCENT, sky, location, 0.3);
		ParticleUtil.drawLine(p, DESCENT_SPARK, sky, location, 0.6);
		IMPACT_PARTICLES.play(p, location);
		IMPACT_SMOKE.play(p, location);
		IMPACT_SOUND.play(p, location);
		LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInRadius(p, location, IMPACT);
		for (LivingEntity target : targets) {
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
					DamageStatTracker.of(id + slot, this)), target);
		}
		if (!targets.isEmpty()) data.addRift(new Rift(data, location.clone(), riftDurationTicks, this));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				GlossaryTag.CHANNEL.tag(this) + " for " + DescUtil.val("1s") + ", then drop "
				+ DescUtil.val(meteorCount) + " meteors randomly within " + DescUtil.val(showerRadius)
				+ " blocks. Each deals " + GlossaryTag.DARK.tag(this, damage)
				+ " damage in an area and becomes a " + GlossaryTag.RIFT.tag(this) + " if it hits an enemy.");
	}
}
