package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Exertion extends Equipment {
	private static final String ID = "exertion";
	private static final TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).offsetY(0.5);
	private int damage, selfDmg = 3;

	public Exertion(boolean isUpgraded) {
		super(ID, "Exertion", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 15, 12, tp.range));
		damage = isUpgraded ? 250 : 150;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(ToAshes.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.fire.play(p, p);
			cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), null);
			LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInCone(p, tp);
			for (LivingEntity ent : trgs) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
			}
			FightInstance.dealDamage(new DamageMeta(data, selfDmg, DamageType.FIRE), p);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, deal " + GlossaryTag.FIRE.tag(this, damage, true)
						+ " to all enemies in a cone in front of you, but " + "deal "
						+ GlossaryTag.FIRE.tag(this, selfDmg, false) + " to yourself.");
	}
}
