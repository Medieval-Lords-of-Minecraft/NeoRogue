package me.neoblade298.neorogue.player.boost;


import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.bukkit.Bukkit;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.io.Section;
import me.neoblade298.neorogue.NeoRogue;

// Configurable exp boost type loaded from expboosts.yml. Enum-style lookup methods
// are retained so persisted ids and existing command integrations remain compatible.
public class ExpBoostType {
	private static final LinkedHashMap<String, ExpBoostType> types = new LinkedHashMap<String, ExpBoostType>();

	private final String id;
	private String displayName;
	private double multiplier;
	private BoostDurationType durationType;
	private String permission, receiveMessage, extendMessage;

	private ExpBoostType(String id, Section sec) {
		this.id = normalizeId(id);
		this.displayName = sec.getString("display-name", this.id);
		this.multiplier = sec.getDouble("multiplier", 0.0);
		if (multiplier < 0.0) throw new IllegalArgumentException("multiplier cannot be negative");
		String configuredDurationType = sec.getString("duration-type");
		this.durationType = configuredDurationType == null || configuredDurationType.isBlank() ? null
				: BoostDurationType.valueOf(configuredDurationType.toUpperCase(Locale.ROOT));
		String configuredPermission = sec.getString("permission");
		this.permission = configuredPermission == null || configuredPermission.isBlank()
				? null : configuredPermission.trim();
		this.receiveMessage = sec.getString("receive-message");
		this.extendMessage = sec.getString("extend-message");
		if (durationType == null && permission == null) {
			throw new IllegalArgumentException("duration-type or permission is required");
		}
	}

	public static synchronized void reload() {
		LinkedHashMap<String, ExpBoostType> loaded = new LinkedHashMap<String, ExpBoostType>();
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "expboosts.yml"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					ExpBoostType type = new ExpBoostType(key, yml.getSection(key));
					loaded.put(type.id, type);
				} catch (Exception ex) {
					ex.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load exp boost type " + key
							+ " in file " + file.getName());
				}
			}
		});

		LinkedHashMap<String, ExpBoostType> reloaded = new LinkedHashMap<String, ExpBoostType>();
		for (ExpBoostType configured : loaded.values()) {
			ExpBoostType existing = types.get(configured.id);
			if (existing != null) {
				existing.copyConfiguration(configured);
				reloaded.put(existing.id, existing);
			} else {
				reloaded.put(configured.id, configured);
			}
		}
		types.clear();
		types.putAll(reloaded);
	}

	private void copyConfiguration(ExpBoostType configured) {
		displayName = configured.displayName;
		multiplier = configured.multiplier;
		durationType = configured.durationType;
		permission = configured.permission;
		receiveMessage = configured.receiveMessage;
		extendMessage = configured.extendMessage;
	}

	public static ExpBoostType get(String id) {
		return id == null ? null : types.get(normalizeId(id));
	}

	public static Collection<ExpBoostType> getTypes() {
		return Collections.unmodifiableCollection(types.values());
	}

	public static ExpBoostType[] values() {
		return types.values().toArray(new ExpBoostType[0]);
	}

	public static ExpBoostType valueOf(String id) {
		ExpBoostType type = get(id);
		if (type == null) throw new IllegalArgumentException("Unknown exp boost type: " + id);
		return type;
	}

	private static String normalizeId(String id) {
		return id.trim().toUpperCase(Locale.ROOT);
	}

	public String getId() {
		return id;
	}

	public String name() {
		return id;
	}

	public boolean isRegistered() {
		return types.get(id) == this;
	}

	public String getDisplayName() {
		return displayName;
	}

	// Additive multiplier, e.g. 0.30 means +30% exp.
	public double getMultiplier() {
		return multiplier;
	}

	public BoostDurationType getDurationType() {
		return durationType;
	}

	public boolean isGrantable() {
		return durationType != null;
	}

	public String getPermission() {
		return permission;
	}

	public String getReceiveMessage() {
		return receiveMessage;
	}

	public String getExtendMessage() {
		return extendMessage;
	}

	public String formatGrantMessage(boolean extended, String playerName, long duration, long remaining) {
		String message = extended ? extendMessage : receiveMessage;
		if (message == null || message.isBlank()) return null;
		return message.replace("%boost%", displayName)
				.replace("%duration%", formatDuration(duration))
				.replace("%remaining%", formatDuration(remaining))
				.replace("%player%", playerName);
	}

	private String formatDuration(long duration) {
		if (durationType == BoostDurationType.RUNS) {
			return duration + (duration == 1 ? " run" : " runs");
		}
		if (duration % 86400 == 0) {
			long days = duration / 86400;
			return days + (days == 1 ? " day" : " days");
		}
		if (duration % 3600 == 0) {
			long hours = duration / 3600;
			return hours + (hours == 1 ? " hour" : " hours");
		}
		if (duration % 60 == 0) {
			long minutes = duration / 60;
			return minutes + (minutes == 1 ? " minute" : " minutes");
		}
		return duration + (duration == 1 ? " second" : " seconds");
	}
}
