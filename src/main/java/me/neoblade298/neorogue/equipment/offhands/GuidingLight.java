package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
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
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class GuidingLight extends Equipment {
	private static final String ID = "GuidingLight";
	private static final int ATTACKS = 7;
	private static final TargetProperties tp = TargetProperties.cone(45, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer coneEdge =
			new ParticleContainer(Particle.FIREWORK).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer coneFill =
			new ParticleContainer(Particle.DUST).dustOptions(new DustOptions(Color.fromRGB(255, 226, 128), 0.8F))
					.count(1).spread(0.05, 0).speed(0);
	private static final SoundContainer procSound =
			new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.45F, 1.55F);
	private int damage, sanctified;

	public GuidingLight(boolean isUpgraded) {
		super(ID, "Guiding Light", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(10, 0, 16, 0));
		damage = isUpgraded ? 90 : 60;
		sanctified = isUpgraded ? 12 : 8;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player player = data.getPlayer();
			am.setCount(ATTACKS);
			Sounds.enchant.play(player, player);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (am.getCount() <= 0) return TriggerResult.keep();
			PreBasicAttackEvent event = (PreBasicAttackEvent) in;
			event.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.LIGHT,
					DamageStatTracker.of(id + slot, this)));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (am.getCount() <= 0) return TriggerResult.keep();
			BasicAttackEvent event = (BasicAttackEvent) in;
			Player player = data.getPlayer();
			boolean firstAttack = am.getCount() == ATTACKS;
			cone.play(coneEdge, player.getLocation(), LocalAxes.usingEyeLocation(player), coneFill);
			procSound.play(player, player);
			if (firstAttack) {
				FightInstance.applyStatus(event.getTarget(), StatusType.SANCTIFIED, data, sanctified, -1, this);
			}
			for (LivingEntity target : TargetHelper.getEntitiesInCone(player, tp)) {
				if (target.equals(event.getTarget())) continue;
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT,
						DamageStatTracker.of(id + slot, this)), target);
				if (firstAttack) {
					FightInstance.applyStatus(target, StatusType.SANCTIFIED, data, sanctified, -1, this);
				}
			}
			am.addCount(-1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE_DUST,
				"On right click, your next " + DescUtil.val(ATTACKS) + " basic attacks deal an additional "
				+ GlossaryTag.LIGHT.tag(this, damage) + " damage in a narrow cone. The first also applies "
				+ GlossaryTag.SANCTIFIED.tag(this, sanctified) + ".");
	}
}