package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EnderEye extends Equipment {
	private static final String ID = "EnderEye";
	private static final TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ENDERMAN_AMBIENT);
	private int inc;

	public EnderEye(boolean isUpgraded) {
		super(ID, "Ender Eye", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(20, 0, 24, tp.range));
		inc = isUpgraded ? 105 : 70;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			sc.play(p, p);
			cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), null);
			LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInCone(p, tp);
			for (LivingEntity ent : trgs) {
				FightInstance.getFightData(ent).addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL),
						Buff.increase(data, -inc, BuffStatTracker.defenseDebuffEnemy(buffId, this)), 8 * 20);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENDER_EYE,
				"On right click, decrease " + GlossaryTag.MAGICAL.tag(this)
						+ " defense of all enemies in a cone in front of you by " + DescUtil.yellow(inc)
						+ " [<white>8s</white>].");
	}
}
