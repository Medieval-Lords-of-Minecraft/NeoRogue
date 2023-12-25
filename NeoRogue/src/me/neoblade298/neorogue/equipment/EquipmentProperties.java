package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.fight.DamageType;
import net.kyori.adventure.text.Component;

public class EquipmentProperties {
	private static final EquipmentProperties NONE = new EquipmentProperties(0,0,0,0,0,0,0, null, null);
	private ArrayList<Component> lore;
	private double manaCost, staminaCost, cooldown, range, damage, attackSpeed, knockback;
	private DamageType type;
	private Sound swingSound;
	
	private EquipmentProperties(double manaCost, double staminaCost, double cooldown, double range, double damage, double attackSpeed,
			double knockback, DamageType type, Sound swingSound) {
		this.manaCost = manaCost;
		this.staminaCost = staminaCost;
		this.cooldown = cooldown;
		this.range = range;
		this.damage = damage;
		this.attackSpeed = attackSpeed;
		this.type = type;
	}
	
	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, 0, type, swingSound);
	}
	
	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, double knockback, DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, knockback, type, null);
	}
	
	public static EquipmentProperties ofUsable(double manaCost, double staminaCost, double cooldown, double range) {
		return new EquipmentProperties(manaCost, staminaCost, cooldown, range, 0, 0, 0, null, null);
	}
	
	public static EquipmentProperties none() {
		return NONE;
	}
	
	public ArrayList<Component> generateLore() {
		if (lore != null) return lore;
		ArrayList<Component> lore = new ArrayList<Component>();
		if (manaCost > 0) lore.add(SharedUtil.color("<gold>Mana Cost: <yellow>" + manaCost));
		if (staminaCost > 0) lore.add(SharedUtil.color("<gold>Stamina Cost: <yellow>" + staminaCost));
		if (range > 0) lore.add(SharedUtil.color("<gold>Range: <yellow>" + range));
		if (cooldown > 0) lore.add(SharedUtil.color("<gold>Cooldown: <yellow>" + cooldown));
		if (damage > 0) lore.add(SharedUtil.color("<gold>Damage: <yellow>" + damage));
		if (damage > 0) lore.add(SharedUtil.color("<gold>Knockback: <yellow>" + knockback));
		if (type != null) lore.add(SharedUtil.color("<gold>Damage Type: <yellow>" + type));
		if (attackSpeed > 0) lore.add(SharedUtil.color("<gold>Attack Speed: <yellow>" + attackSpeed + "/s"));
		return lore;
	}
	
	public double getManaCost() {
		return manaCost;
	}

	public void setManaCost(double manaCost) {
		this.manaCost = manaCost;
	}

	public double getStaminaCost() {
		return staminaCost;
	}

	public void setStaminaCost(double staminaCost) {
		this.staminaCost = staminaCost;
	}

	public double getCooldown() {
		return cooldown;
	}

	public void setCooldown(double cooldown) {
		this.cooldown = cooldown;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public void setAttackSpeed(double attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public DamageType getType() {
		return type;
	}

	public void setType(DamageType type) {
		this.type = type;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public Sound getSwingSound() {
		return swingSound;
	}

	public void setSwingSound(Sound swingSound) {
		this.swingSound = swingSound;
	}

	public void modifyItemMeta(ItemStack item, ItemMeta meta) {
		String name = item.getType().name();
		if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("HOE") || name.endsWith("SHOVEL")) {
			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "neorogue_attackspeed", attackSpeed - 4, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
	}
}
