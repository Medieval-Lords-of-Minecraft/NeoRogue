package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Untouchable extends Equipment {
	private static final String ID = "Untouchable";
	private int threshold;
	
	public Untouchable(boolean isUpgraded) {
		super(ID, "Untouchable", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		threshold = isUpgraded ? 3 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			
			// Only trigger when player receives stealth
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			
			am.addCount(ev.getStacks());
			
			if (am.getCount() >= threshold) {
				FightInstance.applyStatus(p, StatusType.EVADE, data, am.getCount() / threshold, -1);
				am.setCount(am.getCount() % threshold);
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				"Passive. Every <yellow>" + threshold + "</yellow> stacks of " + GlossaryTag.STEALTH.tag(this) + " you receive, gain <white>1</white> " + GlossaryTag.EVADE.tag(this) + ".");
	}
}
