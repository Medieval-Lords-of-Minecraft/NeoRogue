package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class BlindingCloak extends Equipment {
	private static final String ID = "blindingCloak";
	private double shields;
	
	public BlindingCloak(boolean isUpgraded) {
		super(ID, "Blinding Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 10 : 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			
			data.addSimpleShield(p.getUniqueId(), shields, 60);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS, "Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", "
				+ "grant " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>3s</white>].");
	}
}
