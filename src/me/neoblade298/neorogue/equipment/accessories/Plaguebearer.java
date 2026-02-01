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

public class Plaguebearer extends Equipment {
	private static final String ID = "Plaguebearer";
	private int shields;
	
	public Plaguebearer(boolean isUpgraded) {
		super(ID, "Plaguebearer", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ACCESSORY);
		shields = isUpgraded ? 10 : 7;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
			Player p = data.getPlayer();
			data.addSimpleShield(p.getUniqueId(), shields, 160);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ROTTEN_FLESH, "Whenever you apply " + GlossaryTag.POISON.tag(this) + " to an enemy, gain "
				+ GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>8s</white>].");
	}
}