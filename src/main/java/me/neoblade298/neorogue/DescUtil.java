package me.neoblade298.neorogue;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class DescUtil {
	// Sentinel characters (Unicode private-use area) that wrap an auto-colored value emitted by val(...).
	// Equipment.resolveUpgradeColors(...) replaces each wrapped value with <yellow> if it changes on
	// upgrade or <white> if it stays the same, then strips the sentinels before the tooltip is rendered.
	public static final char VAL_START = '\uE000';
	public static final char VAL_END = '\uE001';

	// Emits a value whose color (yellow/white) is decided automatically by diffing the base item against
	// its upgraded counterpart. Use this instead of manually choosing yellow(...) / white(...).
	public static String val(int txt) {
		return "" + VAL_START + txt + VAL_END;
	}
	public static String val(double txt) {
		return "" + VAL_START + txt + VAL_END;
	}
	public static String val(String txt) {
		return "" + VAL_START + txt + VAL_END;
	}

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

	public static String potion(String txt, int potency, int seconds, boolean upgradePotency, boolean upgradeDuration) {
		return txt + " " + (upgradePotency ? yellow(potency + 1) : white(potency + 1)) + " ["
				+ (upgradeDuration ? yellow(seconds + "s") : white(seconds + "s")) + "]";
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

	// Auto-colored duration: the value is yellow/white based on whether it changes on upgrade.
	public static String duration(int seconds) {
		return "[" + val(seconds + "s") + "]";
	}
}
