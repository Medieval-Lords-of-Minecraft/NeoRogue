package me.neoblade298.neorogue.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

/**
 * A session-scoped wrapper around an Equipment template. Holds a general-purpose
 * string-keyed metadata store for mutable session-level state (e.g. durability,
 * enchantments, counters).
 * 
 * Serialization format (inline, backward-compatible):
 *   equipId+|key1=i:5,key2=d:1.5,key3=s:hello
 * Entries without '|' are treated as legacy (no metadata).
 */
public class SessionEquipment {
	public static final String DURABILITY_KEY = "dur";
	// NBT key used to persist the full serialized SessionEquipment (including metadata) on the
	// item form, so metadata survives round-trips through items (e.g. moving items into storage).
	public static final String NBT_KEY = "sessionEquip";
	private Equipment equipment;
	private HashMap<String, Object> metadata;

	public SessionEquipment(Equipment equipment) {
		this.equipment = equipment;
		this.metadata = new HashMap<>();
	}

	private SessionEquipment(Equipment equipment, HashMap<String, Object> metadata) {
		this.equipment = equipment;
		this.metadata = metadata;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	// --- Display Methods ---

	public ItemStack getItem() {
		ItemStack item = equipment.getItem();
		applyExtraData(item);
		// Persist metadata onto the item so it survives being reconstructed from item form.
		if (!metadata.isEmpty()) {
			String serialized = serialize();
			NBT.modify(item, nbt -> { nbt.setString(NBT_KEY, serialized); });
		}
		return item;
	}

	// Appends this equipment's session-specific extra data (currently just durability) as lore.
	private void applyExtraData(ItemStack item) {
		List<Component> extra = getExtraData();
		if (extra.isEmpty()) return;
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		if (lore == null) lore = new ArrayList<Component>();
		lore.addAll(extra);
		meta.lore(lore);
		item.setItemMeta(meta);
	}

	// The session-specific extra data lines shown on this equipment's tooltip and hoverable.
	// Add future per-session display data here.
	private List<Component> getExtraData() {
		List<Component> lore = new ArrayList<Component>();
		if (hasDurability()) {
			lore.add(Component.text("Breaks after " + getDurability() + " fights", NamedTextColor.RED)
					.decoration(TextDecoration.ITALIC, State.FALSE));
		}
		return lore;
	}

	public static ArrayList<SessionEquipment> fromEquipment(ArrayList<? extends Equipment> equipment) {
		ArrayList<SessionEquipment> sessionEquipmentList = new ArrayList<>();
		for (Equipment eq : equipment) {
			sessionEquipmentList.add(new SessionEquipment(eq));
		}
		return sessionEquipmentList;
	}

	/**
	 * Reconstructs a SessionEquipment from its item form, preserving metadata when present.
	 * Falls back to the plain equipId/isUpgraded NBT tags for items without embedded metadata.
	 */
	public static SessionEquipment fromItem(ItemStack item) {
		if (item == null) return null;
		String serialized = NBT.get(item, nbt -> nbt.hasTag(NBT_KEY) ? nbt.getString(NBT_KEY) : null);
		if (serialized != null) {
			SessionEquipment se = deserialize(serialized);
			if (se != null) return se;
		}
		String equipId = NBT.get(item, nbt -> nbt.hasTag("equipId") ? nbt.getString("equipId") : null);
		if (equipId == null) return null;
		boolean isUpgraded = Boolean.TRUE.equals(NBT.get(item, nbt -> { return nbt.getBoolean("isUpgraded"); }));
		Equipment eq = Equipment.get(equipId, isUpgraded);
		if (eq == null) return null;
		return new SessionEquipment(eq);
	}

	public Component getDisplay() {
		Component display = equipment.getDisplay();
		if (hasDurability()) {
			display = display.append(Component.text(" ")).append(Component.text("(" + getDurability() + " uses)", NamedTextColor.RED));
		}
		return display;
	}

	// Builds this SessionEquipment's own hoverable so it always reflects session-specific extra
	// data (e.g. durability), rather than delegating to the base Equipment's cached hoverable.
	public Component getHoverable() {
		return getDisplay().decorate(TextDecoration.UNDERLINED)
				.hoverEvent(getItem().asHoverEvent())
				.clickEvent(ClickEvent.runCommand("/nr glossary " + equipment.getId()));
	}

	/**
	 * Creates an upgraded SessionEquipment preserving all metadata.
	 */
	public SessionEquipment upgrade() {
		Equipment upgraded = equipment.getUpgraded();
		if (upgraded == null) return this;
		return new SessionEquipment(upgraded, new HashMap<>(metadata));
	}

	// --- Typed Getters ---

	public boolean has(String key) {
		return metadata.containsKey(key);
	}

	public int getInt(String key) {
		Object val = metadata.get(key);
		if (val instanceof Number) return ((Number) val).intValue();
		return 0;
	}

	public int getInt(String key, int defaultValue) {
		Object val = metadata.get(key);
		if (val instanceof Number) return ((Number) val).intValue();
		return defaultValue;
	}

	public double getDouble(String key) {
		Object val = metadata.get(key);
		if (val instanceof Number) return ((Number) val).doubleValue();
		return 0;
	}

	public double getDouble(String key, double defaultValue) {
		Object val = metadata.get(key);
		if (val instanceof Number) return ((Number) val).doubleValue();
		return defaultValue;
	}

	public String getString(String key) {
		Object val = metadata.get(key);
		if (val instanceof String) return (String) val;
		return null;
	}

	public String getString(String key, String defaultValue) {
		Object val = metadata.get(key);
		if (val instanceof String) return (String) val;
		return defaultValue;
	}

	// --- Typed Setters ---

	public void setInt(String key, int value) {
		metadata.put(key, value);
	}

	public void setDouble(String key, double value) {
		metadata.put(key, value);
	}

	public void setString(String key, String value) {
		metadata.put(key, value);
	}

	public void remove(String key) {
		metadata.remove(key);
	}

	// --- Durability ---

	public boolean hasDurability() {
		return metadata.containsKey(DURABILITY_KEY);
	}

	public int getDurability() {
		return getInt(DURABILITY_KEY);
	}

	public void setDurability(int value) {
		setInt(DURABILITY_KEY, value);
	}

	// --- Serialization ---

	/**
	 * Serializes this SessionEquipment to a string.
	 * Format: "equipId+" or "equipId+|key1=i:5,key2=d:1.5,key3=s:hello"
	 */
	public String serialize() {
		String base = equipment.serialize();
		if (metadata.isEmpty()) return base;
		StringBuilder sb = new StringBuilder(base);
		sb.append('|');
		boolean first = true;
		for (Map.Entry<String, Object> entry : metadata.entrySet()) {
			if (!first) sb.append('&');
			first = false;
			sb.append(entry.getKey()).append('=');
			Object val = entry.getValue();
			if (val instanceof Integer) {
				sb.append("i:").append(val);
			} else if (val instanceof Double) {
				sb.append("d:").append(val);
			} else {
				sb.append("s:").append(val);
			}
		}
		return sb.toString();
	}

	/**
	 * Deserializes a single SessionEquipment from a string.
	 * Supports both legacy format ("equipId+") and extended format ("equipId+|metadata").
	 */
	public static SessionEquipment deserialize(String str) {
		if (str == null || str.isBlank()) return null;

		int pipeIdx = str.indexOf('|');
		String equipPart;
		String metaPart;
		if (pipeIdx >= 0) {
			equipPart = str.substring(0, pipeIdx);
			metaPart = str.substring(pipeIdx + 1);
		} else {
			equipPart = str;
			metaPart = null;
		}

		Equipment eq = Equipment.deserialize(equipPart);
		if (eq == null) return null;

		SessionEquipment se = new SessionEquipment(eq);
		if (metaPart != null && !metaPart.isEmpty()) {
			parseMetadata(metaPart, se.metadata);
		}
		return se;
	}

	private static void parseMetadata(String metaStr, HashMap<String, Object> target) {
		String[] entries = metaStr.split("&");
		for (String entry : entries) {
			int eqIdx = entry.indexOf('=');
			if (eqIdx <= 0) continue;
			String key = entry.substring(0, eqIdx);
			String valStr = entry.substring(eqIdx + 1);
			if (valStr.length() < 2 || valStr.charAt(1) != ':') continue;
			char type = valStr.charAt(0);
			String raw = valStr.substring(2);
			try {
				switch (type) {
				case 'i':
					target.put(key, Integer.parseInt(raw));
					break;
				case 'd':
					target.put(key, Double.parseDouble(raw));
					break;
				case 's':
					target.put(key, raw);
					break;
				default:
					target.put(key, raw);
					break;
				}
			} catch (NumberFormatException e) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to parse metadata entry: " + entry);
			}
		}
	}

