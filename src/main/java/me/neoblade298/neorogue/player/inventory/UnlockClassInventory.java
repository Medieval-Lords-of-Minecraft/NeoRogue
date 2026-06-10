package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.unlock.UnlockNode;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class UnlockClassInventory extends CoreInventory {
	private static final int BACK = 0, INFO = 4;
	private static final int PREVIOUS = 48, NEXT = 50;
	private static final int PAGE_SIZE = 36; // Slots 9-44
	private static final String PREV_HEAD = ArtifactsInventory.PREV_HEAD;
	private static final String NEXT_HEAD = ArtifactsInventory.NEXT_HEAD;

	private final EquipmentClass ec;
	private final ArrayList<UnlockNode> nodes;
	private int page;

	public UnlockClassInventory(Player p, EquipmentClass ec) {
		super(p, Bukkit.createInventory(p, 54,
				Component.text((ec != null ? ec.getDisplay() : "Global") + " Unlocks", NamedTextColor.LIGHT_PURPLE)));
		this.ec = ec;
		this.nodes = UnlockRegistry.getNodesForClass(ec);
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();

		// Header row
		contents[BACK] = CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED));

		// Info item showing class + available points
		PlayerData data = PlayerManager.getPlayerData(p.getUniqueId());
		int points = data.getPoints(ec);
		String className = ec != null ? ec.getDisplay() : "Global";
		ItemStack info = new ItemStack(Material.BOOK);
		ItemMeta infoMeta = info.getItemMeta();
		infoMeta.displayName(Component.text(className + " Unlocks", NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, State.FALSE));
		infoMeta.lore(List.of(
				Component.text("Available Points: ", NamedTextColor.GRAY)
						.append(Component.text(points, NamedTextColor.YELLOW))
						.decoration(TextDecoration.ITALIC, State.FALSE),
				Component.text("Total Nodes: ", NamedTextColor.GRAY)
						.append(Component.text(nodes.size(), NamedTextColor.WHITE))
						.decoration(TextDecoration.ITALIC, State.FALSE)
		));
		info.setItemMeta(infoMeta);
		contents[INFO] = info;

		// Node items (paginated, slots 9-44)
		int start = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE && start + i < nodes.size(); i++) {
			contents[9 + i] = nodes.get(start + i).toItemStack(data);
		}

		// Navigation row (row 6, slots 45-53)
		int totalPages = (int) Math.ceil((double) nodes.size() / PAGE_SIZE);
		if (page > 0) {
			contents[PREVIOUS] = CoreInventory.createButton(PREV_HEAD, Component.text("Previous Page"));
		}
		if (page < totalPages - 1) {
			contents[NEXT] = CoreInventory.createButton(NEXT_HEAD, Component.text("Next Page"));
		}

		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getSlot();

		if (slot == BACK) {
			new UnlocksMenuInventory(p);
			return;
		}

		int totalPages = (int) Math.ceil((double) nodes.size() / PAGE_SIZE);
		if (slot == PREVIOUS && page > 0) {
			inv.clear();
			page--;
			setupInventory();
			return;
		}
		if (slot == NEXT && page < totalPages - 1) {
			inv.clear();
			page++;
			setupInventory();
			return;
		}

		// Check if clicking a node item (slots 9-44)
		if (slot >= 9 && slot <= 44) {
			NBTItem nclicked = new NBTItem(e.getCurrentItem());
			if (!nclicked.hasTag("unlockNodeId")) return;
			String nodeId = nclicked.getString("unlockNodeId");
			PlayerData data = PlayerManager.getPlayerData(p.getUniqueId());
			if (data.hasUnlockNode(nodeId)) {
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.5F);
				return;
			}
			boolean success = UnlockRegistry.grantWithCost(data, nodeId);
			if (success) {
				p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
				// Refresh inventory to show updated state
				inv.clear();
				setupInventory();
			} else {
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.5F);
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
