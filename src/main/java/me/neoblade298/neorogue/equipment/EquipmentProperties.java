package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.DamageType;
import net.kyori.adventure.text.Component;

public class EquipmentProperties {
	private static final EquipmentProperties NONE = new EquipmentProperties(0, 0, 0, 0, 0, 0, 0, 0, 0, null, null);
	private ArrayList<Component> lore;
	private HashMap<PropertyType, Property> properties = new HashMap<PropertyType, Property>();
	private DamageType type;
	private SoundContainer swingSound;
	private CastType castType = CastType.STANDARD;
	public static final String PROPERTY_COLOR = "#C98D8D";

	private EquipmentProperties(double manaCost, double staminaCost, double cooldown, double range, double damage,
			double attackSpeed, double knockback, double chargeTime, double area, DamageType type, SoundContainer swingSound) {
		add(PropertyType.MANA_COST, manaCost);
		add(PropertyType.STAMINA_COST, staminaCost);
		add(PropertyType.COOLDOWN, cooldown);
		add(PropertyType.RANGE, range);
		add(PropertyType.DAMAGE, damage);
		add(PropertyType.KNOCKBACK, knockback);
		add(PropertyType.CHARGE_TIME, chargeTime);
		add(PropertyType.ATTACK_SPEED, attackSpeed);
		add(PropertyType.AREA_OF_EFFECT, area);
		this.type = type;
		this.swingSound = swingSound;
	}

	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, 0, 0, 0, type, new SoundContainer(swingSound));
	}

	public static EquipmentProperties ofWeapon(double manaCost, double staminaCost, double damage, double attackSpeed,
			double knockback, DamageType type, SoundContainer swingSound) {
		return new EquipmentProperties(manaCost, staminaCost, 0, 0, damage, attackSpeed, knockback, 0, 0, type,
				swingSound);
	}

	public static EquipmentProperties ofWeapon(double manaCost, double staminaCost, double damage, double attackSpeed,
			double knockback, DamageType type, Sound swingSound) {
		return new EquipmentProperties(manaCost, staminaCost, 0, 0, damage, attackSpeed, knockback, 0, 0, type,
				new SoundContainer(swingSound));
	}

	public static EquipmentProperties ofWeapon(double manaCost, double staminaCost, double damage, double attackSpeed,
			DamageType type, Sound swingSound) {
		return new EquipmentProperties(manaCost, staminaCost, 0, 0, damage, attackSpeed, 0, 0, 0, type,
				new SoundContainer(swingSound));
	}

	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, DamageType type,
			SoundContainer swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, 0, 0, 0, type, swingSound);
	}

	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, double knockback, DamageType type) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, knockback, 0, 0, type, null);
	}

	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, double knockback, DamageType type,
			Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, knockback, 0, 0, type,
				new SoundContainer(swingSound));
	}

	public static EquipmentProperties ofWeapon(double damage, double attackSpeed, double knockback, DamageType type,
			SoundContainer swingSound) {
		return new EquipmentProperties(0, 0, 0, 0, damage, attackSpeed, knockback, 0, 0, type, swingSound);
	}

	public static EquipmentProperties ofRangedWeapon(double damage, double attackSpeed, double knockback, double range,
			DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, range, damage, attackSpeed, knockback, 0, 0, type,
				new SoundContainer(swingSound));
	}

	public static EquipmentProperties ofRangedWeapon(double damage, double attackSpeed, double knockback, double range,
			DamageType type, SoundContainer swingSound) {
		return new EquipmentProperties(0, 0, 0, range, damage, attackSpeed, knockback, 0, 0, type, swingSound);
	}

	public static EquipmentProperties ofRangedWeapon(double damage, double attackSpeed, double knockback, double range,
			DamageType type) {
		return new EquipmentProperties(0, 0, 0, range, damage, attackSpeed, knockback, 0, 0, type, null);
	}

	public static EquipmentProperties ofWand(double damage, double attackSpeed, double knockback, double chargeTime, double range,
			DamageType type, Sound swingSound) {
		return new EquipmentProperties(0, 0, 0, range, damage, attackSpeed, knockback, chargeTime, 0, type,
				new SoundContainer(swingSound));
	}

	public static EquipmentProperties ofBow(double damage, double arrowSpeed, double knockback, double range,
			double manaCost, double staminaCost) {
		EquipmentProperties props = new EquipmentProperties(manaCost, staminaCost, 0, range, damage, 0,
				knockback, 0, 0, null, null).add(PropertyType.ARROW_SPEED, arrowSpeed);
		return props;
	}

	public static EquipmentProperties custom(double manaCost, double staminaCost, double cooldown, double range,
			double damage, double attackSpeed, double knockback, DamageType type, SoundContainer swingSound) {
		return new EquipmentProperties(manaCost, staminaCost, cooldown, range, damage, attackSpeed, knockback, 0, 0, type,
				swingSound);
	}

	public static EquipmentProperties ofUsable(double manaCost, double staminaCost, double cooldown, double range) {
		return new EquipmentProperties(manaCost, staminaCost, cooldown, range, 0, 0, 0, 0, 0, null, null);
	}

	public static EquipmentProperties ofUsable(double manaCost, double staminaCost, double cooldown, double range,
			double aoe) {
		return new EquipmentProperties(manaCost, staminaCost, cooldown, range, 0, 0, 0, 0, aoe, null, null);
	}

	public static EquipmentProperties ofAmmunition(double damage, double knockback, DamageType type) {
		return new EquipmentProperties(0, 0, 0, 0, damage, 0, knockback, 0, 0, type, null);
	}

	public EquipmentProperties clone() {
		return new EquipmentProperties(get(PropertyType.MANA_COST), get(PropertyType.STAMINA_COST),
				get(PropertyType.COOLDOWN), get(PropertyType.RANGE), get(PropertyType.DAMAGE),
				get(PropertyType.ATTACK_SPEED), get(PropertyType.KNOCKBACK), get(PropertyType.CHARGE_TIME),
				get(PropertyType.AREA_OF_EFFECT), type, swingSound);
	}

	public static EquipmentProperties none() {
		return NONE.clone();
	}

	public EquipmentProperties add(PropertyType type, double amount) {
		if (this == NONE) {
			try {
				throw new Exception("Attempted to add " + amount + " " + type + " to NONE");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (amount == 0)
			return this;
		properties.put(type, new Property(amount));
		return this;
	}

	// Compact stat portion of the tooltip (e.g. "15 MP · 14 Range · 10s CD"), or null if this equipment
	// has no numeric properties. The caller merges this onto the rarity/type header line.
	public String getStatLineString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (PropertyType pt : PropertyType.values()) {
			if (!properties.containsKey(pt))
				continue;
			if (!first)
				sb.append("<color:" + PROPERTY_COLOR + "> · ");
			sb.append(generateLoreToken(pt));
			first = false;
		}
		return sb.length() > 0 ? sb.toString() : null;
	}

	// The "Damage Type: X" line (null if this equipment has no damage type). Also registers the damage
	// type's glossary tags on the equipment.
	public Component getDamageTypeLine(Equipment eq) {
		if (type == null)
			return null;
		eq.addTags(type.toGlossary());
		return SharedUtil.color("<color:" + PROPERTY_COLOR + ">Damage Type: <white>" + type);
	}

	// Builds a single compact stat token like "<yellow>15<color:...> MP" for the combined stat line.
	// Upgradeable values are yellow, fixed values white; the abbreviated label uses the property color.
	private String generateLoreToken(PropertyType type) {
		Property prop = properties.get(type);
		String color = prop.canUpgrade ? "<yellow>" : "<white>";
		String valueSuffix = "";
		String label;
		switch (type) {
		case MANA_COST:
			label = "MP";
			break;
		case STAMINA_COST:
			label = "SP";
			break;
		case RANGE:
			label = "Range";
			break;
		case COOLDOWN:
			label = "CD";
			valueSuffix = "s";
			break;
		case CHARGE_TIME:
			label = "Charge";
			valueSuffix = "s";
			break;
		case DAMAGE:
			label = "Dmg";
			break;
		case KNOCKBACK:
			label = "KB";
			break;
		case AREA_OF_EFFECT:
			label = "AoE";
			break;
		case ATTACK_SPEED:
			label = "Atk Spd";
			valueSuffix = "/s";
			break;
		case ARROW_SPEED:
			label = "Arrow Spd";
			valueSuffix = "x";
			break;
		default:
			label = type.label;
			break;
		}
		return color + formatAmount(prop.amount) + valueSuffix + "<color:" + PROPERTY_COLOR + "> " + label;
	}

	// Formats a stat value without a trailing ".0" for whole numbers (15.0 -> "15", 1.5 -> "1.5").
	private static String formatAmount(double amount) {
		if (amount == Math.rint(amount) && !Double.isInfinite(amount)) {
			return Long.toString((long) amount);
		}
		return Double.toString(amount);
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

	public CastType getCastType() {
		return castType;
	}

	public void setCastType(CastType castType) {
		this.castType = castType;
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
		double attackSpeed = properties.containsKey(PropertyType.ATTACK_SPEED)
				? properties.get(PropertyType.ATTACK_SPEED).amount
				: 0;
		if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("HOE") || name.endsWith("SHOVEL")) {
			meta.addAttributeModifier(Attribute.ATTACK_SPEED,
					new AttributeModifier(new NamespacedKey(NeoRogue.inst(), "neorogue_attackspeed"), attackSpeed - 4,
							Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
		}
	}

	public void addUpgrades(PropertyType... types) {
		for (PropertyType type : types) {
			properties.get(type).canUpgrade = true;
		}
	}

	public enum PropertyType {
		MANA_COST("Mana Cost"), STAMINA_COST("Stamina Cost"), RANGE("Range"), COOLDOWN("Cooldown"),
		DAMAGE("Damage"), KNOCKBACK("Knockback"), AREA_OF_EFFECT("Area of Effect"),
		ATTACK_SPEED("Attack Speed"), ARROW_SPEED("Arrow Speed"), CHARGE_TIME("Charge Time");

		private String label;

		private PropertyType(String label) {
			this.label = label;
		}

		public String getDisplay() {
			return label;
		}
	}

	@Override
	public String toString() {
		String str = "";
		for (PropertyType type : PropertyType.values()) {
			if (properties.containsKey(type)) {
				str += type.label + ": " + properties.get(type).amount + ",";
			}
		}
		return str;
	}

	private class Property {
		private double amount;
		private boolean canUpgrade;

		protected Property(double amount) {
			this.amount = amount;
		}
	}

	public static enum CastType {
		STANDARD,
		TOGGLE, // Toggleable abilities that can be cast multiple times without waiting for
							// cooldown
		POST_TRIGGER // Abilities that can fail post-trigger, like ground lance requiring a block to
						// be aimed at. These abilities MUST MANUALLY trigger CAST_USABLE
	}
}
