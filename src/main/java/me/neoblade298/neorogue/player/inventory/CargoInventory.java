package me.neoblade298.neorogue.player.inventory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.ascheladd.asheconomy.pricing.MaterialPrices;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.Cargo;
import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

// GUI for managing a player's persistent cargo stash. No CorePlayerInventory is registered, so
// InventoryListener routes every click/drag (top and bottom) here; interactions are distinguished
// via getClickedInventory(). Deposits accept eligible plain vanilla items (drag/drop, paint-drag,
// or shift-click); left/shift-left click withdraws 1/a stack.
public class CargoInventory extends CoreInventory {
	private static final DecimalFormat df = new DecimalFormat("#,##0.##");

	private final PlayerData pd;
	private final Cargo cargo;
	private final int infoSlot;
	private final HashMap<Integer, Material> slotToMaterial = new HashMap<Integer, Material>();

	public CargoInventory(Player p, PlayerData pd) {
		super(p, Bukkit.createInventory(p, computeSize(pd.getCargo()), Component.text("Cargo", NamedTextColor.GOLD)));
		this.pd = pd;
		this.cargo = pd.getCargo();
		this.infoSlot = inv.getSize() - 1;
		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		render();
	}

	private static int computeSize(Cargo cargo) {
		int itemCount = Math.max(cargo.getSlots(), cargo.getUsedSlots());
		int rows = (int) Math.ceil((itemCount + 1) / 9.0);
		if (rows < 1) rows = 1;
		if (rows > 6) rows = 6;
		return rows * 9;
	}

	private void render() {
		inv.clear();
		slotToMaterial.clear();
		int slot = 0;
		for (Map.Entry<Material, Integer> ent : cargo.getItems().entrySet()) {
			if (slot >= infoSlot) break;
			Material mat = ent.getKey();
			int count = ent.getValue();

			ItemStack disp = new ItemStack(mat, Math.max(1, Math.min(count, mat.getMaxStackSize())));
			ItemMeta meta = disp.getItemMeta();
			List<Component> lore = new ArrayList<Component>();
			lore.add(line(Component.text("Amount: ", NamedTextColor.GRAY)
					.append(Component.text(count, NamedTextColor.WHITE))));
			lore.add(line(Component.text("Sell value: ", NamedTextColor.GRAY)
					.append(Component.text(df.format(MaterialPrices.getPrice(mat) * count), NamedTextColor.GOLD))));
			lore.add(Component.empty());
			lore.add(line(Component.text("Left click: ", NamedTextColor.YELLOW)
					.append(Component.text("withdraw 1", NamedTextColor.WHITE))));
			lore.add(line(Component.text("Shift-left click: ", NamedTextColor.YELLOW)
					.append(Component.text("withdraw a stack", NamedTextColor.WHITE))));
			meta.lore(lore);
			disp.setItemMeta(meta);

			inv.setItem(slot, disp);
			slotToMaterial.put(slot, mat);
			slot++;
		}
		inv.setItem(infoSlot, buildInfoButton());
	}

	private ItemStack buildInfoButton() {
		ItemStack info = new ItemStack(Material.PAPER);
		ItemMeta meta = info.getItemMeta();
		meta.displayName(line(Component.text("Cargo", NamedTextColor.GOLD)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Items: ", NamedTextColor.GRAY)
				.append(Component.text(cargo.getTotalItems() + " / " + cargo.getCapacity(), NamedTextColor.WHITE))));
		lore.add(line(Component.text("Slots: ", NamedTextColor.GRAY)
				.append(Component.text(cargo.getUsedSlots() + " / " + cargo.getSlots(), NamedTextColor.WHITE))));
		lore.add(line(Component.text("Total sell value: ", NamedTextColor.GRAY)
				.append(Component.text(df.format(cargo.getTotalSellValue()), NamedTextColor.GOLD))));
		meta.lore(lore);
		info.setItemMeta(meta);
		return info;
	}

