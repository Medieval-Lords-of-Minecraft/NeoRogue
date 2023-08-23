package me.neoblade298.neorogue.session;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.EquipmentChoiceInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class EquipmentChoiceReward implements Reward {
	private ArrayList<Equipment> equips;
	
	public EquipmentChoiceReward(ArrayList<Equipment> equips) {
		this.equips = equips;
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		new EquipmentChoiceInventory(data, Bukkit.createInventory(data.getPlayer(), 9, "§9Choose one!"), equips, inv, slot);
		return false;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		item.setAmount(equips.size());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Choose 1 of " + equips.size() + " equipment");
		ArrayList<String> lore = new ArrayList<String>();
		for (Equipment equip : equips) {
			lore.add(equip.getDisplay());
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}
