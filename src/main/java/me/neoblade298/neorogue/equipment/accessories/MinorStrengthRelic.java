package me.neoblade298.neorogue.equipment.accessories;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorStrengthRelic extends Equipment {
	private static final String ID = "MinorStrengthRelic";
	private int str;
	
	public MinorStrengthRelic(boolean isUpgraded) {
		super(ID, "Minor Strength Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		str = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.applyStatus(StatusType.STRENGTH, data, str, -1, this);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Increases " + GlossaryTag.STRENGTH.tag(this) + " by " + DescUtil.val(str) + ".");
	}
}
