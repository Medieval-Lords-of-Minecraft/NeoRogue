package me.neoblade298.neorogue.session.reward;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.EquipmentChoiceInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EquipmentChoiceReward implements Reward {
	private ArrayList<Equipment> equips;
	private ItemStack icon;
	
	public EquipmentChoiceReward(ArrayList<Equipment> equips) {
		this.equips = equips;
	}

	public EquipmentChoiceReward(ArrayList<Equipment> equips, ItemStack icon) {
		this.equips = equips;
		this.icon = icon;
	}
	
	public EquipmentChoiceReward(String str) {
		this.equips = Equipment.deserializeAsArrayList(str);
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		new EquipmentChoiceInventory(data, Bukkit.createInventory(data.getPlayer(), 9, Component.text("Choose one!", NamedTextColor.BLUE)),
				equips, inv, slot);
		return false;
	}

	public void spectate(PlayerSessionData data, int slot, RewardInventory inv, Player spectator) {
		new EquipmentChoiceInventory(data, Bukkit.createInventory(data.getPlayer(), 9, Component.text("Choose one!", NamedTextColor.BLUE)),
				equips, inv, slot, spectator);
	}
	@Override
	public ItemStack getIcon() {
		ItemStack item = icon != null ? icon : new ItemStack(Material.CHEST);
		item.setAmount(equips.size());
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Choose 1 of " + equips.size() + " equipment", NamedTextColor.GOLD));
		ArrayList<Component> lore = new ArrayList<Component>();
		for (Equipment equip : equips) {
			lore.add(equip.getDisplay());
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String serialize() {
		return "choice:" + Equipment.serialize(equips);
	}

}
