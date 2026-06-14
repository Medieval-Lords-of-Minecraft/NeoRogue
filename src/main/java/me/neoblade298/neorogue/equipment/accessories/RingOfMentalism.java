package me.neoblade298.neorogue.equipment.accessories;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class RingOfMentalism extends Equipment {
	private static final String ID = "RingOfMentalism";
	private int stacks;
	
	public RingOfMentalism(boolean isUpgraded) {
		super(ID, "Ring Of Mentalism", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		stacks = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.MAGICAL)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() >= 3) {
				am.setCount(0);
				FightInstance.applyStatus(ev.getTarget(), StatusType.INSANITY, data, stacks, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD, "Every " + DescUtil.white("3") + " times you deal " + GlossaryTag.MAGICAL.tag(this) + " damage, apply "
				+ GlossaryTag.INSANITY.tag(this, stacks, true) + " to the target.");
	}
}
