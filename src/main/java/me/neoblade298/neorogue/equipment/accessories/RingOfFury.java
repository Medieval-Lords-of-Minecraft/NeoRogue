package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class RingOfFury extends Equipment {
	private static final String ID = "RingOfFury";
	private static final int THRES = 1200;
	private int berserk;

	public RingOfFury(boolean isUpgraded) {
		super(ID, "Ring of Fury", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		berserk = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			double dmg = 0;
			for (DamageSlice slice : ev.getMeta().getSlices()) {
				dmg += slice.getDamage();
			}
			if (dmg > 0) {
				am.addDouble(dmg);
				if (am.getDouble() >= THRES) {
					am.addDouble(-THRES);
					data.applyStatus(StatusType.BERSERK, data, berserk, -1, this);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT, "Upon dealing at least " + DescUtil.white(THRES)
				+ " damage, gain " + GlossaryTag.BERSERK.tag(this, berserk, true) + ".");
	}
}
