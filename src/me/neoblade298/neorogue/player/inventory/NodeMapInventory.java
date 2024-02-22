package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class NodeMapInventory extends CoreInventory {
	private static final int BACK2 = 2, BACK = 3, FORWARD = 5, FORWARD2 = 6;
	
	private static final String ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIyMjFjYjk2MDdjOGE5YmYwMmZlZjVkNzYxNGUzZWIxNjljYzIxOWJmNDI1MGZkNTcxNWQ1ZDJkNjA0NWY3In19fQ==",
			ARROW_UPLEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJiOGUzZWFlYTU1OGY4NmFlYmEzMjI5NjlkNGVlYjZiOTY5NDM0ZjVhZDc5MjY2ZDVkOTY4YjI4ZDkxOTJlIn19fQ==",
			ARROW_UPRIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzgzMGU4N2JiNDQ3N2QyMTM2ZWQ0MzcxNzg0OTUzN2Y0ZDUxOWI0NGQ4ZmQ2ZTliNGMyZWJlNTJmYThmIn19fQ==",
			ARROW_UP1 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=",
			ARROW_DOWN1 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTllOTM4MTgxZDhjOTZiNGY1OGY2MzMyZDNkZDIzM2VjNWZiODUxYjVhODQwNDM4ZWFjZGJiNjE5YTNmNWYifX19",
			ARROW_UP2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Q2OTVkMzM1ZTZiZThjYjJhMzRlMDVlMThlYTJkMTJjM2IxN2I4MTY2YmE2MmQ2OTgyYTY0M2RmNzFmZmFjNSJ9fX0=",
			ARROW_DOWN2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM3ODYyY2RjMTU5OTk4ZWQ2YjZmZGNjYWFhNDY3NTg2N2Q0NDg0ZGI1MTJhODRjMzY3ZmFiZjRjYWY2MCJ9fX0=";
	
	private int currentPos;
	private Node[][] nodes;
	private Node curr;

	public NodeMapInventory(Player p, Session s) {
		super(p, Bukkit.createInventory(p, 54, Component.text("Node Map", NamedTextColor.GOLD)));
		this.nodes = s.getArea().getNodes();
		currentPos = s.getNode().getPosition();
		curr = s.getNode();
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		
		setupInventory();
		inv.setItem(BACK2, CoreInventory.createButton(ARROW_DOWN2, Component.text("Down 3 positions")));
		inv.setItem(BACK, CoreInventory.createButton(ARROW_DOWN1, Component.text("Down 1 position")));
		inv.setItem(FORWARD, CoreInventory.createButton(ARROW_UP1, Component.text("Up 1 position")));
		inv.setItem(FORWARD2, CoreInventory.createButton(ARROW_UP2, Component.text("Up 3 positions")));
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		
		// Clear existing nodes if any
		for (int i = 9; i < 54; i++) {
			contents[i] = null;
		}
		
		// Place down nodes
		for (int pos = currentPos; pos < currentPos + 3 && pos < nodes.length; pos++) {
			for (int lane = 0; lane < 5; lane++) {
				Node node = nodes[pos][lane];
				if (node == null) continue;
				int slot = nodeToSlot(node);
				if (node.equals(curr)) {
			        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
			        SkullMeta meta = (SkullMeta) skull.getItemMeta();
			        meta.setOwningPlayer(p);
			        skull.setItemMeta(meta);
					contents[slot] = CoreInventory.createButton(skull, Component.text("Lane " + lane + " " + node.getType().name() + " (You are here)"));
				}
				else {
					contents[slot] = CoreInventory.createButton(node.getType().getBlock(), Component.text("Lane " + lane + " " + node.getType().name()));
				}
				ArrayList<Component> lore = new ArrayList<Component>(1 + node.getDestinations().size());
				if (!node.getDestinations().isEmpty()) lore.add(Component.text("Connects to:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
				node.sortDestinations();
				for (Node dest : node.getDestinations()) {
					lore.add(Component.text("- Lane " + dest.getLane() + " " + dest.getType() , NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
					// Only add arrows to next if the node shows up on the inventory
					if (slot > 18) {
						if (dest.getLane() > lane) {
							contents[slot - 8] = CoreInventory.createButton(ARROW_UPRIGHT, Component.text(" "));
						}
						else if (dest.getLane() < lane) {
							contents[slot - 10] = CoreInventory.createButton(ARROW_UPLEFT, Component.text(" "));
						}
						else {
							contents[slot - 9] = CoreInventory.createButton(ARROW_UP, Component.text(" "));
						}
					}
				}
				contents[slot].lore(lore);
			}
		}
		inv.setContents(contents);
	}
	
	private int nodeToSlot(Node node) {
		return (node.getLane() * 2) + ((2 - node.getPosition() + currentPos) * 18) + 9;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return;
		if (e.getClickedInventory() != inv) return;
		int slot = e.getSlot();
		if (slot == BACK2) {
			turnPage(-3);
		}
		else if (slot == BACK) {
			turnPage(-1);
		}
		else if (slot == FORWARD) {
			turnPage(1);
		}
		else if (slot == FORWARD2) {
			turnPage(3);
		}
	}
	
	private void turnPage(int diff) {
		currentPos = Math.max(0, Math.min(nodes.length - 1, currentPos + diff));
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		setupInventory();
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
	
}
