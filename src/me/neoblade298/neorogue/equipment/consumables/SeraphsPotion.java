package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class SeraphsPotion extends Consumable {
	private static final String ID = "SeraphsPotion";

	public SeraphsPotion(boolean isUpgraded) {
		super(ID, "Seraph's Potion", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.applyStatus(StatusType.INVINCIBLE, data, 1, 10);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Grants invulnerability for [<white>10s</white>]. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 255, 255));
		item.setItemMeta(meta);
	}
}
