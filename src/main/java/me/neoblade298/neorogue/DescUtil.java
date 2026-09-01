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
	// its upgraded counterpart.
	public static String val(int txt) {
		return "" + VAL_START + txt + VAL_END;
	}
	public static String val(double txt) {
		return "" + VAL_START + txt + VAL_END;
	}
	public static String val(String txt) {
		return "" + VAL_START + txt + VAL_END;
	}

	// Resolves value tokens used outside equipment lore, where no upgraded counterpart is available.
	public static String resolveValues(String txt) {
		if (txt == null || txt.indexOf(VAL_START) < 0) return txt;
		StringBuilder resolved = new StringBuilder(txt.length());
		int index = 0;
		while (index < txt.length()) {
			int start = txt.indexOf(VAL_START, index);
			if (start < 0) {
				resolved.append(txt, index, txt.length());
				break;
			}
			resolved.append(txt, index, start);
			int end = txt.indexOf(VAL_END, start + 1);
			if (end < 0) {
				resolved.append(txt, start, txt.length());
				break;
			}
			resolved.append("<white>").append(txt, start + 1, end).append("</white>");
			index = end + 1;
		}
		return resolved.toString();
	}

	public static String potion(String txt, int potency, int seconds) {
		return txt + " " + val(potency + 1) + " [" + val(seconds + "s") + "]";
	}

	public static String potion(String txt, int potency, int seconds, boolean upgradePotency, boolean upgradeDuration) {
		return txt + " " + val(potency + 1) + " [" + val(seconds + "s") + "]";
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
		return "[" + val(seconds + "s") + "]";
	}

	// Auto-colored duration: the value is yellow/white based on whether it changes on upgrade.
	public static String duration(int seconds) {
		return "[" + val(seconds + "s") + "]";
	}
}
