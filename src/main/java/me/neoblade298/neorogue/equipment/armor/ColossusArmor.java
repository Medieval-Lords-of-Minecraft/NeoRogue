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

public class ColossusArmor extends Equipment {
	private static final String ID = "ColossusArmor";
	private int threshold;

	public ColossusArmor(boolean isUpgraded) {
		super(ID, "Colossus Armor", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		threshold = isUpgraded ? 20 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			int reduction = (int) (data.getMaxStamina() / threshold);
			if (reduction <= 0) return TriggerResult.keep();
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL),
					Buff.increase(data, reduction, StatTracker.defenseBuffAlly(buffId, this, false)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIAMOND_CHESTPLATE,
				"Reduce " + GlossaryTag.PHYSICAL.tag(this) + " damage taken by " + DescUtil.white(1) +
				" for every " + DescUtil.yellow(threshold) + " max stamina you have.");
	}
}
