package me.neoblade298.neorogue.equipment.consumables;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class MinorMagicalPotion extends Consumable {
	private static final String ID = "minorMagicalPotion";
	private double intel;
	
	public MinorMagicalPotion(boolean isUpgraded) {
		super(ID, "Minor Magical Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.intel = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void runConsumableEffects(Player p, PlayerFightData data) {
		String buffId = UUID.randomUUID().toString();
		data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL), new Buff(data, intel, 0, StatTracker.damageBuffAlly(buffId, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Increases your " + GlossaryTag.MAGICAL.tag(this)
		+ " damage by <yellow>" + intel + "</yellow> for the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 0, 255));
		item.setItemMeta(meta);
	}
}
