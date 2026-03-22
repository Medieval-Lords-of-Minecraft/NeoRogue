package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorShieldsPotion extends Consumable {
	private static final String ID = "MinorShieldsPotion";
	private double shields;
	
	public MinorShieldsPotion(boolean isUpgraded) {
		super(ID, "Minor Shields Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.shields = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
		return TriggerResult.keep();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Applies <yellow>" + shields + "</yellow> " + GlossaryTag.SHIELDS.tag(this) +
				". Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(255, 250, 205));
		item.setItemMeta(meta);
	}
}
