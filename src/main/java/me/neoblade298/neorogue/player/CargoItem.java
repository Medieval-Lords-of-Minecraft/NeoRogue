package me.neoblade298.neorogue.player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import de.tr7zw.nbtapi.NBT;
import me.ascheladd.asheconomy.pricing.ItemPriceQuote;
import me.ascheladd.asheconomy.pricing.MaterialPrices;

/**
 * Immutable identity and one-item prototype for a cargo variant. Only the metadata explicitly
 * supported by AshEconomy is retained: a vanilla potion's base brew, or an enchanted book's
 * stored enchantments and levels. GUI metadata and arbitrary custom/plugin data never enter the
 * prototype.
 */
public final class CargoItem {
	private static final String MATERIAL_PREFIX = "material:";
	private static final String POTION_PREFIX = "potion:";
	private static final String BOOK_PREFIX = "book:";

	private final String key;
	private final ItemStack prototype;

	private CargoItem(String key, ItemStack prototype) {
		this.key = Objects.requireNonNull(key, "key");
		this.prototype = prototype.clone();
		this.prototype.setAmount(1);
	}

	/** Builds a cargo identity only when the exact stack is safely storable and currently quoted. */
	public static Optional<CargoItem> fromItem(ItemStack item) {
		return fromItem(item, true);
	}

	private static Optional<CargoItem> fromItem(ItemStack item, boolean requireQuote) {
		if (item == null || item.getType().isAir()) return Optional.empty();
		if (NBT.get(item, nbt -> { return nbt.hasTag("equipId"); })) return Optional.empty();

		ItemStack prototype;
		String key;
		if (isPotion(item.getType())) {
			if (!(item.getItemMeta() instanceof PotionMeta source) || source.hasCustomEffects()) return Optional.empty();
			PotionType type = source.getBasePotionType();
			if (type == null) return Optional.empty();
			prototype = new ItemStack(item.getType());
			PotionMeta clean = (PotionMeta) prototype.getItemMeta();
			clean.setBasePotionType(type);
			prototype.setItemMeta(clean);
			key = POTION_PREFIX + item.getType().name() + ":" + type.name();
		} else if (item.getType() == Material.ENCHANTED_BOOK) {
			if (!(item.getItemMeta() instanceof EnchantmentStorageMeta source)) return Optional.empty();
			Map<Enchantment, Integer> enchants = source.getStoredEnchants();
			if (enchants.isEmpty()) return Optional.empty();
			List<Map.Entry<Enchantment, Integer>> sorted = new ArrayList<>(enchants.entrySet());
			sorted.sort(Comparator.comparing(entry -> entry.getKey().getKey().toString()));
			prototype = new ItemStack(Material.ENCHANTED_BOOK);
			EnchantmentStorageMeta clean = (EnchantmentStorageMeta) prototype.getItemMeta();
			StringBuilder canonical = new StringBuilder(BOOK_PREFIX);
			for (int i = 0; i < sorted.size(); i++) {
				Map.Entry<Enchantment, Integer> entry = sorted.get(i);
				if (entry.getValue() <= 0) return Optional.empty();
				clean.addStoredEnchant(entry.getKey(), entry.getValue(), true);
				if (i > 0) canonical.append(',');
				canonical.append(entry.getKey().getKey()).append('=').append(entry.getValue());
			}
			prototype.setItemMeta(clean);
			key = boundedBookKey(canonical.toString());
		} else {
			prototype = new ItemStack(item.getType());
			key = MATERIAL_PREFIX + item.getType().name();
		}

		ItemStack one = item.clone();
		one.setAmount(1);
		// Comparing against a freshly constructed whitelist prototype rejects names, lore, damage,
		// ordinary enchants, custom model/data components, item flags, attributes and plugin metadata.
		if (!one.isSimilar(prototype)) return Optional.empty();
		if (requireQuote && MaterialPrices.quote(prototype).isEmpty()) return Optional.empty();
		return Optional.of(new CargoItem(key, prototype));
	}

	/** Restores a persisted identity. Null data is accepted only for migrated ordinary-material rows. */
	public static Optional<CargoItem> fromStorage(String materialName, String itemKey, String itemData) {
		Material material = Material.getMaterial(materialName);
		if (material == null || itemKey == null || itemKey.isBlank()) return Optional.empty();
		if (itemData == null || itemData.isBlank()) {
			if (!itemKey.equals(MATERIAL_PREFIX + material.name())) return Optional.empty();
			if (isPotion(material) || material == Material.ENCHANTED_BOOK) return Optional.empty();
			return fromItem(new ItemStack(material), false);
		}
		try {
			ItemStack decoded = ItemStack.deserializeBytes(Base64.getDecoder().decode(itemData));
			Optional<CargoItem> restored = fromItem(decoded, false);
			if (restored.isEmpty() || !restored.get().key.equals(itemKey)
					|| restored.get().getMaterial() != material) return Optional.empty();
			return restored;
		} catch (RuntimeException ex) {
			return Optional.empty();
		}
	}

	private static boolean isPotion(Material material) {
		return material == Material.POTION || material == Material.SPLASH_POTION
				|| material == Material.LINGERING_POTION;
	}

	private static String boundedBookKey(String fullKey) {
		if (fullKey.length() <= 512) return fullKey;
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(fullKey.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(64);
			for (byte value : digest) hex.append(String.format("%02x", value));
			return fullKey.substring(0, 439) + "#sha256:" + hex;
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}

	public String getKey() {
		return key;
	}

	public Material getMaterial() {
		return prototype.getType();
	}

	/** Returns a defensive one-item clone suitable for pricing or withdrawal. */
	public ItemStack createStack() {
		return createStack(1);
	}

	public ItemStack createStack(int amount) {
		ItemStack copy = prototype.clone();
		copy.setAmount(amount);
		return copy;
	}

	public String serializePrototype() {
		return Base64.getEncoder().encodeToString(prototype.serializeAsBytes());
	}

	public Optional<ItemPriceQuote> quote() {
		return MaterialPrices.quote(prototype);
	}

	public double getEffectivePrice() {
		return quote().map(ItemPriceQuote::effectivePrice).orElse(0.0);
	}

	public String getLabel() {
		if (prototype.getItemMeta() instanceof EnchantmentStorageMeta meta) {
			List<Map.Entry<Enchantment, Integer>> enchants = new ArrayList<>(meta.getStoredEnchants().entrySet());
			enchants.sort(Comparator.comparing(entry -> entry.getKey().getKey().toString()));
			StringBuilder label = new StringBuilder("Enchanted Book (");
			for (int i = 0; i < enchants.size(); i++) {
				if (i > 0) label.append(", ");
				Map.Entry<Enchantment, Integer> enchant = enchants.get(i);
				label.append(prettyName(enchant.getKey().getKey().getKey())).append(' ').append(enchant.getValue());
			}
			return label.append(')').toString();
		}
		return quote().map(ItemPriceQuote::label).orElseGet(() -> prettyName(getMaterial()));
	}

	private static String prettyName(String key) {
		String[] words = key.split("_");
		StringBuilder label = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty()) continue;
			if (label.length() > 0) label.append(' ');
			label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return label.toString();
	}

	private static String prettyName(Material material) {
		return prettyName(material.name().toLowerCase());
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof CargoItem item && key.equals(item.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public String toString() {
		return key;
	}
}
