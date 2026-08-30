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

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.api.NeoRogueAPI;
import me.neoblade298.neorogue.player.Cargo;
import me.neoblade298.neorogue.player.CargoItem;
import me.neoblade298.neorogue.player.FleetHold;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerData.PendingFleetSale;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

// GUI for managing a player's persistent cargo. No CorePlayerInventory is registered, so
// InventoryListener routes every click/drag (top and bottom) here; interactions are distinguished
// via getClickedInventory(). Deposits accept eligible plain vanilla items (drag/drop, paint-drag,
// or shift-click); left/shift-left click withdraws 1/a stack.
//
// The bottom row is a dedicated control row: back, fleet navigation (left/right + hold indicator),
// collect earnings, lost cargo, and info. Fleet holds (currentHold >= 1) are extra holds beyond the
// main cargo that auto-sell at midnight America/Los_Angeles; their proceeds are collected here.
public class CargoInventory extends CoreInventory {
	private static final DecimalFormat df = new DecimalFormat("#,##0.##");

	private final PlayerData pd;
	// 0 = main cargo; 1..fleetSize = fleet holds.
	private int currentHold = 0;

	private final boolean multiHold;
	private final boolean returnToCaravanMenu;
	private final int controlBase;
	private final int backSlot, prevSlot, holdInfoSlot, nextSlot, collectSlot, lostCargoSlot, infoSlot;
	private final HashMap<Integer, CargoItem> slotToItem = new HashMap<Integer, CargoItem>();

	public CargoInventory(Player p, PlayerData pd) {
		this(p, pd, false);
	}

	public CargoInventory(Player p, PlayerData pd, boolean returnToCaravanMenu) {
		super(p, Bukkit.createInventory(p, computeSize(pd), Component.text("Cargo", NamedTextColor.GOLD)));
		this.pd = pd;
		this.returnToCaravanMenu = returnToCaravanMenu;
		// Resolve any fleet holds that auto-sold since the player was last here.
		pd.resolveFleetSales();

		this.multiHold = pd.getFleetSize() >= 1;
		this.controlBase = inv.getSize() - 9;
		this.backSlot = controlBase;
		this.prevSlot = controlBase + 3;
		this.holdInfoSlot = controlBase + 4;
		this.nextSlot = controlBase + 5;
		this.collectSlot = controlBase + 6;
		this.lostCargoSlot = controlBase + 7;
		this.infoSlot = controlBase + 8;

		p.playSound(p, Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);
		render();
	}

	private static int computeSize(PlayerData pd) {
		// A fixed 6-row layout when fleet holds exist, so the inventory can switch holds without resizing.
		if (pd.getFleetSize() >= 1) return 54;
		Cargo c = pd.getCargo();
		int itemCount = Math.max(c.getSlots(), c.getUsedSlots());
		int rows = (int) Math.ceil(itemCount / 9.0) + 1; // +1 for the control row
		if (rows < 2) rows = 2;
		if (rows > 6) rows = 6;
		return rows * 9;
	}

	private boolean isFleetView() {
		return currentHold >= 1;
	}

	private FleetHold activeFleetHold() {
		return pd.getFleetHold(currentHold);
	}

	// The item storage backing the current view.
	private Cargo activeCargo() {
		if (isFleetView()) {
			FleetHold hold = activeFleetHold();
			if (hold != null) return hold.getCargo();
		}
		return pd.getCargo();
	}

	private int depositActive(CargoItem item, int amount) {
		if (isFleetView()) {
			FleetHold hold = activeFleetHold();
			return hold != null ? hold.addItem(item, amount) : 0;
		}
		return pd.getCargo().addItem(item, amount);
	}

	private int withdrawActive(CargoItem item, int amount) {
		if (isFleetView()) {
			FleetHold hold = activeFleetHold();
			return hold != null ? hold.removeItem(item, amount) : 0;
		}
		return pd.getCargo().removeItem(item, amount);
	}

	private boolean hasLostCargo() {
		return pd.hasFlag(PlayerData.FLAG_CARGO_INSURANCE) && pd.getLostCargo().getTotalItems() > 0;
	}

