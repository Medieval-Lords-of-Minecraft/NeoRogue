package me.neoblade298.neorogue;

public class DescUtil {
	public static String yellow(String txt) {
		return "<yellow>" + txt + "</yellow>";
	}
	public static String yellow(int txt) {
		return "<yellow>" + txt + "</yellow>";
	}
	public static String yellow(double txt) {
		return "<yellow>" + txt + "</yellow>";
	}
	public static String white(String txt) {
		return "<white>" + txt + "</white>";
	}
	public static String white(int txt) {
		return "<white>" + txt + "</white>";
	}
	public static String white(double txt) {
		return "<white>" + txt + "</white>";
	}

	public static String potion(String txt, int potency, int seconds) {
		return txt + " " + white(potency + 1) + " [" + white(seconds + "s") + "]";
	}

	public static String potionUp(String txt, int potency, int seconds) {
		return txt + " " + white(potency + 1) + " [" + yellow(seconds + "s") + "]";
	}

	public static String duration(int seconds, boolean upgrade) {
		return "[" + (upgrade ? yellow(seconds + "s") : white(seconds + "s")) + "]";
	}
}
