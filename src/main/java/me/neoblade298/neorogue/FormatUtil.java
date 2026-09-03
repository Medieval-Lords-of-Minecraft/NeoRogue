package me.neoblade298.neorogue;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {
	public static String whole(long value) {
		return NumberFormat.getIntegerInstance(Locale.US).format(value);
	}

	public static String whole(double value) {
		return whole(Math.round(value));
	}
}