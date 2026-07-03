package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class GuardianPlate extends Equipment {
	private static final String ID = "GuardianPlate";
	private int damageReduction;

	public GuardianPlate(boolean isUpgraded) {
		super(ID, "Guardian Plate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 4 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			if (data.getShields().isEmpty()) return TriggerResult.keep();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damageReduction, StatTracker.defenseBuffAlly(buffId, this, false)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_CHESTPLATE, "Reduce all damage taken by " + DescUtil.yellow(damageReduction) +
				" while you have " + GlossaryTag.SHIELDS.tag(this) + ".");
	}
}
