package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class SilentSteps extends Equipment {
	private static final String ID = "silentSteps";
	private int duration, reduc;
	
	public SilentSteps(boolean isUpgraded) {
		super(ID, "Silent Steps", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = isUpgraded ? 2 : 1;
		reduc = isUpgraded ? 3 : 2;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Fade.get(), Vanish.get(), SilentSteps2.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID,  Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev.getDurationBuff().addIncrease(data, 20);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			if (!pdata.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(pdata, 3, 0), BuffOrigin.NORMAL, false);
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
