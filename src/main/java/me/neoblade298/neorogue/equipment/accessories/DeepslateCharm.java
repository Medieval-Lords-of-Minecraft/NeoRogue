package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class DeepslateCharm extends Equipment {
	private static final String ID = "DeepslateCharm";
	private static final int DURATION = 5;
	private int shields;

	public DeepslateCharm(boolean isUpgraded) {
		super(ID, "Deepslate Charm", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			Player player = data.getPlayer();
			data.addSimpleShield(player.getUniqueId(), shields, DURATION * 20, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEEPSLATE, "Applying " + GlossaryTag.CONCUSSED.tag(this) + " grants "
				+ GlossaryTag.SHIELDS.tag(this, shields, true) + " [" + DescUtil.white(DURATION + "s") + "].");
	}
}
