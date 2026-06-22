package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class PowerThrough extends Equipment implements Power {
	private static final String ID = "PowerThrough";
	private int cutoff;
	
	public PowerThrough(boolean isUpgraded) {
		super(ID, "Power Through", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 25, 0, 0));
		
		cutoff = isUpgraded ? 3 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		}));
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata2, in2) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in2;
			if (!ev.getStatusId().equals(StatusType.BERSERK.name())) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			
			int num = inst.getCount() / cutoff;
			data.applyStatus(StatusType.PROTECT, data, num, -1);
			inst.setCount(inst.getCount() % cutoff);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				GlossaryTag.POWER.tag(this) + ". For every " + GlossaryTag.BERSERK.tag(this, cutoff, true) + " you acquire, apply " + GlossaryTag.PROTECT.tag(this, 1, false) + " to yourself.");
	}
}
