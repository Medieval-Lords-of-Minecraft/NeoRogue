package me.neoblade298.neorogue.player.inventory;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

import me.ascheladd.asheconomy.pricing.MaterialPrices;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.Cargo;
import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

// Withdraw-only GUI for a player's lost cargo (unsold run cargo that didn't fit back into the main
// cargo at run end). Items can only be taken out; nothing can be deposited. Shares the cargo limits.
public class LostCargoInventory extends CoreInventory {
	private static final DecimalFormat df = new DecimalFormat("#,##0.##");

	private final PlayerData pd;
	private final Cargo cargo;
	private final int infoSlot;
	private final java.util.HashMap<Integer, Material> slotToMaterial = new java.util.HashMap<Integer, Material>();

	public LostCargoInventory(Player p, PlayerData pd) {
		super(p, Bukkit.createInventory(p, computeSize(pd.getLostCargo()), Component.text("Lost Cargo", NamedTextColor.GOLD)));
		this.pd = pd;
		this.cargo = pd.getLostCargo();
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
		meta.displayName(line(Component.text("Lost Cargo", NamedTextColor.GOLD)));
		List<Component> lore = new ArrayList<Component>();
		lore.add(line(Component.text("Unsold cargo that didn't fit back into your cargo.", NamedTextColor.GRAY)));
		lore.add(line(Component.text("You can only withdraw items here.", NamedTextColor.GRAY)));
		lore.add(Component.empty());
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

		// Top (lost cargo) inventory: withdraw only.
		if (clicked == inv) {
			e.setCancelled(true);
			int slot = e.getSlot();
			if (slot == infoSlot) return;

			Material mat = slotToMaterial.get(slot);
			if (mat == null) return;
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

		// Bottom (player) inventory: block deposits (shift-click and double-click collect).
		if (clicked == p.getInventory()) {
			if (e.isShiftClick() || e.getClick() == ClickType.DOUBLE_CLICK) {
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		// Block any drag that touches the lost cargo inventory (no deposits allowed).
		for (int raw : e.getRawSlots()) {
			if (raw < inv.getSize()) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		pd.saveLostCargoAsync();
	}

	private void giveOrDrop(Material mat, int amount) {
		ItemStack stack = new ItemStack(mat, amount);
		Map<Integer, ItemStack> leftover = p.getInventory().addItem(stack);
		for (ItemStack left : leftover.values()) {
			p.getWorld().dropItemNaturally(p.getLocation(), left);
		}
	}

	private void click() {
		p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
	}
}
