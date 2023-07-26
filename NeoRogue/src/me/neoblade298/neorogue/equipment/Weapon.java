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
import me.neoblade298.neorogue.session.fights.DamageType;
import net.md_5.bungee.api.ChatColor;

public abstract class Weapon extends HotbarCompatible {
	protected double damage, attackSpeed;
	protected DamageType type;

	public Weapon(String id, boolean isUpgraded, Rarity rarity) {
		super(id, isUpgraded, rarity);
		// TODO Auto-generated constructor stub
	}

	public static ItemStack createItem(Weapon w, Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		
		// Add stats
		if (w.damage > 0) {
			preLore.add("§6Damage: §e" + w.damage);
			preLore.add("§6Damage Type: §e" + w.type.getDisplay());
		}
		if (w.attackSpeed > 0) preLore.add("§6Attack Speed: §e" + w.attackSpeed);
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(SharedUtil.translateColors(l));
			}
		}
		ItemStack item = Equipment.createItem(w, mat, "Weapon", preLore, loreLine, null);
		ItemMeta meta = item.getItemMeta();
		
		// Set attack speed if weapon is melee
		String name = item.getType().name();
		if (name.endsWith("SWORD") || name.endsWith("AXE")) {
			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), w.id, w.attackSpeed - 4, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
		item.setItemMeta(meta);
		return item;
	}
}
