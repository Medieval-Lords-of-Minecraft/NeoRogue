package me.neoblade298.neorogue.equipment.consumables;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WeaponsPotion extends Consumable {
	private static final String ID = "WeaponsPotion";

	public WeaponsPotion(boolean isUpgraded) {
		super(ID, "Weapons Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		PlayerSessionData sdata = data.getSessionData();
		Session s = sdata.getSession();
		int value = s.getBaseDropValue() + 2;
		Equipment weapon = Equipment.getDrop(value, sdata.getPlayerClass());
		weapon.initialize(data, null, EquipSlot.HOTBAR, slot);
		p.getInventory().setItem(slot, weapon.getItem());
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, "Replaces this consumable with a random weapon for the duration of the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(139, 69, 19));
		item.setItemMeta(meta);
	}
}
