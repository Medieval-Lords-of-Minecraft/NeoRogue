package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class CeruleanBracelet extends Equipment {
	private static final String ID = "ceruleanBracelet";
	private int mp;
	public CeruleanBracelet(boolean isUpgraded) {
		super(ID, "Cerulean Bracelet", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		mp = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addMaxMana(mp);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI, "At the start of the fight, increase your max mana by " + DescUtil.yellow(mp) + ".");
	}
}
