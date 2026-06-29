package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class Fade extends Equipment implements Power {
	private static final String ID = "Fade";
	private int duration, stealthDur;
	
	public Fade(boolean isUpgraded) {
		super(ID, "Fade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = 2;
		stealthDur = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 2) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, (pdata2, in2) -> {
			PreApplyStatusEvent ev2 = (PreApplyStatusEvent) in2;
			if (!ev2.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev2.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});

		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata3, in3) -> {
			data.applyStatus(StatusType.STEALTH, data, 1, stealthDur * 20);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, inst);
	}


	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				GlossaryTag.POWER.tag(this) + ". Activates after receiving " + GlossaryTag.STEALTH.tag(this) + " " + DescUtil.white(2) + " times. Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.white(duration + "s") + "."
				+ " Basic attacks additionally grant you " + GlossaryTag.STEALTH.tag(this, 1, false) + " " + DescUtil.duration(stealthDur, true) + ".");
	}
}
