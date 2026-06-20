package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class SigilOfDestruction extends Equipment {
	private static final String ID = "SigilOfDestruction";
	private static final int THRES = 500;
	private int shields;

	public SigilOfDestruction(boolean isUpgraded) {
		super(ID, "Sigil of Destruction", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.PHYSICAL)) return TriggerResult.keep();
			double dmg = 0;
			for (DamageSlice slice : ev.getMeta().getSlices()) {
				if (DamageCategory.PHYSICAL.hasType(slice.getType())) {
					dmg += slice.getDamage();
				}
			}
			if (dmg > 0) {
				am.addDouble(dmg);
				while (am.getDouble() >= THRES) {
					am.addDouble(-THRES);
					Player p = data.getPlayer();
					data.applyStatus(StatusType.BERSERK, data, 1, -1);
					data.addSimpleShield(p.getUniqueId(), shields, 100);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT, "For every " + DescUtil.white(THRES) + " "
				+ GlossaryTag.PHYSICAL.tag(this) + " damage dealt, gain "
				+ GlossaryTag.BERSERK.tag(this, 1, false) + " and "
				+ GlossaryTag.SHIELDS.tag(this, shields, true) + " [" + DescUtil.white("5s") + "].");
	}
}
