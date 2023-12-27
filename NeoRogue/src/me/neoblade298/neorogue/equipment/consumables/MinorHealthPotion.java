package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorHealthPotion extends Equipment {
	private double health;
	
	public MinorHealthPotion(boolean isUpgraded) {
		super("minorHealthPotion", "Minor Health Potion", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS, EquipmentType.CONSUMABLE);
		this.health = isUpgraded ? 50 : 35;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, (pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_WITCH_DRINK, false);
			data.getSessionData().removeEquipment(es, slot);
			data.addHealth(health);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Restores <yellow>" + health + "</yellow> health. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 0, 0));
		item.setItemMeta(meta);
	}
}
