package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorHealthPotion extends Consumable {
	private static final String ID = "MinorHealthPotion";
	private double healthPct;
	
	public MinorHealthPotion(boolean isUpgraded) {
		super(ID, "Minor Health Potion", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);
		this.healthPct = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		FightInstance.giveHeal(p, data.getMaxHealth() * healthPct, p);
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Restores " + DescUtil.yellow((int)(healthPct * 100) + "%") + " of max health. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 0, 0));
		item.setItemMeta(meta);
	}
}
