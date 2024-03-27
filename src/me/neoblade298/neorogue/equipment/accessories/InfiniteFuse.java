package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class InfiniteFuse extends Equipment {
	private static final String ID = "infiniteFuse";
	private int tick = 0;
	
	public InfiniteFuse(boolean isUpgraded) {
		super(ID, "Infinite Fuse", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> TriggerResult.keep());
		
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (tick++ % 20 == 0)
				pdata.applyStatus(StatusType.BURN, data, 1, -1); // hope u made -1 mean it doesn't go away! i am not checking
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.RABBIT_FOOT,
				"Burn on you does not tick down." + (isUpgraded ? " Gain one burn per second." : "")
		);
	}
}
