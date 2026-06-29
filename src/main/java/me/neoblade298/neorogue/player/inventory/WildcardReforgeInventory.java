package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

/**
 * Displays every reforge result a target equipment can become and lets the player pick one,
 * consuming both the target and the wildcard reforge item. Used by reforge wildcards such as
 * Transmutation, which can reforge any item regardless of normal pairing rules.
 */
public class WildcardReforgeInventory extends CoreInventory {
	private static final int PREVIOUS = 4, NEXT = 6;
	private PlayerSessionData data;
	private Equipment toReforge, wildcard;
	private ArrayList<Equipment> results;
	private int page;

	public WildcardReforgeInventory(PlayerSessionData data, Equipment toReforge, Equipment wildcard) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(),
				calculateSize(toReforge.getAllReforgeResults().size()),
				Component.text("Wildcard Reforge", NamedTextColor.GOLD)));
		this.data = data;
		this.toReforge = toReforge;
		this.wildcard = wildcard;
		this.results = toReforge.getAllReforgeResults();
		setupInventory();
	}

	private static int calculateSize(int count) {
		if (count <= 0) return 9;
		int size = 9 * (int) Math.ceil((double) count / 9);
		if (count > 45) size += 9;
		return Math.min(54, size);
	}

	private void setupInventory() {
		p.playSound(p, Sound.BLOCK_ANVIL_PLACE, 0.5F, 1F);
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < contents.length; i++) {
			contents[i] = null;
		}

		int start = page * 45;
		int maxItems = Math.min(results.size() - start, Math.min(45, contents.length));
		for (int i = 0; i < maxItems; i++) {
			contents[i] = createResultIcon(results.get(start + i));
		}

		if (results.size() > 45) {
			if (page > 0)
				contents[contents.length - 9 + PREVIOUS] = CoreInventory.createButton(
						ArtifactsInventory.PREV_HEAD, Component.text("Previous Page"));
			if (start + 45 < results.size())
				contents[contents.length - 9 + NEXT] = CoreInventory.createButton(
						ArtifactsInventory.NEXT_HEAD, Component.text("Next Page"));
		}

		inv.setContents(contents);
	}

	private ItemStack createResultIcon(Equipment result) {
		ItemStack icon = result.getItem().clone();
		ItemMeta meta = icon.getItemMeta();
		ArrayList<Component> lore = meta.hasLore() ? new ArrayList<Component>(meta.lore()) : new ArrayList<Component>();
		lore.add(Component.empty());
		lore.add(Component.text("Left-click to reforge into this", NamedTextColor.GREEN)
				.decoration(TextDecoration.ITALIC, State.FALSE));
		lore.add(Component.text("Right-click for glossary", NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, State.FALSE));
		meta.lore(lore);
		icon.setItemMeta(meta);
		return icon;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getSlot();

		// Handle pagination
		if (results.size() > 45 && slot >= inv.getSize() - 9) {
			if (slot == inv.getSize() - 9 + NEXT && (page + 1) * 45 < results.size()) {
				page++;
				setupInventory();
			}
			else if (slot == inv.getSize() - 9 + PREVIOUS && page > 0) {
				page--;
				setupInventory();
			}
			return;
		}

		int index = page * 45 + slot;
		if (index >= results.size()) return;
		Equipment result = results.get(index);

		// Right-click: open glossary for this result. Closing returns the items to the player.
		if (e.isRightClick()) {
			new BukkitRunnable() {
				public void run() {
					new EquipmentGlossaryInventory(p, result, null);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		// Left-click: perform the reforge
		p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
		Component cmp = SharedUtil.color("<yellow>" + p.getName() + "</yellow> reforged their ")
				.append(toReforge.getHoverable());
		cmp = cmp.append(Component.text(" into a(n) ").append(result.getHoverable().append(Component.text("!"))));
		data.getSession().broadcast(cmp);

		data.giveEquipmentSilent(result);
		toReforge = null;
		wildcard = null;
		new BukkitRunnable() {
			public void run() {
				p.closeInventory();
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (toReforge != null) {
			data.giveEquipment(toReforge, null, null);
			data.giveEquipment(wildcard, null, null);
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
