package me.neoblade298.neorogue.equipment.armor;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class LeatherArmguard extends Equipment {
	private static final String ID = "LeatherArmguard";
	private double def, spdef;
	
	public LeatherArmguard(boolean isUpgraded) {
		super(ID, "Leather Armguard", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		def = 2;
		spdef = isUpgraded ? 3 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.increase(data, data.hasStatus(StatusType.STEALTH) ? spdef + def : def, StatTracker.defenseBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_DYE, "Reduce " + GlossaryTag.GENERAL.tag(this) + " damage received by " + DescUtil.white(def) + ", increased to "
				+ DescUtil.yellow(spdef) + " when in " + GlossaryTag.STEALTH.tag(this) + ".");
	}
}
