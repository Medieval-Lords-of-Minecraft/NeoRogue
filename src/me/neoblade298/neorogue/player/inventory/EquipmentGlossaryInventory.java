package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class EquipmentGlossaryInventory extends GlossaryInventory {
	private static final String ARROW_DOWN2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM3ODYyY2RjMTU5OTk4ZWQ2YjZmZGNjYWFhNDY3NTg2N2Q0NDg0ZGI1MTJhODRjMzY3ZmFiZjRjYWY2MCJ9fX0=";
	Equipment eq;
	public EquipmentGlossaryInventory(Player viewer, Equipment equip, CoreInventory prev) {
		super(viewer, calculateSize(equip), equip.getUnupgraded().getDisplay(), prev);
		this.eq = equip.getUnupgraded();
		
		ItemStack[] contents = inv.getContents();
		contents[BASIC] = eq.getUnupgraded().getItem();
		contents[UPGRADED] = eq.canUpgrade() ? eq.getUpgraded().getItem() : null;
		contents[TAGS] = createTagsItem(eq);
		if (!eq.getReforgeParents().isEmpty()) contents[PARENTS] = createParentsItem(eq);

		// Custom glossary tags, basically just shoehorned in for now, nowhere to put them, only 1 slot for them
		for (GlossaryIcon icon : eq.getTags()) {
			if (icon instanceof CustomGlossaryIcon) {
				contents[CUSTOM] = icon.getIcon();
				break;
			}
		}
		
		// Reforge options
		if (!eq.getReforgeOptions().isEmpty()) {
			for (int i = 9; i < 18; i++) {
				contents[i] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text("Reforge Options:"));
			}

			int col = 0;
			for (Entry<Equipment, Equipment[]> ent : eq.getReforgeOptions().entrySet()) {
				int row = 2;
				contents[(row++ * 9) + col] = ent.getKey().getItem();
				contents[(row++ * 9) + col] = CoreInventory.createButton(ARROW_DOWN2, 
					Component.text("Combine ").append(ent.getKey().getDisplay()).append(Component.text(" with ").append(eq.getDisplay()).append(Component.text(":"))));
				for (Equipment option : ent.getValue()) {
					if (row > 5) {
						row = 4;
						col++;
					}
					contents[(row++ * 9) + col] = option.getItem();
				}
				col++;
			}
		}
		inv.setContents(contents);
	}

	private ItemStack createTagsItem(Equipment eq) {
		ItemStack item = CoreInventory.createButton(new ItemStack(Material.PAPER), Component.text("Tags", NamedTextColor.WHITE));
		ArrayList<Component> lore = new ArrayList<Component>();
		Iterator<GlossaryIcon> iter = eq.getTags().iterator();
		while (iter.hasNext()) {
			GlossaryIcon icon = iter.next();
			if (icon instanceof GlossaryTag) {
				GlossaryTag tag = (GlossaryTag) icon;
				lore.add(tag.getTag());
				lore.addAll(tag.getLore());
				lore.add(Component.text("-----", NamedTextColor.DARK_GRAY));
			}
		}
		if (!lore.isEmpty()) {
			lore.removeLast();
		}
		else {
			lore.add(Component.text("None", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
		}
		item.lore(lore);
		return item;
	}

	private ItemStack createParentsItem(Equipment eq) {
		ItemStack item = CoreInventory.createButton(new ItemStack(Material.ANVIL), Component.text("Reforged from:", NamedTextColor.WHITE));
		ArrayList<Component> lore = new ArrayList<Component>();
		Iterator<Equipment> iter = eq.getReforgeParents().iterator();
		while (iter.hasNext()) {
			lore.add(Component.text("- ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE)
				.append(iter.next().getDisplay()).append(Component.text(" and ").append(iter.next().getDisplay())));
		}
		item.lore(lore);
		return item;
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getSlot() == PARENTS && !eq.getReforgeParents().isEmpty()) {
			openOther = false;
			new BukkitRunnable() {
				public void run() {
					new EquipmentParentsGlossaryInventory(p, eq, prev);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		if (!e.isRightClick()) return;
		if (e.getCurrentItem() == null) return;
		NBTItem nbti = new NBTItem(e.getCurrentItem());
		if (!nbti.getKeys().contains("equipId")) return;
		openOther = false;
		new BukkitRunnable() {
			public void run() {
				new EquipmentGlossaryInventory(p, Equipment.get(nbti.getString("equipId"), false), prev);
			}
		}.runTask(NeoRogue.inst());
	}
}
