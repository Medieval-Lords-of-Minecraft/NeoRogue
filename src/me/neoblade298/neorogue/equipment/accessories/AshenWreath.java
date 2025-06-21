package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class AshenWreath extends Equipment {
	private static final String ID = "ashenWreath";
	private int inc, thres;
	private static final int MAX = 5;
	public AshenWreath(boolean isUpgraded) {
		super(ID, "Ashen Wreath", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		inc = 1;
		thres = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.BURN) || ev.getTarget() instanceof PlayerFightData) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() % thres == 0 && am.getCount() <= MAX * thres) {
				data.applyStatus(StatusType.SHELL, data, inc, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GUNPOWDER, "Every " + DescUtil.white(thres) + " times you apply " + GlossaryTag.BURN.tag(this) + " to an enemy, gain a stack of " +
		GlossaryTag.SHELL.tag(this, inc, false) + ", up to " + DescUtil.white(MAX) + " stacks.");
	}
}
