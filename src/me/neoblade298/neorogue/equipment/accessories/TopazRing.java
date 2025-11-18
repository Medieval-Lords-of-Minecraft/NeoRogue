package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class TopazRing extends Equipment {
	private static final String ID = "TopazRing";
	private int stacks;
	
	public TopazRing(boolean isUpgraded) {
		super(ID, "Topaz Ring", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		stacks = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			ev.getStacksBuffList().add(new Buff(data, stacks, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS, "Increase stacks of " + GlossaryTag.ELECTRIFIED.tag(this) + " applied by <yellow>"
				+ stacks + "</yellow>.");
	}
}
