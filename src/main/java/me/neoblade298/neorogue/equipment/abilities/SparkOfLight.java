package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.offhands.GuidingLight;
import me.neoblade298.neorogue.equipment.offhands.PureEmber;
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

public class SparkOfLight extends Equipment {
	private static final String ID = "SparkOfLight";
	private static final TargetProperties tp = TargetProperties.cone(45, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer coneEdge =
			new ParticleContainer(Particle.FIREWORK).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer coneFill =
			new ParticleContainer(Particle.END_ROD).count(1).spread(0.05, 0).speed(0);
	private static final ParticleContainer castSpark =
			new ParticleContainer(Particle.FIREWORK).count(4).spread(0.06, 0.06).speed(0.005)
					.offsetY(1.2).offsetForward(0.7);
	private static final SoundContainer castSound =
			new SoundContainer(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.55F, 1.45F);
	private int attacks, damage;

	public SparkOfLight(boolean isUpgraded) {
		super(ID, "Spark of Light", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 0, 0, tp.range));
		attacks = isUpgraded ? 3 : 5;
		damage = isUpgraded ? 50 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(PureEmber.get(), GuidingLight.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		ItemStack progressIcon = item.clone();
		ItemStack chargedIcon = item.clone().withType(Material.NETHER_STAR);
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);

		inst.setAction((pdata, in) -> {
			Player player = data.getPlayer();
			am.setCount(0);
			progressIcon.setAmount(1);
			inst.setIcon(progressIcon);

			cone.play(coneEdge, player.getLocation().add(0, 1, 0), LocalAxes.usingEyeLocation(player), coneFill);
			castSpark.play(player, player);
			castSound.play(player, player);
			for (LivingEntity target : TargetHelper.getEntitiesInCone(player, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT,
						DamageStatTracker.of(id + slot, this)), target);
			}
			return TriggerResult.keep();
		});
		inst.setCondition((pdata, player, in) -> am.getCount() >= attacks);
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (am.getCount() >= attacks) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() >= attacks) {
				inst.setIcon(chargedIcon);
			}
			else {
				progressIcon.setAmount(am.getCount());
				inst.setIcon(progressIcon);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"Can be cast every " + DescUtil.val(attacks) + " basic attacks. On cast, deal "
				+ GlossaryTag.LIGHT.tag(this, damage) + " damage in a narrow cone in front of you.");
	}
}