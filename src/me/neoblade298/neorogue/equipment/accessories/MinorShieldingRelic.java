package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorShieldingRelic extends Equipment {
	private double shields;
	
	public MinorShieldingRelic(boolean isUpgraded) {
		super("minorShieldingRelic", "Minor Shielding Relic", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		shields = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pdata, in) -> {
			data.addShield(p.getUniqueId(), shields, true, 40L, 100, 0, 1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Whenever you gain " + GlossaryTag.SHIELDS.tag(this) + ", gain <yellow>"
				+ shields + "</yellow> bonus " + GlossaryTag.SHIELDS.tag(this) +" for <white>2</white> seconds.");
	}
}
