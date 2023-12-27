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
	private int hits;
	
	public WristBlade(boolean isUpgraded) {
		super("wristBlade", "Wrist Blade", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.OFFHAND);
		hits = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, new WristBladeInstance());
	}
	
	private class WristBladeInstance implements TriggerAction {
		private int count = 0;
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			if (++count >= hits) {
				data.runActions(data, Trigger.BASIC_ATTACK, inputs);
				count = 0;
			}
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		String hitsString = isUpgraded ? "3rd" : "2nd";
		item = createItem(Material.PRISMARINE_SHARD, "Every <yellow>" + hitsString + " basic attack will trigger"
				+ " on-hit effects twice.");
	}
}
