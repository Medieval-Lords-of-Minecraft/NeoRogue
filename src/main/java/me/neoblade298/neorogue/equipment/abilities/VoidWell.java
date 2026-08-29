package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
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

public class VoidWell extends Equipment {
	private static final String ID = "VoidWell";
	private static final TargetProperties AREA = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle AREA_CIRCLE = new Circle(AREA.range);
	private static final Circle INNER_RING = new Circle(1.4);
	private static final ParticleContainer VOID_EDGE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer VOID_FILL = new ParticleContainer(Particle.SOUL)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer VOID_CORE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(16).spread(0.1, 0.1).speed(0.01).offsetY(0.35);
	private static final SoundContainer WELL_SOUND = new SoundContainer(Sound.BLOCK_PORTAL_TRIGGER, 0.7F, 0.75F);
	private int shields, shieldDurationTicks, slowLevel, slowDurationTicks, damage, riftDurationTicks;
	private double pullStrength;

	public VoidWell(boolean isUpgraded) {
		super(ID, "Void Well", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 8, 0, AREA.range));
		shields = 6;
		shieldDurationTicks = (isUpgraded ? 6 : 4) * 20;
		slowLevel = 2;
		slowDurationTicks = 40;
		damage = isUpgraded ? 150 : 100;
		riftDurationTicks = 200;
		pullStrength = 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Location center = p.getLocation().clone();
			data.addSimpleShield(p.getUniqueId(), shields, shieldDurationTicks, this);
			data.addRift(new Rift(data, center, riftDurationTicks, this));
			Location ground = center.clone().add(0, 0.08, 0);
			AREA_CIRCLE.play(VOID_EDGE, ground, LocalAxes.xz(), VOID_FILL);
			INNER_RING.play(VOID_EDGE, ground, LocalAxes.xz(), null);
			VOID_CORE.play(p, center);
			WELL_SOUND.play(p, center);
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(p, center, AREA)) {
				Vector pull = center.toVector().subtract(target.getLocation().toVector());
				if (!pull.isZero()) target.setVelocity(pull.normalize().multiply(pullStrength).setY(0.2));
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDurationTicks, slowLevel));
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
						DamageStatTracker.of(id + slot, this)), target);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CRYING_OBSIDIAN,
				"Gain " + GlossaryTag.SHIELDS.tag(this, shields) + " ["
				+ DescUtil.val((shieldDurationTicks / 20) + "s") + "] and create a " + GlossaryTag.RIFT.tag(this)
				+ ". Pull enemies within " + DescUtil.val((int) AREA.range) + " blocks, apply "
				+ DescUtil.potion("Slowness", slowLevel, slowDurationTicks / 20) + ", and deal "
				+ GlossaryTag.DARK.tag(this, damage) + " damage.");
	}
}
