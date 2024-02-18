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
	private static final int BACK2 = 47, BACK = 48, FORWARD = 50, FORWARD2 = 51;
	
	private static final String ARROW_FORWARD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU0MmQwYzc0MjkzODc5NDhjZjk5ZThjYjhjNTU1OWU3MzNmYzdkYmZiMTg0YzJjMGI3ZDllZmQ4MjlmZiJ9fX0=",
			ARROW_FORWARD2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmEwYmI3Nzc3MzhmMDVjNDQxNDhkYzQ3NDUzZmNlNzdlMTJhYTQxYTEwYjExODk1YmJlN2UyNDY5ODI3MzEifX19",
			ARROW_BACK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU4ZjQyNGUzNjk3YmE0YWViZmU2NzgwNTcxOTczOTU2NWQ1MzY4NjY1YWIyMWFmOWNlMmViZGRkODk0NWM4In19fQ==",
			ARROW_BACK2 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM0OTM5ZDI2NDQ0YTU3MzI3ZjA2NGMzOTI4ZGE2MWYzNmNhZjYyMmRlYmU3NGMzM2Y4ZjhhMzZkYTIyIn19fQ==";
	
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
		inv.setItem(BACK2, CoreInventory.createButton(ARROW_BACK2, Component.text("Back 5 positions")));
		inv.setItem(BACK, CoreInventory.createButton(ARROW_BACK, Component.text("Back 1 position")));
		inv.setItem(FORWARD, CoreInventory.createButton(ARROW_FORWARD, Component.text("Forward 1 position")));
		inv.setItem(FORWARD2, CoreInventory.createButton(ARROW_FORWARD2, Component.text("Forward 5 positions")));
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		
		// Clear existing nodes if any
		for (int i = 0; i < 45; i++) {
			contents[i] = null;
		}
		
		// Place down nodes
		for (int pos = currentPos; pos < currentPos + 5 && pos < nodes.length; pos++) {
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
				lore.add(Component.text("Connects to:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
				node.sortDestinations();
				for (Node dest : node.getDestinations()) {
					lore.add(Component.text("- Lane " + dest.getLane() + " " + dest.getType() , NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
				}
				contents[slot].lore(lore);
			}
		}
		inv.setContents(contents);
	}
	
	private int nodeToSlot(Node node) {
		return (node.getLane() * 9) + ((node.getPosition() - currentPos) * 2);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return;
		if (e.getClickedInventory() != inv) return;
		int slot = e.getSlot();
		if (slot == BACK2) {
			turnPage(-5);
		}
		else if (slot == BACK) {
			turnPage(-1);
		}
		else if (slot == FORWARD) {
			turnPage(1);
		}
		else if (slot == FORWARD2) {
			turnPage(5);
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