	private void render() {
		inv.clear();
		slotToItem.clear();
		Cargo view = activeCargo();
		boolean fleet = isFleetView();
		FleetHold hold = fleet ? activeFleetHold() : null;

		int slot = 0;
		for (Map.Entry<CargoItem, Integer> ent : view.getItems().entrySet()) {
			if (slot >= controlBase) break;
			CargoItem item = ent.getKey();
			int count = ent.getValue();

			// Add GUI lore only to a defensive clone; the withdrawable prototype remains untouched.
			ItemStack disp = item.createStack(Math.min(count, item.createStack().getMaxStackSize()));
			ItemMeta meta = disp.getItemMeta();
			List<Component> lore = new ArrayList<Component>();
			lore.add(line(Component.text("Amount: ", NamedTextColor.GRAY)
					.append(Component.text(count, NamedTextColor.WHITE))));
			if (fleet && hold != null) {
				lore.add(line(Component.text("Locked-in value: ", NamedTextColor.GRAY)
						.append(Component.text(df.format(hold.getUnitPrice(item) * count), NamedTextColor.GOLD))));
			} else {
				lore.add(line(Component.text("Sell value: ", NamedTextColor.GRAY)
						.append(Component.text(df.format(item.getEffectivePrice() * count), NamedTextColor.GOLD))));
			}
			lore.add(Component.empty());
			lore.add(line(Component.text("Left click: ", NamedTextColor.YELLOW)
					.append(Component.text("withdraw 1", NamedTextColor.WHITE))));
			lore.add(line(Component.text("Shift-left click: ", NamedTextColor.YELLOW)
					.append(Component.text("withdraw a stack", NamedTextColor.WHITE))));
			meta.lore(lore);
			disp.setItemMeta(meta);

			inv.setItem(slot, disp);
			slotToItem.put(slot, item);
			slot++;
		}
		// Leave slots open (empty) up to the cargo's slot limit so the player has room to deposit;
		// pane off everything beyond the limit.
		int openLimit = Math.min(view.getSlots(), controlBase);
		for (int i = slot; i < controlBase; i++) {
			if (i >= openLimit) inv.setItem(i, buildFillerPane());
		}
		renderControlRow();
	}

	private void renderControlRow() {
		for (int i = controlBase; i < inv.getSize(); i++) {
			inv.setItem(i, buildFillerPane());
		}
		inv.setItem(backSlot, buildBackButton());
		inv.setItem(infoSlot, buildInfoButton());
		if (multiHold) {
			inv.setItem(prevSlot, buildNavButton(false));
			inv.setItem(holdInfoSlot, buildHoldInfoButton());
			inv.setItem(nextSlot, buildNavButton(true));
		}
		if (pd.hasPendingFleetSales()) inv.setItem(collectSlot, buildCollectButton());
		if (hasLostCargo()) inv.setItem(lostCargoSlot, buildLostCargoButton());
	}

	private ItemStack buildFillerPane() {
		ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = pane.getItemMeta();
		meta.displayName(Component.empty());
		pane.setItemMeta(meta);
		return pane;
	}