	// --- Utility ---

	public static ArrayList<SessionEquipment> wrap(ArrayList<? extends Equipment> equips) {
		ArrayList<SessionEquipment> result = new ArrayList<>(equips.size());
		for (Equipment eq : equips) {
			result.add(new SessionEquipment(eq));
		}
		return result;
	}

	@Override
	public String toString() {
		return "SessionEquipment{" +
				"equipment=" + equipment +
				", metadata=" + metadata +
				'}';
	}

	// --- Array Serialization ---

	public static String serialize(SessionEquipment[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				sb.append(' ');
			} else {
				sb.append(arr[i].serialize());
			}
			sb.append(';');
		}
		return sb.toString();
	}

	public static SessionEquipment[] deserializeAsArray(String str) {
		String[] separated = str.split(";");
		SessionEquipment[] arr = new SessionEquipment[separated.length];
		try {
			for (int i = 0; i < separated.length; i++) {
				if (separated[i].isBlank()) continue;
				arr[i] = SessionEquipment.deserialize(separated[i]);
			}
		} catch (Exception e) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to deserialize session equipment as array: " + str);
			e.printStackTrace();
		}
		return arr;
	}

	public static String serialize(ArrayList<SessionEquipment> arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.size(); i++) {
			sb.append(arr.get(i).serialize());
			sb.append(';');
		}
		return sb.toString();
	}

	public static ArrayList<SessionEquipment> deserializeAsArrayList(String str) {
		if (str == null || str.isBlank()) return new ArrayList<>();
		String[] separated = str.split(";");
		ArrayList<SessionEquipment> arr = new ArrayList<>(separated.length);
		for (String s : separated) {
			if (s.isBlank()) continue;
			SessionEquipment se = SessionEquipment.deserialize(s);
			if (se != null) arr.add(se);
		}
		return arr;
	}
}
