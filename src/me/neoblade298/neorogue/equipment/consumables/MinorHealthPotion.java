package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MinorHealthPotion extends Consumable {
	private double health;
	
	public MinorHealthPotion(boolean isUpgraded) {
		super("minorHealthPotion", "Minor Health Potion", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);
		this.health = isUpgraded ? 30 : 20;
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
		FightInstance.giveHeal(p, health, p);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Restores <yellow>" + health + "</yellow> health. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 0, 0));
		item.setItemMeta(meta);
	}
}
