package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Snakebite extends Equipment {
	private static final String ID = "Snakebite";
	private static final TargetProperties TARGETS = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	private static final Cone CONE = new Cone(TARGETS.range, TARGETS.arc);
	private static final ParticleContainer CONE_PARTICLES = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(90, 180, 55), 0.9F)).count(1).spread(0.03, 0.03).speed(0);
	private static final ParticleContainer HIT_PARTICLES = new ParticleContainer(Particle.WITCH)
			.count(5).spread(0.2, 0.35).speed(0.01);
	private int poison, poisonDuration, poisonMultiplier;

	public Snakebite(boolean isUpgraded) {
		super(ID, "Snakebite", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 12, TARGETS.range).add(PropertyType.CHARGE_TIME, 1));
		poison = 40;
		poisonDuration = isUpgraded ? 160 : 120;
		poisonMultiplier = 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			data.charge(properties.get(PropertyType.CHARGE_TIME)).then(() -> {
				Player p = data.getPlayer();
				Sounds.extinguish.play(p, p);
				CONE.play(CONE_PARTICLES, p.getLocation(), LocalAxes.usingEyeLocation(p), null);
				for (LivingEntity target : TargetHelper.getEntitiesInCone(p, TARGETS)) {
					FightData fd = FightInstance.getFightData(target);
					fd.applyStatus(StatusType.POISON, data, poison, poisonDuration, this);
					HIT_PARTICLES.play(p, target);
					double damage = fd.getStatus(StatusType.POISON).getStacks() * poisonMultiplier;
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.POISON,
							DamageStatTracker.of(id + slot, this)), target);
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FERMENTED_SPIDER_EYE,
				"On cast, " + DescUtil.charge(this, 1, properties.get(PropertyType.CHARGE_TIME))
						+ " before applying " + GlossaryTag.POISON.tag(this, poison) + " ["
						+ DescUtil.duration(poisonDuration / 20, true) + "] to enemies in a cone in front of you. Then, deal "
						+ GlossaryTag.POISON.tag(this) + " damage based on their " + GlossaryTag.POISON.tag(this)
						+ " stacks multiplied by " + DescUtil.val(poisonMultiplier) + ".");
	}
}