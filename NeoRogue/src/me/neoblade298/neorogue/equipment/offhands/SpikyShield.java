package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SpikyShield extends Equipment {
	private int reduction, amount;
	
	public SpikyShield(boolean isUpgraded) {
		super("spikyShield", "Spiky Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = 8;
		amount = isUpgraded ? 6 : 4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.applyStatus(StatusType.THORNS, p.getUniqueId(), amount, -1);
		data.addTrigger(id, Trigger.RAISE_SHIELD, (pdata, inputs) -> {
			data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, reduction);
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, inputs) -> {
			data.addBuff(p.getUniqueId(), false, false, BuffType.PHYSICAL, -reduction);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduce all damage taken by <yellow>" + reduction + "</yellow>."
				+ " Also grants <yellow>" + amount + "</yellow> thorns at the start of combat.");
	}
}
