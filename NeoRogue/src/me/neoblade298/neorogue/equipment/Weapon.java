package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.shared.util.SharedUtil;
import net.md_5.bungee.api.ChatColor;

public abstract class Weapon extends Usable {
	protected double damage, attackSpeed;

	public Weapon(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
		// TODO Auto-generated constructor stub
	}

	public static ItemStack createItem(Weapon w, Material mat, String[] preLoreLine, String loreLine) {
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + w.display + (w.isUpgraded ? "+" : ""));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§4Weapon");
		
		// Add stats
		if (w.damage > 0) lore.add("§6Damage: §e" + w.damage);
		if (w.attackSpeed > 0) lore.add("§6Attack Speed: §e" + w.attackSpeed);
		
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				lore.add(SharedUtil.translateColors(l));
			}
		}
		
		if (loreLine != null) {
			lore.addAll(SharedUtil.addLineBreaks(SharedUtil.translateColors(loreLine), 200, ChatColor.GRAY));
		}
		meta.setLore(lore);
		
		// Set attack speed if weapon is melee
		String name = item.getType().name();
		if (name.endsWith("SWORD") || name.endsWith("AXE")) {
			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), w.id, w.attackSpeed - 4, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", w.id);
		nbti.setString("type", "WEAPON");
		nbti.setBoolean("isUpgraded", w.isUpgraded);
		return nbti.getItem();
	}
}
