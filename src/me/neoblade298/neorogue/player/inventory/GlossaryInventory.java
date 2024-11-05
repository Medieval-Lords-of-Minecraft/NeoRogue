package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.fight.Mob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GlossaryInventory extends CoreInventory {
	private static final String ARROW_DOWN2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM3ODYyY2RjMTU5OTk4ZWQ2YjZmZGNjYWFhNDY3NTg2N2Q0NDg0ZGI1MTJhODRjMzY3ZmFiZjRjYWY2MCJ9fX0=";
	private CoreInventory prev;
	private boolean openOther = true;
	
	private static final int BASIC = 4, UPGRADED = 5, TAGS = 3, PARENTS = 8, CUSTOM = 0;
	// Mob glossary book
	public GlossaryInventory(Player viewer, Mob mob, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, calculateSize(mob.getTags().size()),
				Component.text("Glossary: ").append(LegacyComponentSerializer.legacyAmpersand()
						.deserialize(NeoRogue.mythicApi.getMythicMob(mob.getId()).getDisplayName().get()))));
		this.prev = prev;
		Sounds.turnPage.play(p, p);
		
		// Glossary tags
		ItemStack[] contents = inv.getContents();
		Iterator<GlossaryIcon> iter = mob.getTags().iterator();
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 9; col++) {
				if (!iter.hasNext()) break;
				contents[(row * 9) + col] = iter.next().getIcon();
			}
		}
		inv.setContents(contents);
	}
	// Chance glossary book (infrequently used, LostRelicChance for example)
	public GlossaryInventory(Player viewer, ChanceChoice choice, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, calculateSize(choice.getTags().size()),
				Component.text("Glossary: ", NamedTextColor.GOLD).append(choice.getItemWithoutConditions().displayName())));
		this.prev = prev;
		Sounds.turnPage.play(p, p);
		
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
	// Regular glossary book
	public GlossaryInventory(Player viewer, Equipment eq, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, calculateSize(eq), Component.text("Glossary: ").append(eq.getUnupgraded().getDisplay())));
		this.prev = prev;
		eq = eq.getUnupgraded();
		Sounds.turnPage.play(p, p);
		
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
	
	private static int calculateSize(int numTags) {
		return numTags + (9 - numTags % 9);
	}
	
	private static int calculateSize(Equipment eq) {
		return eq.getReforgeOptions().isEmpty() ? 9 : 54;
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (!e.isRightClick()) return;
		if (e.getCurrentItem() == null) return;
		NBTItem nbti = new NBTItem(e.getCurrentItem());
		if (!nbti.getKeys().contains("equipId")) return;
		openOther = false;
		new BukkitRunnable() {
			public void run() {
				new GlossaryInventory(p, Equipment.get(nbti.getString("equipId"), false), prev);
			}
		}.runTask(NeoRogue.inst());
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// Don't open the previous inventory if we're opening another glossary page
		if (openOther) {
			new BukkitRunnable() {
				public void run() {
					if (prev != null) prev.openInventory();
				}
			}.runTask(NeoRogue.inst());
		}
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