	private ItemStack buildBackButton() {
		ItemStack item = new ItemStack(Material.ARROW);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text("Back", NamedTextColor.YELLOW)));
		meta.lore(new ArrayList<Component>());
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildNavButton(boolean next) {
		ItemStack item = new ItemStack(Material.SPECTRAL_ARROW);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text(next ? "Next Hold \u2192" : "\u2190 Previous Hold", NamedTextColor.AQUA)));
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildHoldInfoButton() {
		ItemStack item = new ItemStack(isFleetView() ? Material.MINECART : Material.CHEST_MINECART);
		ItemMeta meta = item.getItemMeta();
		if (isFleetView()) {
			FleetHold hold = activeFleetHold();
			meta.displayName(line(Component.text("Fleet Hold " + currentHold + " of " + pd.getFleetSize(), NamedTextColor.AQUA)));
			List<Component> lore = new ArrayList<Component>();
			lore.add(line(Component.text("Locked-in value: ", NamedTextColor.GRAY)
					.append(Component.text(df.format(hold != null ? hold.getSnapshotValue() : 0), NamedTextColor.GOLD))));
			lore.add(Component.empty());
			lore.add(line(Component.text("This hold is auto-sold at midnight", NamedTextColor.GRAY)));
			lore.add(line(Component.text("(America/Los_Angeles). Collect the coin", NamedTextColor.GRAY)));
			lore.add(line(Component.text("from the gold button afterwards.", NamedTextColor.GRAY)));
			meta.lore(lore);
		} else {
			meta.displayName(line(Component.text("Main Cargo", NamedTextColor.GOLD)));
			List<Component> lore = new ArrayList<Component>();
			lore.add(line(Component.text("Your main cargo is never auto-sold.", NamedTextColor.GRAY)));
			lore.add(line(Component.text("Use the arrows to view your fleet holds.", NamedTextColor.GRAY)));
			meta.lore(lore);
		}
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildCollectButton() {
		ItemStack item = new ItemStack(Material.GOLD_INGOT);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text("Collect Fleet Earnings", NamedTextColor.GOLD)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Your fleet sold these goods:", NamedTextColor.GRAY)));
		for (PendingFleetSale sale : pd.getPendingFleetSales()) {
			lore.add(line(Component.text("  " + sale.item.getLabel() + " x" + sale.amount, NamedTextColor.WHITE)
					.append(Component.text(" for ", NamedTextColor.GRAY))
					.append(Component.text(df.format(sale.value), NamedTextColor.GOLD))));
		}
		lore.add(Component.empty());
		lore.add(line(Component.text("Total: ", NamedTextColor.GRAY)
				.append(Component.text(df.format(pd.getPendingFleetEarnings()), NamedTextColor.GOLD))));
		lore.add(Component.empty());
		lore.add(line(Component.text("Left click: ", NamedTextColor.YELLOW)
				.append(Component.text("collect", NamedTextColor.WHITE))));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildLostCargoButton() {
		Cargo lost = pd.getLostCargo();
		ItemStack item = new ItemStack(Material.HOPPER_MINECART);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text("Lost Cargo", NamedTextColor.RED)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Items: ", NamedTextColor.GRAY)
				.append(Component.text(lost.getTotalItems(), NamedTextColor.WHITE))));
		lore.add(line(Component.text("Total sell value: ", NamedTextColor.GRAY)
				.append(Component.text(df.format(lost.getTotalSellValue()), NamedTextColor.GOLD))));
		lore.add(Component.empty());
		lore.add(line(Component.text("Left click: ", NamedTextColor.YELLOW)
				.append(Component.text("open lost cargo", NamedTextColor.WHITE))));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack buildInfoButton() {
		Cargo view = activeCargo();
		ItemStack info = new ItemStack(Material.PAPER);
		ItemMeta meta = info.getItemMeta();
		meta.displayName(line(Component.text(isFleetView() ? "Fleet Hold " + currentHold : "Cargo", NamedTextColor.GOLD)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Items: ", NamedTextColor.GRAY)
				.append(Component.text(view.getTotalItems() + " / " + view.getCapacity(), NamedTextColor.WHITE))));
		lore.add(line(Component.text("Slots: ", NamedTextColor.GRAY)
				.append(Component.text(view.getUsedSlots() + " / " + view.getSlots(), NamedTextColor.WHITE))));
		if (isFleetView()) {
			FleetHold hold = activeFleetHold();
			lore.add(line(Component.text("Locked-in value: ", NamedTextColor.GRAY)
					.append(Component.text(df.format(hold != null ? hold.getSnapshotValue() : 0), NamedTextColor.GOLD))));
		} else {
			lore.add(line(Component.text("Total sell value: ", NamedTextColor.GRAY)
					.append(Component.text(df.format(view.getTotalSellValue()), NamedTextColor.GOLD))));
		}
		meta.lore(lore);
		info.setItemMeta(meta);
		return info;
	}

	private static Component line(Component c) {
		return c.decoration(TextDecoration.ITALIC, State.FALSE);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		if (!SessionManager.requireGeneralPermission(p)) {
			e.setCancelled(true);
			p.closeInventory();
			return;
		}
		if (SessionManager.getSession(p) != null) {
			e.setCancelled(true);
			p.closeInventory();
			Util.displayError(p, "You can't manage cargo during a run!");
			return;
		}
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
			if (slot == backSlot) {
				click();
				new BukkitRunnable() {
					@Override
					public void run() {
						if (returnToCaravanMenu || NeoRogue.isLightweightMode()) NeoRogueAPI.openCaravanMenu(p);
						else new MainMenuInventory(p);
					}
				}.runTask(NeoRogue.inst());
				return;
			}
			if (multiHold && (slot == prevSlot || slot == nextSlot)) {
				int size = pd.getFleetSize();
				if (slot == nextSlot) currentHold = currentHold >= size ? 0 : currentHold + 1;
				else currentHold = currentHold <= 0 ? size : currentHold - 1;
				render();
				click();
				return;
			}
			if (multiHold && slot == holdInfoSlot) return;
			if (slot == collectSlot && pd.hasPendingFleetSales()) {
				double collected = pd.collectFleetEarnings();
				if (collected > 0) {
					p.sendMessage(Component.text("Collected ", NamedTextColor.GRAY)
							.append(Component.text(df.format(collected), NamedTextColor.GOLD))
							.append(Component.text(" from your fleet.", NamedTextColor.GRAY)));
				}
				render();
				click();
				return;
			}
			if (slot == lostCargoSlot && hasLostCargo()) {
				click();
				new BukkitRunnable() {
					@Override
					public void run() {
						new LostCargoInventory(p, pd);
					}
				}.runTask(NeoRogue.inst());
				return;
			}

			CargoItem item = slotToItem.get(slot);
			if (item != null) {
				// Withdraw: left = 1, shift-left = a stack. Ignore right clicks.
				if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) return;
				int amount = e.isShiftClick() ? item.createStack().getMaxStackSize() : 1;
				int removed = withdrawActive(item, amount);
				if (removed <= 0) return;
				giveOrDrop(item, removed);
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
				if (!pd.canDepositMaterial(item.getType())) {
					Util.displayError(p, "You don't have a permit to store that material!");
					return;
				}
				CargoItem cargoItem = CargoItem.fromItem(item).orElse(null);
				int added = cargoItem == null ? 0 : depositActive(cargoItem, item.getAmount());
				if (added <= 0) {
					Util.displayError(p, "That hold has no room for that!");
					return;
				}
				int remaining = item.getAmount() - added;
				e.setCurrentItem(remaining > 0 ? withAmount(item, remaining) : null);
				render();
				click();
				if (remaining > 0) Util.displayError(p, "That hold couldn't fit all of that!");
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
		if (!pd.canDepositMaterial(cursor.getType())) {
			Util.displayError(p, "You don't have a permit to store that material!");
			return;
		}
		CargoItem item = CargoItem.fromItem(cursor).orElse(null);
		int added = item == null ? 0 : depositActive(item, cursor.getAmount());
		if (added <= 0) {
			Util.displayError(p, "That hold has no room for that!");
			return;
		}
		int remaining = cursor.getAmount() - added;
		p.setItemOnCursor(remaining > 0 ? withAmount(cursor, remaining) : null);
		render();
		click();
		if (remaining > 0) Util.displayError(p, "That hold couldn't fit all of that!");
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
		if (!pd.canDepositMaterial(dragged.getType())) {
			Util.displayError(p, "You don't have a permit to store that material!");
			return;
		}
		CargoItem item = CargoItem.fromItem(dragged).orElse(null);
		int added = item == null ? 0 : depositActive(item, dragged.getAmount());
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
			Util.displayError(p, "That hold has no room for that!");
			return;
		}
		render();
		click();
		if (remaining > 0) Util.displayError(p, "That hold couldn't fit all of that!");
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		pd.saveCargoAsync();
	}

	private void giveOrDrop(CargoItem item, int amount) {
		ItemStack stack = item.createStack(amount);
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
