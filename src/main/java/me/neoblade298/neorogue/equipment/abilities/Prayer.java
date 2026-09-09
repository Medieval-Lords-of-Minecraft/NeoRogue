package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Prayer extends Equipment {
	private static final String ID = "Prayer";
	private static final int SANCTIFIED_THRESHOLD = 10;
	private int shields;
	
	public Prayer(boolean isUpgraded) {
		super(ID, "Prayer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta progress = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
			if (progress.addCount(ev.getStacks()) < SANCTIFIED_THRESHOLD) return TriggerResult.keep();
			progress.addCount(-SANCTIFIED_THRESHOLD);
			data.addSimpleShield(data.getPlayer().getUniqueId(), shields, 60, this); // 3s
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				GlossaryTag.PASSIVE.tag(this) + ". Every " + GlossaryTag.SANCTIFIED.tag(this, SANCTIFIED_THRESHOLD)
				+ " you apply grants " + GlossaryTag.SHIELDS.tag(this, shields) + " [" + DescUtil.val("3s") + "].");
	}
}
