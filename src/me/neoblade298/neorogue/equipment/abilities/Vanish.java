package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Vanish extends Equipment {
	private static final String ID = "vanish";
	private int duration, threshold;
	
	public Vanish(boolean isUpgraded) {
		super(ID, "Vanish", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = 2;
		threshold = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			inst.addCount(1);
			ev.getDurationBuff().addIncrease(data, duration);
			
			if (inst.getCount() >= threshold) {
				data.applyStatus(StatusType.EVADE, data, 1, -1);
				data.addStamina(5);
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.RECEIVE_STATUS, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"Passive. Whenever you become " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by <yellow>" + duration + "</yellow>." +
				" Every <yellow>" + threshold + "</yellow> times you become " + GlossaryTag.STEALTH.tag(this) + ", gain " + GlossaryTag.EVADE.tag(this, 1, false) +
				" and <white>5</white> stamina.");
	}
}
