package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class BasicIceMastery extends Equipment {
	private static final String ID = "basicIceMastery";
	private int shields;
	
	public BasicIceMastery(boolean isUpgraded) {
		super(ID, "Basic Ice Mastery", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				shields = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), 5, false);
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			ev.getStacksBuff().addIncrease(data, 1);
			data.addSimpleShield(p.getUniqueId(), shields, 60);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_ICE,
				"Passive. Start fights with " + GlossaryTag.SHIELDS.tag(this, 5, false) + ". Increase application of " + GlossaryTag.FROST.tag(this) +
				" by <white>1</white>, and gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>3s</white>] upon applying it.");
	}
}
