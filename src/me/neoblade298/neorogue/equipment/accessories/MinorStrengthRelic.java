package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorStrengthRelic extends Equipment {
	private static final String ID = "minorStrengthRelic";
	private double str;
	
	public MinorStrengthRelic(boolean isUpgraded) {
		super(ID, "Minor Strength Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		str = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, true, false, DamageCategory.PHYSICAL, str);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Increases " + GlossaryTag.STRENGTH.tag(this) + " by <yellow>" + str + "</yellow>.");
	}
}
