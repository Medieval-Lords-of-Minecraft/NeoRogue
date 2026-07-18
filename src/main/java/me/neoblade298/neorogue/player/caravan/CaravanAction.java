package me.neoblade298.neorogue.player.caravan;

import org.bukkit.Bukkit;

import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// A single effect applied when a caravan upgrade is purchased. Parsed from a string of the form
// "<id> [amount|arg]" in caravan.yml, e.g. "cargo_amount 1000" or "cargo_sellables ores".
public class CaravanAction {
	public enum Type {
		CARGO_ACCESS, CARGO_AMOUNT, CARGO_SLOTS, CARGO_INSURANCE, CARGO_BASE_REWARD, CARGO_SELLABLES, CARGO_SELL_MULTIPLIER,
		FLEET_SIZE, FLEET_CAPACITY, FLEET_SLOTS
	}

	private final Type type;
	private final int amount;
	private final String arg;

	private CaravanAction(Type type, int amount, String arg) {
		this.type = type;
		this.amount = amount;
		this.arg = arg;
	}

	// Returns null (and logs) if the action string can't be parsed.
	public static CaravanAction parse(String raw) {
		if (raw == null || raw.isBlank()) return null;
		String[] parts = raw.trim().split("\\s+", 2);
		Type type;
		try {
			type = Type.valueOf(parts[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().warning("[NeoRogue] Unknown caravan action id '" + parts[0] + "'");
			return null;
		}
		String param = parts.length > 1 ? parts[1].trim() : null;
		int amount = 0;
		// Every action except cargo_sellables takes a numeric amount; sellables takes a package id.
		if (param != null && type != Type.CARGO_SELLABLES) {
			try {
				amount = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				Bukkit.getLogger().warning("[NeoRogue] Caravan action '" + parts[0]
						+ "' expected a number but got '" + param + "'");
			}
		}
		return new CaravanAction(type, amount, param);
	}

	public void apply(PlayerData pd) {
		switch (type) {
		case CARGO_ACCESS:
			pd.addFlag(PlayerData.FLAG_CARGO_ACCESS);
			break;
		case CARGO_AMOUNT:
			pd.addCargoCapacity(amount);
			break;
		case CARGO_SLOTS:
			pd.addCargoSlots(amount);
			break;
		case CARGO_INSURANCE:
			pd.addFlag(PlayerData.FLAG_CARGO_INSURANCE);
			break;
		case CARGO_BASE_REWARD:
			pd.addCargoBaseReward(amount);
			break;
		case CARGO_SELLABLES:
			if (arg != null) pd.addSellablePackage(arg);
			break;
		case CARGO_SELL_MULTIPLIER:
			pd.addSellMultiplier(amount);
			break;
		case FLEET_SIZE:
			pd.addFleetSize(amount);
			break;
		case FLEET_CAPACITY:
			pd.addFleetCapacity(amount);
			break;
		case FLEET_SLOTS:
			pd.addFleetSlots(amount);
			break;
		}
	}

	// A short human-readable summary of the effect, shown in the upgrade tooltip.
	public Component describe() {
		switch (type) {
		case CARGO_ACCESS:
			return Component.text("Unlocks cargo access", NamedTextColor.GRAY);
		case CARGO_AMOUNT:
			return Component.text("+" + amount + " cargo capacity", NamedTextColor.GRAY);
		case CARGO_SLOTS:
			return Component.text("+" + amount + " cargo slots", NamedTextColor.GRAY);
		case CARGO_INSURANCE:
			return Component.text("Unlocks cargo insurance", NamedTextColor.GRAY);
		case CARGO_BASE_REWARD:
			return Component.text("+" + amount + " region completion reward", NamedTextColor.GRAY);
		case CARGO_SELLABLES:
			return Component.text("Grants the " + arg + " sellable package", NamedTextColor.GRAY);
		case CARGO_SELL_MULTIPLIER:
			return Component.text("+" + amount + "% cargo sell value", NamedTextColor.GRAY);
		case FLEET_SIZE:
			return Component.text("+" + amount + " fleet hold" + (amount == 1 ? "" : "s"), NamedTextColor.GRAY);
		case FLEET_CAPACITY:
			return Component.text("+" + amount + " fleet hold capacity", NamedTextColor.GRAY);
		case FLEET_SLOTS:
			return Component.text("+" + amount + " fleet hold slots", NamedTextColor.GRAY);
		default:
			return Component.empty();
		}
	}
}
