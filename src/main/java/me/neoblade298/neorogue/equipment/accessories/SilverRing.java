package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SilverRing extends Equipment {
	private static final String ID = "SilverRing";
	private int reflect, shell;

	public SilverRing(boolean isUpgraded) {
		super(ID, "Silver Ring", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR, EquipmentType.ACCESSORY);
		reflect = 20;
		shell = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.REFLECT, data, reflect, -1, this);
		data.applyStatus(StatusType.SHELL, data, shell, -1, this);
		data.addTrigger(id, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			data.applyStatus(StatusType.SHELL, data, -shell, -1, this);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Start fights with " + GlossaryTag.REFLECT.tag(this, reflect) + " and "
				+ GlossaryTag.SHELL.tag(this, shell) + ". Lose the granted " + GlossaryTag.SHELL.tag(this)
				+ " when you take health damage.");
	}
}