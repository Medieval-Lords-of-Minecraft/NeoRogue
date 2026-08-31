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

// Configurable currency boost type loaded from currencyboosts.yml. Currency boost ids
// and storage are intentionally independent from exp boosts.
public class CurrencyBoostType {
	private static final LinkedHashMap<String, CurrencyBoostType> types = new LinkedHashMap<String, CurrencyBoostType>();

	private final String id;
	private String displayName;
	private double multiplier;
	private BoostDurationType durationType;
	private String permission, receiveMessage, extendMessage;

	private CurrencyBoostType(String id, Section sec) {
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
		LinkedHashMap<String, CurrencyBoostType> loaded = new LinkedHashMap<String, CurrencyBoostType>();
		NeoCore.loadFiles(new File(NeoRogue.inst().getDataFolder(), "currencyboosts.yml"), (yml, file) -> {
			for (String key : yml.getKeys()) {
				try {
					CurrencyBoostType type = new CurrencyBoostType(key, yml.getSection(key));
					loaded.put(type.id, type);
				} catch (Exception ex) {
					ex.printStackTrace();
					Bukkit.getLogger().warning("[NeoRogue] Failed to load currency boost type " + key
							+ " in file " + file.getName());
				}
			}
		});

		LinkedHashMap<String, CurrencyBoostType> reloaded = new LinkedHashMap<String, CurrencyBoostType>();
		for (CurrencyBoostType configured : loaded.values()) {
			CurrencyBoostType existing = types.get(configured.id);
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

	private void copyConfiguration(CurrencyBoostType configured) {
		displayName = configured.displayName;
		multiplier = configured.multiplier;
		durationType = configured.durationType;
		permission = configured.permission;
		receiveMessage = configured.receiveMessage;
		extendMessage = configured.extendMessage;
	}

	public static CurrencyBoostType get(String id) {
		return id == null ? null : types.get(normalizeId(id));
	}

	public static Collection<CurrencyBoostType> getTypes() {
		return Collections.unmodifiableCollection(types.values());
	}

	public static CurrencyBoostType[] values() {
		return types.values().toArray(new CurrencyBoostType[0]);
	}

	public static CurrencyBoostType valueOf(String id) {
		CurrencyBoostType type = get(id);
		if (type == null) throw new IllegalArgumentException("Unknown currency boost type: " + id);
		return type;
	}

	private static String normalizeId(String id) {
		return id.trim().toUpperCase(Locale.ROOT);
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

	// Additive bonus, e.g. 0.30 means +30% run currency.
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

	public String formatGrantMessage(boolean extended, String playerName, long duration, long remaining) {
		String message = extended ? extendMessage : receiveMessage;
		if (message == null || message.isBlank()) return null;
		return message.replace("%boost%", displayName)
				.replace("%duration%", formatDuration(duration))
				.replace("%remaining%", formatRemaining(remaining))
				.replace("%player%", playerName);
	}

	private String formatRemaining(long remaining) {
		if (durationType == BoostDurationType.TIME) {
			long totalMinutes = Math.max(1, (remaining + 59) / 60);
			long days = totalMinutes / 1440;
			long hours = totalMinutes % 1440 / 60;
			long minutes = totalMinutes % 60;
			StringBuilder formatted = new StringBuilder();
			appendDurationPart(formatted, days, "day");
			appendDurationPart(formatted, hours, "hour");
			appendDurationPart(formatted, minutes, "minute");
			return formatted.toString();
		}
		return formatDuration(remaining);
	}

	private void appendDurationPart(StringBuilder formatted, long amount, String unit) {
		if (amount == 0) return;
		if (formatted.length() > 0) formatted.append(' ');
		formatted.append(amount).append(' ').append(unit);
		if (amount != 1) formatted.append('s');
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
