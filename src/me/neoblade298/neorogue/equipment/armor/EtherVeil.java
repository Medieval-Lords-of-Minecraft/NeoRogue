package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EtherVeil extends Equipment {
	private static final String ID = "EtherVeil";
	private double def;
	private int defStr;

	public EtherVeil(boolean isUpgraded) {
		super(ID, "Ether Veil", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
		def = isUpgraded ? 0.2 : 0.1;
		defStr = (int) (def * 100);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(data.getPlayer().getUniqueId(), data.getMaxMana() * def);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BELL,
				"Start fights with " + DescUtil.yellow(defStr + "%") + " of your max mana in " + GlossaryTag.SHIELDS.tag(this) + ". ");
	}
}
