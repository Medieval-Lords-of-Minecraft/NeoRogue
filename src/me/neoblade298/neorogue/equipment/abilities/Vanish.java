package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 15, 0, 0));
		duration = 2;
		threshold = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			StandardPriorityAction inst = new StandardPriorityAction(ID);
			inst.setAction((pdata2, in2) -> {
				PreApplyStatusEvent ev = (PreApplyStatusEvent) in2;
				if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
				inst.addCount(1);
				ev.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
				if (inst.getCount() >= threshold) {
					data.applyStatus(StatusType.EVADE, data, 1, 100);
					data.addStamina(10);
				}
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PRE_RECEIVE_STATUS, inst);

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				GlossaryTag.POWER.tag(this) + ". Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by " + DescUtil.yellow(duration) + "." +
				" Every " + DescUtil.yellow(threshold) + " times you receive " + GlossaryTag.STEALTH.tag(this) + ", gain " + GlossaryTag.EVADE.tag(this, 1, false) +
				" [<white>5s</white>] and " + DescUtil.white(10) + " stamina.");
	}
}
