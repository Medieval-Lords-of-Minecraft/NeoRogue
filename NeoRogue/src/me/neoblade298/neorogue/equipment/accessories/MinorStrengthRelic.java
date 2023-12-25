package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorStrengthRelic extends Equipment {
	private double str;
	
	public MinorStrengthRelic(boolean isUpgraded) {
		super("minorStrengthRelic", "Minor Strength Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		str = isUpgraded ? 6 : 4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addBuff(p.getUniqueId(), true, false, BuffType.PHYSICAL, str);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Increases physical damage by <yellow>" + str + "</yellow>.");
	}
}
