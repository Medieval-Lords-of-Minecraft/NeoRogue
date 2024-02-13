package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MinorMagicalPotion extends Consumable {
	private double intel;
	
	public MinorMagicalPotion(boolean isUpgraded) {
		super("minorMagicalPotion", "Minor Magical Potion", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS);
		this.intel = isUpgraded ? 30 : 20;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, (pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_WITCH_DRINK, false);
			data.getSessionData().removeEquipment(es, slot);
			data.addBuff(p.getUniqueId(), true, false, BuffType.MAGICAL, intel);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Increases your " + GlossaryTag.MAGICAL.tag(this)
		+ " by <yellow>" + intel + "</yellow> for the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(0, 0, 255));
		item.setItemMeta(meta);
	}
}
