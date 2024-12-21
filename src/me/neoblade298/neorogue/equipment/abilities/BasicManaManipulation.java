package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class BasicManaManipulation extends Equipment {
	private static final String ID = "basicManaManipulation";
	private int stacks;
	
	public BasicManaManipulation(boolean isUpgraded) {
		super(ID, "Basic Mana Manipulation", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
				stacks = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.isStatus(StatusType.STEALTH)) {
				ev.getDurationBuffList().add(Buff.increase(data, 1, StatTracker.IGNORED));
			}
			else if (ev.isStatus(StatusType.ELECTRIFIED) || ev.isStatus(StatusType.INSANITY)) {
				ev.getStacksBuffList().add(Buff.increase(data, stacks, StatTracker.IGNORED));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SOUL_TORCH,
				"Passive. Increase " + GlossaryTag.STEALTH.tag(this) + " application by <white>1s</white>. Increase " + GlossaryTag.ELECTRIFIED.tag(this) + " and " +
				GlossaryTag.INSANITY.tag(this) + " application stacks by " + DescUtil.yellow(stacks) + ".");
	}
}
