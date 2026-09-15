package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class LightTheSky extends Equipment implements Power {
	private static final String ID = "LightTheSky";
	private static final String BOLT_DAMAGE_TAG = ID + "-bolt";
	private static final int ACTIVATION_THRESHOLD = 50;
	private static final int ELECTRIFIED_THRESHOLD = 10;
	private static final int MANA_GAIN = 15;
	private static final int BOLT_COUNT = 2;
	private static final int BOLT_RANGE = 6;
	private static final int BOLT_AOE = 2;
	private static final int SLOW_AMPLIFIER = 1;
	private static final int SLOW_DURATION_TICKS = 40;
	private static final ParticleContainer BOLT_CORE = new ParticleContainer(Particle.END_ROD)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer BOLT_GLOW = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 195, 255), 1.2F))
			.count(1).spread(0.04, 0.04).speed(0);
	private static final ParticleContainer IMPACT_EDGE = new ParticleContainer(Particle.FIREWORK)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer IMPACT_SPARKS = new ParticleContainer(Particle.FIREWORK)
			.count(16).spread(0.1, 0.1).speed(0.01).offsetY(0.25);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7F, 1.25F);
	private final double damageMultiplier;
	private final int damageMultiplierPercent;
	private final int boltDamage;
	private final TargetProperties boltTargets;
	private final Circle boltCircle;

	public LightTheSky(boolean isUpgraded) {
		super(ID, "Light the Sky", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.custom(0, 0, 0, BOLT_RANGE, isUpgraded ? 140 : 120, 0, 0,
						DamageType.LIGHTNING, null).add(PropertyType.AREA_OF_EFFECT, BOLT_AOE));
		damageMultiplier = isUpgraded ? 0.8 : 0.6;
		damageMultiplierPercent = (int) (damageMultiplier * 100);
		boltDamage = (int) properties.get(PropertyType.DAMAGE);
		double boltAoe = properties.get(PropertyType.AREA_OF_EFFECT);
		boltTargets = TargetProperties.radius(boltAoe, false, TargetType.ENEMY);
		boltCircle = new Circle(boltAoe);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activationProgress = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.ELECTRIFIED) || event.getStacks() <= 0) return TriggerResult.keep();
			if (activationProgress.addCount(event.getStacks()) < ACTIVATION_THRESHOLD) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id + "-active", Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			FightData targetData = FightInstance.getFightData(event.getTarget());
			if (event.getMeta().hasTag(BOLT_DAMAGE_TAG)
					|| !event.getMeta().containsType(DamageType.LIGHTNING)
					|| targetData == null
					|| !targetData.hasStatus(StatusType.ELECTRIFIED)
					|| targetData.getStatus(StatusType.ELECTRIFIED).getStacks() < ELECTRIFIED_THRESHOLD) {
				return TriggerResult.keep();
			}

			event.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.LIGHTNING),
					Buff.multiplier(data, damageMultiplier, BuffStatTracker.damageBuffAlly(buffId, this)));
			data.addMana(MANA_GAIN);
			Location center = event.getTarget().getLocation();
			for (int bolt = 0; bolt < BOLT_COUNT; bolt++) strikeBolt(data, slot, randomLocation(center));
			return TriggerResult.keep();
		});
	}

	private Location randomLocation(Location center) {
		double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
		double radius = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * properties.get(PropertyType.RANGE);
		return center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
	}

	private void strikeBolt(PlayerFightData data, int slot, Location location) {
		Player player = data.getPlayer();
		Location ground = location.clone().add(0, 0.08, 0);
		Location sky = location.clone().add(0, 8, 0);
		ParticleUtil.drawLine(player, BOLT_CORE, sky, location, 0.3);
		ParticleUtil.drawLine(player, BOLT_GLOW, sky, location, 0.25);
		boltCircle.play(IMPACT_EDGE, ground, LocalAxes.xz(), null);
		IMPACT_SPARKS.play(player, location);
		IMPACT_SOUND.play(player, location);
		for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, location, boltTargets)) {
			DamageMeta meta = new DamageMeta(data, boltDamage, DamageType.LIGHTNING,
					DamageStatTracker.of(id + slot, this));
			meta.addTag(BOLT_DAMAGE_TAG);
			FightInstance.dealDamage(meta, target);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_DURATION_TICKS, SLOW_AMPLIFIER));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this)
						+ ". Activates after applying " + GlossaryTag.ELECTRIFIED.tag(this, ACTIVATION_THRESHOLD)
						+ ". Dealing " + GlossaryTag.LIGHTNING.tag(this) + " damage to enemies with "
						+ GlossaryTag.ELECTRIFIED.tag(this, ELECTRIFIED_THRESHOLD) + " increases the damage by "
						+ DescUtil.val(damageMultiplierPercent + "%") + ", grants " + DescUtil.val(MANA_GAIN)
						+ " mana, and drops " + DescUtil.val(BOLT_COUNT) + " bolts randomly within "
						+ DescUtil.val((int) properties.get(PropertyType.RANGE)) + " blocks. Each bolt deals "
						+ GlossaryTag.LIGHTNING.tag(this, boltDamage) + " damage in a "
						+ DescUtil.val((int) properties.get(PropertyType.AREA_OF_EFFECT)) + " block area and applies "
						+ DescUtil.potion("Slowness", SLOW_AMPLIFIER, SLOW_DURATION_TICKS / 20) + ".");
	}
}