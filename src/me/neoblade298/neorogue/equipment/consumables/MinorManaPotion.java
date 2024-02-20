package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MinorManaPotion extends Consumable {
	private double mana;
	
	public MinorManaPotion(boolean isUpgraded) {
		super("minorManaPotion", "Minor Mana Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.mana = isUpgraded ? 3 : 2;
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
		data.addManaRegen(mana);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Increases your mana regen by <yellow>" + mana + "</yellow> for the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 0, 255));
		item.setItemMeta(meta);
	}
}
