package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MinorStaminaPotion extends Consumable {
	private static final String ID = "minorStaminaPotion";
	private double stamina;
	
	public MinorStaminaPotion(boolean isUpgraded) {
		super(ID, "Minor Stamina Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.stamina = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
		data.addStaminaRegen(stamina);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Increases your stamina regen by <yellow>" + stamina + "</yellow> for the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 255, 0));
		item.setItemMeta(meta);
	}
}
