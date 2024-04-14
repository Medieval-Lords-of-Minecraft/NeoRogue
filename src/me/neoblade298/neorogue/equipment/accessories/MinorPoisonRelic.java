package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class MinorPoisonRelic extends Equipment {
	private static final String ID = "minorPoisonRelic";
	private int increase;
	
	public MinorPoisonRelic(boolean isUpgraded) {
		super(ID, "Minor Poison Relic", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		increase = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.POISON.name())) return TriggerResult.keep();
			ev.getStacksBuff().addIncrease(data, increase);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Whenever you apply " + GlossaryTag.POISON.tag(this) + ", increase the stacks by <yellow>"
				+ increase + "</yellow>.");
	}
}
