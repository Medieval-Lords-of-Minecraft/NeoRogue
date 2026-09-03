package me.neoblade298.neorogue.integrations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.NeoRogue;

public final class TownyFlightIntegration {
	private static final String PLUGIN_NAME = "TownyFlight";
	private static Method getApiMethod, setForceAllowFlightMethod;
	private static boolean initializationFailed;

	private TownyFlightIntegration() {
	}

	public static void enable(Player player) {
		setForceAllowFlight(player, true);
	}

	public static void disable(Player player) {
		setForceAllowFlight(player, false);
	}

	private static void setForceAllowFlight(Player player, boolean force) {
		if (player == null || initializationFailed || !Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) return;
		try {
			if (getApiMethod == null) {
				Class<?> pluginClass = Class.forName("com.gmail.llmdlio.townyflight.TownyFlight");
				getApiMethod = pluginClass.getMethod("getAPI");
				Class<?> apiClass = Class.forName("com.gmail.llmdlio.townyflight.TownyFlightAPI");
				setForceAllowFlightMethod = apiClass.getMethod("setForceAllowFlight", Player.class, boolean.class);
			}
			setForceAllowFlightMethod.invoke(getApiMethod.invoke(null), player, force);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
			initializationFailed = true;
			NeoRogue.inst().getLogger().warning("TownyFlight integration could not be initialized: " + ex.getMessage());
		}
	}
}