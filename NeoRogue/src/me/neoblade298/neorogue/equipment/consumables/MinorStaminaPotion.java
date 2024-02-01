package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorStaminaPotion extends Consumable {
	private double stamina;
	
	public MinorStaminaPotion(boolean isUpgraded) {
		super("minorStaminaPotion", "Minor Stamina Potion", Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.stamina = isUpgraded ? 105 : 75;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, (pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_WITCH_DRINK, false);
			data.getSessionData().removeEquipment(es, slot);
			data.addStamina(stamina);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Restores <white>" + stamina + "</white> stamina. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 255, 0));
		item.setItemMeta(meta);
	}
}
