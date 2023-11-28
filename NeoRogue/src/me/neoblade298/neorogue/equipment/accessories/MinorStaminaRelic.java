package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Accessory;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;
import me.neoblade298.neorogue.session.fights.TickAction;

public class MinorStaminaRelic extends Accessory {
	private double regen;
	
	public MinorStaminaRelic(boolean isUpgraded) {
		super("minorStaminaRelic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		regen = isUpgraded ? 1.5 : 1;
		display = "Earthen Ring";
		item = createItem(Material.GOLD_NUGGET, "ACCESSORY", reforgeOptions, "Increases stamina regen by <yellow>" + regen + "</yellow>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addTickAction(new MinorStaminaRelicTick(data));
	}
	
	private class MinorStaminaRelicTick extends TickAction {
		private PlayerFightData data;
		public MinorStaminaRelicTick(PlayerFightData data) {
			this.data = data;
		}
		
		@Override
		public boolean run() {
			data.addStamina(regen);
			return false;
		}
	}
}
