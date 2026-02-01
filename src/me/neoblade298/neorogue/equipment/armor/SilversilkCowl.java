package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreEvadeEvent;

public class SilversilkCowl extends Equipment {
	private static final String ID = "SilversilkCowl";
	private int evade = 2;
	private double mult;
	
	public SilversilkCowl(boolean isUpgraded) {
		super(ID, "Silversilk Cowl", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		mult = isUpgraded ? 1.0 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Grant evade at the start of the fight
		data.applyStatus(StatusType.EVADE, data, evade, -1);
		
		// Increase the damage mitigated per stamina
		data.addTrigger(id, Trigger.PRE_EVADE, (pdata, in) -> {
			PreEvadeEvent ev = (PreEvadeEvent) in;
			ev.getStaminaCostBuff().add(Buff.multiplier(data, mult, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		int pct = (int)(mult * 100);
		item = createItem(Material.CHAINMAIL_HELMET,
				"Start every fight with <yellow>" + evade + "</yellow> " + GlossaryTag.EVADE.tag(this) + ". "
				+ "The amount of damage that " + GlossaryTag.EVADE.tag(this) + " mitigates per stamina is increased by <yellow>" + pct + "%</yellow>.");
	}
}