	private static Component line(Component c) {
		return c.decoration(TextDecoration.ITALIC, State.FALSE);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory clicked = e.getClickedInventory();
		if (clicked == null) {
			e.setCancelled(true);
			return;
		}

		// Top (cargo) inventory: fully controlled.
		if (clicked == inv) {
			e.setCancelled(true);
			int slot = e.getSlot();
			if (slot == infoSlot) return;

			Material mat = slotToMaterial.get(slot);
			if (mat != null) {
				// Withdraw: left = 1, shift-left = a stack. Ignore right clicks.
				if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) return;
				int amount = e.isShiftClick() ? mat.getMaxStackSize() : 1;
				int removed = cargo.removeItem(mat, amount);
				if (removed <= 0) return;
				giveOrDrop(mat, removed);
				render();
				click();
				return;
			}

			// Empty cargo slot: deposit whatever is on the cursor.
			ItemStack cursor = e.getCursor();
			if (cursor != null && !cursor.getType().isAir()) {
				depositFromCursor(cursor);
			}
			return;
		}

		// Bottom (player) inventory.
		if (clicked == p.getInventory()) {
			// Block double-click "collect to cursor" so it can't pull items out of the cargo display.
			if (e.getClick() == ClickType.DOUBLE_CLICK) {
				e.setCancelled(true);
				return;
			}
			if (e.isShiftClick()) {
				e.setCancelled(true);
				ItemStack item = e.getCurrentItem();
				if (item == null || item.getType().isAir()) return;
				if (!Cargo.isEligible(item)) {
					Util.displayError(p, "That item can't be added to cargo!");
					return;
				}
				int added = cargo.addItem(item.getType(), item.getAmount());
				if (added <= 0) {
					Util.displayError(p, "Your cargo has no room for that!");
					return;
				}
				int remaining = item.getAmount() - added;
				e.setCurrentItem(remaining > 0 ? withAmount(item, remaining) : null);
				render();
				click();
				if (remaining > 0) Util.displayError(p, "Your cargo couldn't fit all of that!");
			}
			// Non-shift clicks in the player's own inventory are left alone (normal manipulation).
			return;
		}
	}

	private void depositFromCursor(ItemStack cursor) {
		if (!Cargo.isEligible(cursor)) {
			Util.displayError(p, "That item can't be added to cargo!");
			return;
		}
		int added = cargo.addItem(cursor.getType(), cursor.getAmount());
		if (added <= 0) {
			Util.displayError(p, "Your cargo has no room for that!");
			return;
		}
		int remaining = cursor.getAmount() - added;
		p.setItemOnCursor(remaining > 0 ? withAmount(cursor, remaining) : null);
		render();
		click();
		if (remaining > 0) Util.displayError(p, "Your cargo couldn't fit all of that!");
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		boolean touchesTop = false;
		for (int raw : e.getRawSlots()) {
			if (raw < inv.getSize()) {
				touchesTop = true;
				break;
			}
		}
		// Drags confined to the player's own inventory are allowed.
		if (!touchesTop) return;

		e.setCancelled(true);
		ItemStack dragged = e.getOldCursor();
		if (dragged == null || dragged.getType().isAir()) return;
		if (!Cargo.isEligible(dragged)) {
			Util.displayError(p, "That item can't be added to cargo!");
			return;
		}
		int added = cargo.addItem(dragged.getType(), dragged.getAmount());
		int remaining = dragged.getAmount() - added;
		// Cancelling a drag restores the full cursor next tick, so override it afterwards.
		final ItemStack newCursor = remaining > 0 ? withAmount(dragged, remaining) : null;
		new BukkitRunnable() {
			@Override
			public void run() {
				p.setItemOnCursor(newCursor);
			}
		}.runTask(NeoRogue.inst());

		if (added <= 0) {
			Util.displayError(p, "Your cargo has no room for that!");
			return;
		}
		render();
		click();
		if (remaining > 0) Util.displayError(p, "Your cargo couldn't fit all of that!");
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		pd.saveCargoAsync();
	}

	private void giveOrDrop(Material mat, int amount) {
		ItemStack stack = new ItemStack(mat, amount);
		Map<Integer, ItemStack> leftover = p.getInventory().addItem(stack);
		for (ItemStack left : leftover.values()) {
			p.getWorld().dropItemNaturally(p.getLocation(), left);
		}
	}

	private static ItemStack withAmount(ItemStack base, int amount) {
		ItemStack clone = base.clone();
		clone.setAmount(amount);
		return clone;
	}

	private void click() {
		p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
	}
}
