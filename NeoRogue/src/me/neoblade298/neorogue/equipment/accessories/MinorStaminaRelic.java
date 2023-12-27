package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TickAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class MinorStaminaRelic extends Equipment {
	private double regen;
	
	public MinorStaminaRelic(boolean isUpgraded) {
		super("minorStaminaRelic", "Minor Stamina Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		regen = isUpgraded ? 1.5 : 1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTickAction(new MinorStaminaRelicTick(data));
	}
	
	private class MinorStaminaRelicTick extends TickAction {
		private PlayerFightData data;
		public MinorStaminaRelicTick(PlayerFightData data) {
			this.data = data;
		}
		
		@Override
		public TickResult run() {
			data.addStamina(regen);
			return TickResult.KEEP;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, "Increases stamina regen by <yellow>" + regen + "</yellow>.");
	}
}
