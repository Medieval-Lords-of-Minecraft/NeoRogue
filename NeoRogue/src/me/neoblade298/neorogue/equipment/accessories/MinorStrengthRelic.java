package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Accessory;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorStrengthRelic extends Accessory {
	private double str;
	
	public MinorStrengthRelic(boolean isUpgraded) {
		super("minorStrengthRelic", "Minor Strength Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		str = isUpgraded ? 1.5 : 1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addBuff(p.getUniqueId(), true, false, BuffType.PHYSICAL, str);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "ACCESSORY", null, "Increases physical damage by <yellow>" + str + "</yellow>.");
	}
}
