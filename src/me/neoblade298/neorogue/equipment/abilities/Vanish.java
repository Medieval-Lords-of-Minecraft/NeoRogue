package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class Vanish extends Equipment {
	private static final String ID = "Vanish";
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
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			inst.addCount(1);
			ev.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
			
			if (inst.getCount() >= threshold) {
				data.applyStatus(StatusType.EVADE, data, 1, 100);
				data.addStamina(10);
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PRE_RECEIVE_STATUS, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"Passive. Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by <yellow>" + duration + "</yellow>." +
				" Every <yellow>" + threshold + "</yellow> times you become " + GlossaryTag.STEALTH.tag(this) + ", gain " + GlossaryTag.EVADE.tag(this, 1, false) +
				" [<white>5s</white>] and <white>10</white> stamina.");
	}
}
