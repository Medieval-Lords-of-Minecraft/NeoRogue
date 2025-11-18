package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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

public class Fade extends Equipment {
	private static final String ID = "Fade";
	private int duration, cooldown;
	
	public Fade(boolean isUpgraded) {
		super(ID, "Fade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, isUpgraded ? 7 : 10, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		duration = 2;
		cooldown = (int) properties.get(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID,  Trigger.PRE_RECEIVE_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
		
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			if (!inst.canUse()) return TriggerResult.keep();
			inst.setNextUse(System.currentTimeMillis() + (1000 * cooldown));
			data.applyStatus(StatusType.STEALTH, data, 1, 60);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"Passive. Whenever you become " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by <yellow>" + duration + "</yellow>."
				+ " Basic attacks additionally grant you " + GlossaryTag.STEALTH.tag(this, 1, false) + " for <white>3</white> seconds.");
	}
}
