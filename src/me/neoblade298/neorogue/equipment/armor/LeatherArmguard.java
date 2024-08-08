package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LeatherArmguard extends Equipment {
	private static final String ID = "leatherArmguard";
	private double def, spdef;
	
	public LeatherArmguard(boolean isUpgraded) {
		super(ID, "Leather Armguard", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		def = isUpgraded ? 2 : 1;
		spdef = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			data.addBuff(data, false, false, BuffType.GENERAL, data.hasStatus(StatusType.STEALTH) ? spdef + def : def);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_DYE, "Decrease " + GlossaryTag.GENERAL.tag(this) + " damage received by <yellow>" + def + "</yellow>, increased to"
				+ " <yellow>" + spdef + " </yellow> when in " + GlossaryTag.STEALTH.tag(this) + ".");
	}
}
