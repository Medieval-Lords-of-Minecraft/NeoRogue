package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
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

public class StoneUppercut extends Equipment {
	private static final String ID = "StoneUppercut";
	private static final int RADIUS = 3;
	private static final double LAUNCH_SPEED = 0.7, KNOCKBACK_SPEED = 0.6;
	private static final TargetProperties AOE = TargetProperties.radius(RADIUS, true, TargetType.ENEMY);
	private static final TargetProperties FRONT = TargetProperties.cone(90, RADIUS, true, TargetType.ENEMY);
	private static final Circle KNOCKUP_AREA = new Circle(RADIUS), LAUNCH_RING = new Circle(1.2);
	private static final ParticleContainer LAUNCH_EDGE = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.STONE.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer KNOCKUP_EDGE = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer KNOCKUP_FILL = new ParticleContainer(Particle.DUST_PLUME)
			.count(1).spread(0.1, 0).speed(0);
	private static final ParticleContainer IMPACT = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.DEEPSLATE.createBlockData()).count(12).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private static final ParticleContainer IMPACT_DUST = new ParticleContainer(Particle.DUST_PLUME)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(0.7);
	private static final SoundContainer LAUNCH_SOUND = new SoundContainer(Sound.BLOCK_STONE_BREAK, 0.9F, 1.3F);
	private static final SoundContainer IMPACT_SOUND = new SoundContainer(Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.8F, 1.2F);
	private int damage, concussed;

	public StoneUppercut(boolean isUpgraded) {
		super(ID, "Stone Uppercut", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 5, 6, RADIUS));
		damage = isUpgraded ? 105 : 70;
		concussed = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			LAUNCH_SOUND.play(p, p);
			LAUNCH_RING.play(LAUNCH_EDGE, p.getLocation(), LocalAxes.xz(), null);
			KNOCKUP_AREA.play(KNOCKUP_EDGE, p.getLocation(), LocalAxes.xz(), KNOCKUP_FILL);
			p.setVelocity(p.getVelocity().setY(LAUNCH_SPEED));
			LivingEntity target = TargetHelper.getNearest(p, FRONT);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, AOE)) {
				Vector away = ent.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0);
				if (away.lengthSquared() > 0) away.normalize().multiply(KNOCKBACK_SPEED);
				FightInstance.knockback(ent, away.setY(LAUNCH_SPEED));
			}
			if (target != null) {
				IMPACT.play(p, target.getLocation());
				IMPACT_DUST.play(p, target.getLocation());
				IMPACT_SOUND.play(p, target.getLocation());
				Sounds.crit.play(p, target.getLocation());
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
						DamageStatTracker.of(id + slot, this)), target);
				FightInstance.applyStatus(target, StatusType.CONCUSSED, data, concussed, -1, this);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_BUTTON, "On cast, launch yourself and knock up nearby enemies. The nearest enemy in front of you takes "
				+ GlossaryTag.EARTHEN.tag(this, damage) + " damage and " + GlossaryTag.CONCUSSED.tag(this, concussed) + ".");
	}
}