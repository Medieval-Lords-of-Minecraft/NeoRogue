package me.neoblade298.neorogue.equipment.armor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class StarlightHood extends Equipment {
	private static final String ID = "StarlightHood";
	private int stacks;

	public StarlightHood(boolean isUpgraded) {
		super(ID, "Starlight Hood", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
		stacks = isUpgraded ? 4 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.PROTECT, data, stacks, 400, this);
		data.applyStatus(StatusType.SHELL, data, stacks, 400, this);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET,
				"Start fights with " + GlossaryTag.PROTECT.tag(this, stacks, true) + " and "
						+ GlossaryTag.SHELL.tag(this, stacks, true) + " [" + DescUtil.white("20s") + "].");

		LeatherArmorMeta dye = (LeatherArmorMeta) item.getItemMeta();
		dye.setColor(Color.BLUE);
		item.setItemMeta(dye);
	}
}
