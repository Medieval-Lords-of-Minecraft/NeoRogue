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
	private ArrayList<ReforgeResultEntry> entries;
	private int page;

	public AvailableReforgesInventory(PlayerSessionData data) {
		this(data, data.computeAvailableReforges());
	}

	private AvailableReforgesInventory(PlayerSessionData data, ArrayList<ReforgePairData> pairs) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(),
				calculateSize(countResults(pairs)), Component.text("Available Reforges", NamedTextColor.GOLD)));
		new PlayerSessionInventory(data);
		this.data = data;
		this.entries = buildEntries(pairs);
		setupInventory();
	}

	private static int countResults(ArrayList<ReforgePairData> pairs) {
		int count = 0;
		for (ReforgePairData pair : pairs) {
			for (Equipment result : pair.getResults()) {
				if (result != null) count++;
			}
		}
		return count;
	}

	private static ArrayList<ReforgeResultEntry> buildEntries(ArrayList<ReforgePairData> pairs) {
		ArrayList<ReforgeResultEntry> entries = new ArrayList<>();
		for (ReforgePairData pair : pairs) {
			for (Equipment result : pair.getResults()) {
				if (result != null) {
					entries.add(new ReforgeResultEntry(pair, result));
				}
			}
		}
		return entries;
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
		int maxItems = Math.min(entries.size() - start, Math.min(45, contents.length));

		for (int i = 0; i < maxItems; i++) {
			ReforgeResultEntry entry = entries.get(start + i);
			contents[i] = createResultIcon(entry);
		}

		if (entries.isEmpty()) {
			for (int i = 0; i < contents.length; i++) {
				contents[i] = CoreInventory.createButton(Material.BARRIER,
						Component.text("No reforges available!", NamedTextColor.RED));
			}
		}

		if (entries.size() > 45) {
			if (page > 0)
				contents[contents.length - 9 + PREVIOUS] = CoreInventory.createButton(
						ArtifactsInventory.PREV_HEAD, Component.text("Previous Page"));
			if (start + 45 < entries.size())
				contents[contents.length - 9 + NEXT] = CoreInventory.createButton(
						ArtifactsInventory.NEXT_HEAD, Component.text("Next Page"));
		}

		inv.setContents(contents);
	}

	private ItemStack createResultIcon(ReforgeResultEntry entry) {
		ReforgePairData pair = entry.pair;
		Equipment eq1 = pair.getMeta1().getEquipment();
		Equipment eq2 = pair.getMeta2().getEquipment();
		Equipment result = entry.result;

		ItemStack icon = result.getItem().clone();
		ItemMeta meta = icon.getItemMeta();

		// Name: the combining pair
		meta.displayName(Component.text("Reforge: ", NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, State.FALSE)
				.append(eq1.getHoverable())
				.append(Component.text(" + ", NamedTextColor.GRAY))
				.append(eq2.getHoverable()));

		ArrayList<Component> lore = new ArrayList<>();

		// Result name and description from the item's existing lore
		lore.add(Component.text("Result: ", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, State.FALSE)
				.append(result.getDisplay()));
		ItemStack resultItem = result.getItem();
		if (resultItem.hasItemMeta() && resultItem.getItemMeta().hasLore()) {
			for (Component loreLine : resultItem.getItemMeta().lore()) {
				lore.add(loreLine);
			}
		}

		// Opportunity cost: other reforges using the same components
		ArrayList<Component> conflicts = new ArrayList<>();
		String id1 = eq1.getId();
		String id2 = eq2.getId();
		for (ReforgeResultEntry other : entries) {
			if (other == entry) continue;
			// Same pair, different result
			if (other.pair == pair) {
				conflicts.add(Component.text("  - ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE)
						.append(other.result.getHoverable())
						.append(Component.text(" (same pair)", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, State.FALSE)));
				continue;
			}
			String otherId1 = other.pair.getMeta1().getEquipment().getId();
			String otherId2 = other.pair.getMeta2().getEquipment().getId();
			boolean shares = id1.equals(otherId1) || id1.equals(otherId2) || id2.equals(otherId1) || id2.equals(otherId2);
			if (shares) {
				Equipment otherEq1 = other.pair.getMeta1().getEquipment();
				Equipment otherEq2 = other.pair.getMeta2().getEquipment();
				conflicts.add(Component.text("  - ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE)
						.append(otherEq1.getHoverable())
						.append(Component.text(" + ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE))
						.append(otherEq2.getHoverable())
						.append(Component.text(" \u2192 ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE))
						.append(other.result.getHoverable()));
			}
		}

		if (!conflicts.isEmpty()) {
			lore.add(Component.empty());
			lore.add(Component.text("Choosing this blocks:", NamedTextColor.RED).decoration(TextDecoration.ITALIC, State.FALSE));
			lore.addAll(conflicts);
		}
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
		if (entries.size() > 45 && slot >= inv.getSize() - 9) {
			if (slot == inv.getSize() - 9 + NEXT && (page + 1) * 45 < entries.size()) {
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
		if (index >= entries.size()) return;
		ReforgeResultEntry entry = entries.get(index);
		ReforgePairData pair = entry.pair;

		// Right-click: open glossary for this result
		if (e.isRightClick()) {
			new BukkitRunnable() {
				public void run() {
					new EquipmentGlossaryInventory(p, entry.result, AvailableReforgesInventory.this);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		// Left-click: validate items still exist and open confirm inventory
		EquipmentMetadata m1 = pair.getMeta1();
		EquipmentMetadata m2 = pair.getMeta2();

		Equipment[] arr1 = data.getEquipment(m1.getEquipSlot());
		Equipment[] arr2 = data.getEquipment(m2.getEquipSlot());

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
				new ReforgeConfirmInventory(data, eq1, eq2, entry.result, AvailableReforgesInventory.this);
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

	private static class ReforgeResultEntry {
		final ReforgePairData pair;
		final Equipment result;

		ReforgeResultEntry(ReforgePairData pair, Equipment result) {
			this.pair = pair;
			this.result = result;
		}
	}
}
