package me.neoblade298.neorogue;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

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

	public static String charge(Equipment eq, int potency, int seconds) {
		eq.addTags(GlossaryTag.CHARGE);
		return GlossaryTag.CHARGE.tag + " <gray>[<white>Slowness " + (potency + 1) + ", " + seconds + "s</white>]</gray>";
	}

	public static String channel(Equipment eq, int seconds) {
		eq.addTags(GlossaryTag.CHANNEL);
		return GlossaryTag.CHANNEL.tag + " for <gray>[<white>" + seconds + "s</white>]</gray>";
	}

	public static String duration(int seconds, boolean upgrade) {
		return "[" + (upgrade ? yellow(seconds + "s") : white(seconds + "s")) + "]";
	}
}
