package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;

// An equipment instance that requires arrows to cast
public class AmmoEquipmentInstance extends EquipmentInstance {
	public AmmoEquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es) {
		super(p, eq, slot, es);
	}
	public AmmoEquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es, TriggerAction act) {
		super(p, eq, slot, es, act);
	}

	@Override
	public boolean canTrigger(Player p, PlayerFightData data) {
		if (!super.canTrigger(p, data)) return false;
		if (data.getAmmunition() == null) return false;
		return true;
	}
}
