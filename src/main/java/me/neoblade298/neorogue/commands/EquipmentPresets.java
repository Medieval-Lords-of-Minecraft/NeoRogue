package me.neoblade298.neorogue.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import me.neoblade298.neorogue.NeoRogue;

public class EquipmentPresets {
	private static final HashMap<String, String> presets = new HashMap<>();

	public static void reload() {
		presets.clear();
		File file = new File(NeoRogue.inst().getDataFolder(), "equipment-presets.yml");
		if (!file.exists()) return;
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		for (String key : yml.getKeys(false)) {
			String value = yml.getString(key);
			if (value != null) presets.put(key, value);
		}
	}

	public static String get(String name) {
		return presets.get(name);
	}

	public static ArrayList<String> getNames() {
		return new ArrayList<>(presets.keySet());
	}
}
