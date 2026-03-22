package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorManaPotion extends Consumable {
	private static final String ID = "MinorManaPotion";
	private double mana;
	
	public MinorManaPotion(boolean isUpgraded) {
		super(ID, "Minor Mana Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.mana = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.addManaRegen(mana);
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Increases your mana regen by <yellow>" + mana + "</yellow> for the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 0, 255));
		item.setItemMeta(meta);
	}
}
