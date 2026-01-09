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
	private int stacks, shields;
	
	public TopazRing(boolean isUpgraded) {
		super(ID, "Topaz Ring", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		stacks = isUpgraded ? 25 : 15;
		shields = isUpgraded ? 5 : 3;
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
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS, "Increase stacks of " + GlossaryTag.ELECTRIFIED.tag(this) + " applied by <yellow>"
				+ stacks + "</yellow>. Applying " + GlossaryTag.ELECTRIFIED.tag(this) + " grants "
				+ GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}
