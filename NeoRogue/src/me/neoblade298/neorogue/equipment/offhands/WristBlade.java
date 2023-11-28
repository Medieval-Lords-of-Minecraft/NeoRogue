package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Offhand;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class WristBlade extends Offhand {
	private int hits;
	
	public WristBlade(boolean isUpgraded) {
		super("wristBlade", isUpgraded, Rarity.RARE, EquipmentClass.THIEF);
		hits = isUpgraded ? 3 : 2;
		String hitsString = isUpgraded ? "3rd" : "2nd";
		item = createItem(this, Material.PRISMARINE_SHARD, null, "Every <yellow>" + hitsString + " basic attack will trigger"
				+ " on-hit effects twice.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, new WristBladeInstance(this, data));
	}
	
	private class WristBladeInstance extends EquipmentInstance {
		private PlayerFightData data;
		private int count = 0;
		public WristBladeInstance(Equipment eq, PlayerFightData data) {
			super(eq);
			this.data = data;
		}
		
		@Override
		public boolean run(Object[] inputs) {
			if (++count >= hits) {
				data.runActions(Trigger.BASIC_ATTACK, inputs);
				count = 0;
			}
			return false;
		}
	}
}
