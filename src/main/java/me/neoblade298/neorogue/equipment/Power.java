package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.ActivatePowerEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public interface Power {
	void onPowerActivated(PlayerFightData data, int slot, EquipSlot es);

	default boolean activatePower(PlayerFightData data, int slot, EquipSlot es) {
		Equipment equip = (Equipment) this;
		ActivatePowerEvent event = new ActivatePowerEvent(equip, slot, es);
		if (data.runActions(data, Trigger.PRE_ACTIVATE_POWER, event)) return false;
		if (data.hasStatus(StatusType.DAMPENED)) return false;
		Player p = data.getPlayer();
		Sounds.fire.play(p, p);
		Util.msgRaw(p, Component.text("").append(equip.getHoverable()).append(Component.text(" was activated", NamedTextColor.GRAY)));
		onPowerActivated(data, slot, es);
		data.runActions(data, Trigger.ACTIVATE_POWER, event);
		return true;
	}
}
