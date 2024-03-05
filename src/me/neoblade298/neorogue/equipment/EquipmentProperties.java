package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import net.kyori.adventure.text.Component;

public class EquipmentProperties {
	private static final EquipmentProperties NONE = new EquipmentProperties(0,0,0,0,0,0,0, null, null);
	private ArrayList<Component> lore;
	private HashMap<PropertyType, Property> properties = new HashMap<PropertyType, Property>();
	private DamageType type;
	private SoundContainer swingSound;
	
	private EquipmentProperties(double manaCost, double staminaCost, double cooldown, double range, double damage, double attackSpeed,
			double knockback, DamageType type, SoundContainer swingSound) {
		setupProperty(PropertyType.MANA_COST, manaCost);
		setupProperty(PropertyType.STAMINA_COST, staminaCost);
		setupProperty(PropertyType.COOLDOWN, cooldown);
		setupProperty(PropertyType.RANGE, range);
		setupProperty(PropertyType.DAMAGE, damage);
		setupProperty(PropertyType.KNOCKBACK, knockback);
		setupProperty(PropertyType.ATTACK_SPEED, attackSpeed);
		this.type = type;
		this.swingSound = swingSound;
	}
	
	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, 0, type, new SoundContainer(swingSound));
	}
	
	public static EquipmentProperties ofWeapon(double manaCost, double staminaCost, double damage, double attackSpeed, DamageType type, Sound swingSound) {
		return new EquipmentProperties(manaCost, staminaCost, 0, 0, damage, attackSpeed, 0, type, new SoundContainer(swingSound));
	}
	
	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, DamageType type, SoundContainer swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, 0, type, swingSound);
	}
	
	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, double knockback, DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, knockback, type, new SoundContainer(swingSound));
	}
	
	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, double knockback, DamageType type, SoundContainer swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, knockback, type, swingSound);
	}
	
	public static EquipmentProperties ofUsable(double manaCost, double staminaCost, double cooldown, double range) {
		return new EquipmentProperties(manaCost, staminaCost, cooldown, range, 0, 0, 0, null, null);
	}
	
	public static EquipmentProperties none() {
		return NONE;
	}
	
	private void setupProperty(PropertyType type, double amount) {
		if (amount == 0) return;
		properties.put(type, new Property(amount));
	}
	
	public ArrayList<Component> generateLore(Equipment eq) {
		if (lore != null) return lore;
		ArrayList<Component> lore = new ArrayList<Component>();
		for (PropertyType type : PropertyType.values()) {
			if (!properties.containsKey(type)) continue;
			lore.add(generateLoreLine(type));
		}
		if (type != null) {
			lore.add(SharedUtil.color("<gold>Damage Type: <white>" + type));
			eq.addTags(DamageType.toGlossary(type));
		}
		return lore;
	}
	
	public DamageMeta getDamageMeta(FightData owner) {
		return new DamageMeta(owner, get(PropertyType.DAMAGE), type);
	}
	
	private Component generateLoreLine(PropertyType type) {
		Property prop = properties.get(type);
		String color = prop.canUpgrade ? "<yellow>" : "<white>";
		return SharedUtil.color("<gold>" + type.label + color + prop.amount);
	}
	
	public boolean contains(PropertyType type) {
		return properties.containsKey(type);
	}
	
	public double get(PropertyType type) {
		return properties.containsKey(type) ? properties.get(type).amount : 0;
	}
	
	public boolean has(PropertyType type) {
		return properties.containsKey(type);
	}

	public DamageType getType() {
		return type;
	}

	public void setType(DamageType type) {
		this.type = type;
	}

	public SoundContainer getSwingSound() {
		return swingSound;
	}

	public void setSwingSound(SoundContainer swingSound) {
		this.swingSound = swingSound;
	}

	public void modifyItemMeta(ItemStack item, ItemMeta meta) {
		String name = item.getType().name();
		double attackSpeed = properties.containsKey(PropertyType.ATTACK_SPEED) ? properties.get(PropertyType.ATTACK_SPEED).amount : 0;
		if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("HOE") || name.endsWith("SHOVEL")) {
			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "neorogue_attackspeed", attackSpeed - 4, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
	}
	
	public void addUpgrades(PropertyType... types) {
		for (PropertyType type : types) {
			properties.get(type).canUpgrade = true;
		}
	}
	
	public enum PropertyType {
		MANA_COST("Mana Cost: "),
		STAMINA_COST("Stamina Cost: "),
		RANGE("Range: "),
		COOLDOWN("Cooldown: "),
		DAMAGE("Damage: "),
		KNOCKBACK("Knockback: "),
		ATTACK_SPEED("Attack Speed: ");
		
		private String label;
		private PropertyType(String label) {
			this.label = label;
		}
	}
	
	private class Property {
		private double amount;
		private boolean canUpgrade;
		protected Property(double amount) {
			this.amount = amount;
		}
	}
}
