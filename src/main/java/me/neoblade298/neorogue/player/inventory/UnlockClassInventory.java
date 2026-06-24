package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.unlock.UnlockNode;
import me.neoblade298.neorogue.player.unlock.UnlockNode.TargetType;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class UnlockClassInventory extends CoreInventory {
	private static final int BACK = 0, INFO = 4;

	private final EquipmentClass ec;
	private final ArrayList<UnlockNode> nodes;
	private Player spectator;
	private PlayerData targetData;

	public UnlockClassInventory(Player p, EquipmentClass ec) {
		this(p, ec, null);
	}

	public UnlockClassInventory(Player viewer, EquipmentClass ec, PlayerData targetData) {
		super(viewer, Bukkit.createInventory(viewer, 54,
				Component.text((ec != null ? ec.getDisplay() : "Global") + " Unlocks", NamedTextColor.LIGHT_PURPLE)));
		this.ec = ec;
		this.nodes = UnlockRegistry.getNodesForClass(ec);
		this.targetData = targetData;
		this.spectator = targetData != null ? viewer : null;
		setupInventory();
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();

		// Header row
		contents[BACK] = CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED));

		// Info item showing class + available points
		PlayerData data = targetData != null ? targetData : PlayerManager.getPlayerData(p.getUniqueId());
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

		// Node items (slots 9-53)
		Set<Integer> reserved = new HashSet<>();
		for (int s : new int[] { BACK, INFO }) {
			reserved.add(s);
		}
		boolean[] occupied = new boolean[54];
		for (int s : new int[] { BACK, INFO }) {
			occupied[s] = true;
		}
		List<UnlockNode> unplaced = new ArrayList<>();
		for (UnlockNode node : nodes) {
			int preferred = node.getSlot();
			if (preferred >= 0 && preferred < 54) {
				if (occupied[preferred]) {
					Bukkit.getLogger().warning("[NeoRogue] Unlock node " + node.getId()
							+ " has slot " + preferred + " which is already occupied, using next available");
					unplaced.add(node);
				} else {
					contents[preferred] = node.toItemStack(data);
					occupied[preferred] = true;
				}
			} else {
				unplaced.add(node);
			}
		}
		// Fill unplaced nodes into first available slots (9-53)
		int nextSlot = 9;
		for (UnlockNode node : unplaced) {
			while (nextSlot < 54 && occupied[nextSlot]) {
				nextSlot++;
			}
			if (nextSlot >= 54) break;
			contents[nextSlot] = node.toItemStack(data);
			occupied[nextSlot] = true;
			nextSlot++;
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
			if (spectator != null) {
				new UnlocksMenuInventory(spectator, targetData);
			} else {
				new UnlocksMenuInventory(p);
			}
			return;
		}

		// Check if clicking a node item
		if (slot >= 9) {
			NBTItem nclicked = new NBTItem(e.getCurrentItem());
			if (!nclicked.hasTag("unlockNodeId")) return;
			String nodeId = nclicked.getString("unlockNodeId");
			PlayerData data = targetData != null ? targetData : PlayerManager.getPlayerData(p.getUniqueId());
			UnlockNode node = UnlockRegistry.getNode(nodeId);

			// Right-click: view equipment in glossary
			if (e.isRightClick() && node != null && node.getTargetType() == TargetType.EQUIPMENT) {
				ArrayList<Equipment> equips = new ArrayList<>();
				for (String targetId : node.getTargetIds()) {
					Equipment eq = Equipment.get(targetId, false);
					if (eq != null) equips.add(eq);
				}
				if (!equips.isEmpty()) {
					new GlossaryViewInventory(p, equips, Component.text(node.getDisplayName()), this);
				}
				return;
			}

			if (spectator != null) {
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.5F);
				return;
			}

			if (data.hasUnlockNode(nodeId)) {
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.5F);
				return;
			}
			boolean success = UnlockRegistry.grantWithCost(data, nodeId);
			if (success) {
				p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
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
