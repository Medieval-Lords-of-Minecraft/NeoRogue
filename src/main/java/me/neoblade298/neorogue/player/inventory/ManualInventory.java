package me.neoblade298.neorogue.player.inventory;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.book.BookRegistry;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ManualInventory extends CoreInventory {
	private static final int EQUIPMENT = 11, BESTIARY = 13, TUTORIAL = 15, BACK = 22;

	private final CoreInventory prev;
	private final PlayerSessionData sessionData;

	public ManualInventory(Player viewer, CoreInventory prev) {
		this(viewer, prev, null);
	}

	public ManualInventory(Player viewer, PlayerSessionData sessionData) {
		this(viewer, null, sessionData);
	}

	private ManualInventory(Player viewer, CoreInventory prev, PlayerSessionData sessionData) {
		super(viewer, Bukkit.createInventory(viewer, 27, Component.text("Manual", NamedTextColor.GOLD)));
		this.prev = prev;
		this.sessionData = sessionData;
		inv.setItem(EQUIPMENT, createButton(Material.KNOWLEDGE_BOOK, "Equipment Glossary", NamedTextColor.AQUA,
				"View all equipment glossaries!", "You can also right click any", "item for its glossary"));
		inv.setItem(BESTIARY, createButton(Material.ZOMBIE_HEAD, "Bestiary", NamedTextColor.GREEN,
				"View all mob info!"));
		inv.setItem(TUTORIAL, createButton(Material.WRITABLE_BOOK, "Tutorial", NamedTextColor.GOLD,
				"View the tutorial!"));
		inv.setItem(BACK, CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED)));
	}

	public static ItemStack createIcon() {
		return createButton(Material.BOOK, "Manual", NamedTextColor.GOLD,
				"Access equipment glossary, bestiary, and tutorial");
	}

	private static ItemStack createButton(Material material, String name, NamedTextColor color, String... loreLines) {
		ItemStack item = CoreInventory.createButton(material, Component.text(name, color));
		ItemMeta meta = item.getItemMeta();
		meta.lore(List.of(loreLines).stream()
				.map(line -> Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
				.toList());
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (event.getCurrentItem() == null) return;
		switch (event.getSlot()) {
		case EQUIPMENT:
			new EquipmentGlossaryBrowserInventory(p, this);
			break;
		case BESTIARY:
			new BestiaryInventory(p, this);
			break;
		case TUTORIAL:
			p.closeInventory();
			if (sessionData != null) sessionData.trigger(SessionTrigger.OPEN_TUTORIAL_BOOK, null);
			BookRegistry.openTableOfContents(p, "neorogue_guide");
			break;
		case BACK:
			if (sessionData != null) new PlayerSessionInventory(sessionData);
			else if (prev != null) prev.openInventory();
			break;
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent event) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}