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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorPhysicalPotion extends Consumable {
	private static final String ID = "MinorPhysicalPotion";
	private int strength;
	
	public MinorPhysicalPotion(boolean isUpgraded) {
		super(ID, "Minor Physical Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.strength = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		data.applyStatus(StatusType.STRENGTH, data, strength, -1);
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Increases your " + GlossaryTag.STRENGTH.tag(this) +
				" by <yellow>" + strength + "</yellow> for the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 0, 255));
		item.setItemMeta(meta);
	}
}
