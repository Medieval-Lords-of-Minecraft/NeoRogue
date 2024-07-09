package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class SilentSteps2 extends Equipment {
	private static final String ID = "silentSteps2";
	private int duration, reduc;
	
	public SilentSteps2(boolean isUpgraded) {
		super(ID, "Silent Steps II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = 2;
		reduc = isUpgraded ? 9 : 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID,  Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev.getDurationBuff().addIncrease(data, duration);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"Passive. Whenever you become " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by <yellow>" + duration + "</yellow>." +
				" Damage received is reduced by <yellow>" + reduc + "</yellow> while " + GlossaryTag.STEALTH.tag(this) +".");
	}
}
