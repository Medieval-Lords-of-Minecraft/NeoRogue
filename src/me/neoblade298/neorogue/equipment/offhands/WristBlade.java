package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WristBlade extends Equipment {
	private static final String ID = "wristBlade";
	private int hits;
	
	public WristBlade(boolean isUpgraded) {
		super(ID, "Wrist Blade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.OFFHAND);
		hits = isUpgraded ? 2 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, new WristBladeInstance());
	}
	
	private class WristBladeInstance implements TriggerAction {
		private int count = 0;
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			if (++count >= hits) {
				count = -1; // -1 so that the double trigger sets it to 0
				data.runActions(data, Trigger.PRE_BASIC_ATTACK, inputs);
			}
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		String hitsString = isUpgraded ? "2nd" : "3rd";
		item = createItem(Material.PRISMARINE_SHARD, "Every <yellow>" + hitsString + "</yellow> basic attack will trigger"
				+ " on-hit effects twice.");
	}
}
