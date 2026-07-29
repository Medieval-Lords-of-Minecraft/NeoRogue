package me.neoblade298.neorogue.player.inventory;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import net.kyori.adventure.text.Component;

public class ChanceGlossaryInventory extends GlossaryInventory {
	// Run on close instead of reopening a prev inventory, used when the glossary was opened from a dialog.
	private Runnable reopen;

	// Chance glossary book (infrequently used, LostRelicChance for example)
	public ChanceGlossaryInventory(Player viewer, ChanceChoice choice, CoreInventory prev) {
		super(viewer, calculateSize(choice.getTags().size()),
				choice.getItemWithoutConditions().displayName(), prev);
		fillIcons(choice.getTags());
	}

	// Combined glossary for every tag across a dialog's choices. reopen re-shows the dialog on close.
	public ChanceGlossaryInventory(Player viewer, Collection<GlossaryIcon> tags, Component title, Runnable reopen) {
		super(viewer, Math.min(54, calculateSize(tags.size())), title, null);
		this.reopen = reopen;
		fillIcons(tags);
	}

	private void fillIcons(Collection<GlossaryIcon> tags) {
		ItemStack[] contents = inv.getContents();
		Iterator<GlossaryIcon> iter = tags.iterator();
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 9; col++) {
				if (!iter.hasNext()) break;
				contents[(row * 9) + col] = iter.next().getIcon();
			}
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (reopen == null) {
			super.handleInventoryClose(e);
			return;
		}
		// openOther is false while navigating to a sub-glossary page; only reopen the dialog on a real close.
		if (openOther) {
			new BukkitRunnable() {
				public void run() {
					reopen.run();
				}
			}.runTask(NeoRogue.inst());
		}
	}
}
