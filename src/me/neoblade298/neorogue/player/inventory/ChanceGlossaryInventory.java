package me.neoblade298.neorogue.player.inventory;

import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.chance.ChanceChoice;

public class ChanceGlossaryInventory extends GlossaryInventory {
	// Chance glossary book (infrequently used, LostRelicChance for example)
	public ChanceGlossaryInventory(Player viewer, ChanceChoice choice, CoreInventory prev) {
		super(viewer, calculateSize(choice.getTags().size()),
				choice.getItemWithoutConditions().displayName(), prev);
		
		// Glossary tags
		ItemStack[] contents = inv.getContents();
		Iterator<GlossaryIcon> iter = choice.getTags().iterator();
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 9; col++) {
				if (!iter.hasNext()) break;
				contents[(row * 9) + col] = iter.next().getIcon();
			}
		}
		inv.setContents(contents);
	}
}
