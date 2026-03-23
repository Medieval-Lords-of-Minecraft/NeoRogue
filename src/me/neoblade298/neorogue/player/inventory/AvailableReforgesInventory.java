package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.player.PlayerSessionData.ReforgePairData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class AvailableReforgesInventory extends CoreInventory {
	private static final int PREVIOUS = 4, NEXT = 6;
	private PlayerSessionData data;
	private ArrayList<ReforgePairData> pairs;
	private int page;

	public AvailableReforgesInventory(PlayerSessionData data) {
		this(data, data.computeAvailableReforges());
	}

	private AvailableReforgesInventory(PlayerSessionData data, ArrayList<ReforgePairData> pairs) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(),
				calculateSize(pairs.size()), Component.text("Available Reforges", NamedTextColor.DARK_PURPLE)));
		new PlayerSessionInventory(data);
		this.data = data;
		this.pairs = pairs;
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

		int start = page * 45;
		int maxItems = Math.min(pairs.size() - start, Math.min(45, contents.length));

		for (int i = 0; i < maxItems; i++) {
			ReforgePairData pair = pairs.get(start + i);
			contents[i] = createPairIcon(pair);
		}

		if (pairs.isEmpty()) {
			for (int i = 0; i < contents.length; i++) {
				contents[i] = CoreInventory.createButton(Material.BARRIER,
						Component.text("No reforges available!", NamedTextColor.RED));
			}
		}

		if (pairs.size() > 45) {
			if (page > 0)
				contents[contents.length - 9 + PREVIOUS] = CoreInventory.createButton(
						ArtifactsInventory.PREV_HEAD, Component.text("Previous Page"));
			if (start + 45 < pairs.size())
				contents[contents.length - 9 + NEXT] = CoreInventory.createButton(
						ArtifactsInventory.NEXT_HEAD, Component.text("Next Page"));
		}

		inv.setContents(contents);
	}

	private ItemStack createPairIcon(ReforgePairData pair) {
		Equipment eq1 = pair.getMeta1().getEquipment();
		Equipment eq2 = pair.getMeta2().getEquipment();
		Equipment[] results = pair.getResults();

		// Use first result's item as the icon base
		ItemStack icon = results[0].getItem().clone();
		ItemMeta meta = icon.getItemMeta();

		meta.displayName(Component.text("Reforge: ", NamedTextColor.LIGHT_PURPLE)
				.decoration(TextDecoration.ITALIC, State.FALSE)
				.append(eq1.getHoverable())
				.append(Component.text(" + ", NamedTextColor.GRAY))
				.append(eq2.getHoverable()));

		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Possible Results:", NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, State.FALSE));
		for (Equipment result : results) {
			if (result == null) continue;
			lore.add(Component.text("  - ", NamedTextColor.GRAY)
					.decoration(TextDecoration.ITALIC, State.FALSE)
					.append(result.getHoverable()));
		}
		lore.add(Component.empty());
		lore.add(Component.text("Left-click to reforge", NamedTextColor.YELLOW)
				.decoration(TextDecoration.ITALIC, State.FALSE));
		lore.add(Component.text("Right-click for details", NamedTextColor.GRAY)
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
		if (pairs.size() > 45 && slot >= inv.getSize() - 9) {
			if (slot == inv.getSize() - 9 + NEXT && (page + 1) * 45 < pairs.size()) {
				inv.clear();
				page++;
				setupInventory();
			}
			else if (slot == inv.getSize() - 9 + PREVIOUS && page > 0) {
				inv.clear();
				page--;
				setupInventory();
			}
			return;
		}

		int index = page * 45 + slot;
		if (index >= pairs.size()) return;
		ReforgePairData pair = pairs.get(index);

		// Right-click: open glossary for first result
		if (e.isRightClick()) {
			Equipment[] results = pair.getResults();
			if (results.length > 0 && results[0] != null) {
				new BukkitRunnable() {
					public void run() {
						new EquipmentGlossaryInventory(p, results[0], null);
					}
				}.runTask(NeoRogue.inst());
			}
			return;
		}

		// Left-click: validate items still exist and perform reforge
		EquipmentMetadata m1 = pair.getMeta1();
		EquipmentMetadata m2 = pair.getMeta2();

		Equipment[] arr1 = data.getEquipment(m1.getEquipSlot());
		Equipment[] arr2 = data.getEquipment(m2.getEquipSlot());

		// Validate items still exist at their locations
		if (arr1[m1.getSlot()] != m1.getEquipment() || arr2[m2.getSlot()] != m2.getEquipment()) {
			p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1F, 1F);
			new BukkitRunnable() {
				public void run() {
					p.closeInventory();
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		Equipment eq1 = data.removeEquipment(m1.getEquipSlot(), m1.getSlot());
		Equipment eq2 = data.removeEquipment(m2.getEquipSlot(), m2.getSlot());

		new BukkitRunnable() {
			public void run() {
				p.closeInventory();
				new ReforgeOptionsInventory(data, eq1, eq2);
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
