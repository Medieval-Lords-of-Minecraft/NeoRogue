package me.neoblade298.neorogue.player.inventory;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class EquipmentParentsGlossaryInventory extends GlossaryInventory {
	private static final int SLOT1 = 2, SLOT2 = 6, ICON = 4;
	private static final int[] FILLER = new int[] {0, 1, 7, 8};
	private static final String PLUS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcxZDg5NzljMTg3OGEwNTk4N2E3ZmFmMjFiNTZkMWI3NDRmOWQwNjhjNzRjZmZjZGUxZWExZWRhZDU4NTIifX19";
	public EquipmentParentsGlossaryInventory(Player viewer, Equipment eq, CoreInventory prev) {
		super(viewer, ((eq.getReforgeParents().size() / 2) + 1) * 9, eq.getUnupgraded().getDisplay().append(Component.text(" Parents", NamedTextColor.DARK_GRAY)), prev);
		eq = eq.getUnupgraded();
		eq.getReforgeParents();
		
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < 9; i++) {
			if (i == 4) {
				contents[i] = eq.getItem();
			}
			else {
				contents[i] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
			}
		}
		
		Iterator<Equipment> iter = eq.getReforgeParents().iterator();
		int row = 1;
		while (iter.hasNext()) {
			Equipment eq1 = iter.next();
			Equipment eq2 = iter.next();
			contents[(row * 9) + SLOT1] = eq1.getItem();
			contents[(row * 9) + SLOT2] = eq2.getItem();
			contents[(row * 9) + ICON] = CoreInventory.createButton(PLUS_HEAD, Component.text("Combine:", NamedTextColor.GRAY),
				(TextComponent) eq1.getDisplay(), (TextComponent) eq2.getDisplay());
			for (int i : FILLER) {
				contents[(row * 9) + i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, 
					Component.text(" "));
			}
			row++;
		}
		inv.setContents(contents);
	}
}
