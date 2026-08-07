package me.neoblade298.neorogue.player.boost;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BoostTimeFormat {
	private static final Pattern PART_PATTERN = Pattern.compile("(\\d+)([smhdw])");

	private BoostTimeFormat() {
	}

	public static long parseSeconds(String input) {
		String value = input.toLowerCase(Locale.ROOT);
		if (value.matches("\\d+")) return Long.parseLong(value);

		Matcher matcher = PART_PATTERN.matcher(value);
		long seconds = 0;
		int parsedThrough = 0;
		while (matcher.find()) {
			if (matcher.start() != parsedThrough) throw new NumberFormatException("Invalid duration");
			long amount = Long.parseLong(matcher.group(1));
			long multiplier;
			switch (matcher.group(2)) {
			case "s":
				multiplier = 1;
				break;
			case "m":
				multiplier = 60;
				break;
			case "h":
				multiplier = 3600;
				break;
			case "d":
				multiplier = 86400;
				break;
			case "w":
				multiplier = 604800;
				break;
			default:
				throw new NumberFormatException("Invalid duration unit");
			}
			seconds = Math.addExact(seconds, Math.multiplyExact(amount, multiplier));
			parsedThrough = matcher.end();
		}
		if (parsedThrough != value.length()) throw new NumberFormatException("Invalid duration");
		return seconds;
	}

	public static String format(long seconds) {
		long weeks = seconds / 604800;
		seconds %= 604800;
		long days = seconds / 86400;
		seconds %= 86400;
		long hours = seconds / 3600;
		seconds %= 3600;
		long minutes = seconds / 60;
		seconds %= 60;

		StringBuilder formatted = new StringBuilder();
		append(formatted, weeks, "w");
		append(formatted, days, "d");
		append(formatted, hours, "h");
		append(formatted, minutes, "m");
		append(formatted, seconds, "s");
		return formatted.length() == 0 ? "0s" : formatted.toString();
	}

	private static void append(StringBuilder formatted, long amount, String unit) {
		if (amount > 0) formatted.append(amount).append(unit);
	}
}