package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SmallShield extends Equipment {
	private int reduction;
	
	public SmallShield(boolean isUpgraded) {
		super("smallShield", "Small Shield", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 5 : 3;
		addReforgeOption("smallShield", "hastyShield", "spikyShield");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
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
		item = createItem(Material.SHIELD, "When raised, reduce all damage taken by <yellow>" + reduction + "</yellow>.");
	}
}
