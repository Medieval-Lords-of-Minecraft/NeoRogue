package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;

public abstract class Weapon extends HotbarCompatible {
	protected double damage, attackSpeed;
	protected DamageType type;

	public Weapon(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec);
	}
	
	protected void setBaseProperties(double damage, double attackSpeed, DamageType type) {
		this.damage = damage;
		this.attackSpeed = attackSpeed;
		this.type = type;
	}

	public ItemStack createItem(Material mat, String[] preLoreLine, String loreLine) {
		ArrayList<String> preLore = new ArrayList<String>();
		
		// Add stats
		if (damage > 0) {
			preLore.add("<gold>Damage: <yellow>" + damage);
			preLore.add("<gold>Damage Type: <yellow>" + type.getDisplay());
		}
		if (attackSpeed > 0) preLore.add("<gold>Attack Speed: <yellow>" + attackSpeed + "/s");
		addToLore(preLore);
		if (preLoreLine != null) {
			for (String l : preLoreLine) {
				preLore.add(l);
			}
		}
		ItemStack item = createItem(mat, "Weapon", preLore, loreLine);
		ItemMeta meta = item.getItemMeta();
		
		// Set attack speed if weapon is melee
		String name = item.getType().name();
		if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("HOE") || name.endsWith("SHOVEL")) {
			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), id, attackSpeed - 4, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public double getAttackSpeed() {
		return attackSpeed;
	}
	
	protected void dealDamage(Player p, Damageable target) {
		FightInstance.dealDamage(p, type, damage, target);
	}
}
