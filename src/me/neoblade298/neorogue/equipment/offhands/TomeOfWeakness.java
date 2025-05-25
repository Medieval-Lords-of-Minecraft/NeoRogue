package me.neoblade298.neorogue.equipment.offhands;

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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TomeOfWeakness extends Equipment {
	private static final String ID = "tomeOfWeakness";
	private static final TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANTED_HIT);
	private int inj;

	public TomeOfWeakness(boolean isUpgraded) {
		super(ID, "Tome of Weakness", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(15, 0, 12, tp.range));
		inj = isUpgraded ? 50 : 35;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.LEFT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			p.swingOffHand();
			Sounds.enchant.play(p, p);
			cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), null);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightData fd = FightInstance.getFightData(ent);
				fd.applyStatus(StatusType.INJURY, data, inj, -1);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On left click, apply " + GlossaryTag.INJURY.tag(this, inj, true)
				+ " to all enemies in a cone in front of you.");
	}
}
