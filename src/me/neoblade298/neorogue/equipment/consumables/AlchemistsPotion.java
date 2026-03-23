package me.neoblade298.neorogue.equipment.consumables;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AlchemistsPotion extends Consumable {
	private static final String ID = "AlchemistsPotion";

	public AlchemistsPotion(boolean isUpgraded) {
		super(ID, "Alchemist's Potion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public TriggerResult runConsumableEffects(Player p, PlayerFightData data, int slot) {
		PlayerSessionData sdata = data.getSessionData();
		Session s = sdata.getSession();
		int value = s.getBaseDropValue();
		Equipment[] hotbar = sdata.getEquipment(EquipSlot.HOTBAR);

		// Find up to 2 empty hotbar slots (include own slot since this potion is consumed)
		ArrayList<Integer> emptySlots = new ArrayList<>();
		emptySlots.add(slot);
		for (int i = 0; i < hotbar.length && emptySlots.size() < 2; i++) {
			if (i == slot) continue;
			if (hotbar[i] == null) {
				emptySlots.add(i);
			}
		}

		ArrayList<Consumable> consumables = Equipment.getConsumable(value, emptySlots.size(), sdata.getPlayerClass());
		for (int i = 0; i < consumables.size(); i++) {
			int emptySlot = emptySlots.get(i);
			Consumable c = consumables.get(i);
			if (isUpgraded) c = (Consumable) c.getUpgraded();
			c.initialize(data, Trigger.getFromHotbarSlot(emptySlot), EquipSlot.HOTBAR, emptySlot);
			p.getInventory().setItem(emptySlot, c.getItem());
		}
		return TriggerResult.remove();
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"Creates <white>2</white> random " + (isUpgraded ? DescUtil.yellow("upgraded") : "") + " consumables in empty hotbar slots for the duration of the fight. Consumed on first use.");
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(50, 205, 50));
		item.setItemMeta(meta);
	}
}
