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

public class Enlighten extends Equipment {
	private static final String ID = "enlighten";
	private int reflect;
	
	public Enlighten(boolean isUpgraded) {
		super(ID, "Enlighten", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		reflect = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.SANCTIFIED.name())) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			
			int num = inst.getCount() / 5;
			data.applyStatus(StatusType.REFLECT, data, reflect * num, -1);
			inst.setCount(inst.getCount() % 5);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				"Passive. For every " + GlossaryTag.SANCTIFIED.tag(this, 5, false) + " you apply, apply " + GlossaryTag.REFLECT.tag(this, reflect, true) + " to yourself.");
	}
}
