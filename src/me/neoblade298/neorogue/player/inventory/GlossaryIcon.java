package me.neoblade298.neorogue.player.inventory;

import java.util.Comparator;

import org.bukkit.inventory.ItemStack;

public interface GlossaryIcon {
	public String getId();
	public ItemStack getIcon();
	public static final Comparator<GlossaryIcon> comparator = new Comparator<GlossaryIcon>() {
		@Override
		public int compare(GlossaryIcon o1, GlossaryIcon o2) {
			return o1.getId().compareTo(o2.getId());
		}
	};
}
