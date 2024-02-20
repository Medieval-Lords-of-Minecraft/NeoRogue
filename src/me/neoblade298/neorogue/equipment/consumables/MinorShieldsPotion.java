package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class MinorShieldsPotion extends Consumable {
	private double shields;
	
	public MinorShieldsPotion(boolean isUpgraded) {
		super("minorShieldsPotion", "Minor Shields Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.shields = isUpgraded ? 60 : 40;
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
		data.addPermanentShield(p.getUniqueId(), shields);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Applies <yellow>" + shields + "</yellow> " + GlossaryTag.SHIELDS.tag(this) +
				". Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 0, 255));
		item.setItemMeta(meta);
	}
}
