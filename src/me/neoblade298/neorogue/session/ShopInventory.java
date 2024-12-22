package me.neoblade298.neorogue.session;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.EquipmentGlossaryInventory;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.player.inventory.SpectateSelectInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShopInventory extends CoreInventory {
	public static final int[] SLOT_ORDER = new int[] { 0, 2, 4, 6, 8, 9, 11, 13, 15, 17 };
	public static final int SELL_PRICE = 25, REMOVE_CURSE_PRICE = 100;
	private PlayerSessionData data;
	private ArrayList<ShopItem> shopItems;
	private Player spectator;
	
	public ShopInventory(PlayerSessionData data, ArrayList<ShopItem> items) {
		super(
				data.getPlayer(),
				Bukkit.createInventory(data.getPlayer(), 27, Component.text("Shop", NamedTextColor.BLUE))
		);
		this.data = data;
		this.shopItems = items;
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		setupInventory();
	}
	
	public ShopInventory(PlayerSessionData data, ArrayList<ShopItem> items, Player spectator) {
		super(
				spectator,
				Bukkit.createInventory(
						data.getPlayer(), 27,
						Component.text(data.getData().getDisplay() + "'s Shop", NamedTextColor.BLUE)
				)
		);
		this.data = data;
		this.shopItems = items;
		this.spectator = spectator;
		setupInventory();
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < shopItems.size(); i++) {
			contents[SLOT_ORDER[i]] = shopItems.get(i).getItem(data);
		}
		contents[22] = CoreInventory.createButton(
				Material.GOLD_NUGGET, Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)
		);
		contents[18] = CoreInventory.createButton(
				Material.GOLD_NUGGET, Component.text("Sell Items", NamedTextColor.RED),
				(TextComponent) NeoCore.miniMessage().deserialize(
						"Drag equipment here to sell it " + "for <yellow>" + SELL_PRICE + " coins</yellow>."
				), 250, NamedTextColor.GRAY
		);
		contents[19] = CoreInventory.createButton(
				Material.SUGAR, Component.text("Remove Curses", NamedTextColor.RED),
				(TextComponent) NeoCore.miniMessage().deserialize(
						"Drag cursed equipment here to remove it " + "in exchange for <yellow>" + REMOVE_CURSE_PRICE
								+ " coins</yellow>."
				), 250, NamedTextColor.GRAY
		);
		if (data.getSession().getParty().size() > 1)
			contents[20] = CoreInventory
					.createButton(Material.SPYGLASS, Component.text("View other players' shops", NamedTextColor.GOLD));
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
		if (spectator != null) {
			e.setCancelled(true);
			return;
		}
		if (iclicked == null)
			return;
		if (iclicked.getType() != InventoryType.CHEST) {
			if (e.getCurrentItem() == null)
				return;
			if (e.isShiftClick()) {
				e.setCancelled(true);
				return;
			}
			ItemStack item = e.getCurrentItem();
			NBTItem nbti = new NBTItem(item);
			// Only allow picking up equipment
			if (!nbti.getKeys().contains("equipId")) {
				e.setCancelled(true);
			}
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			return;
		}
		e.setCancelled(true);
		if (e.getCurrentItem() == null)
			return; // Must have clicked on an item in the inv
		if (e.getCurrentItem().getType() == Material.BARRIER) {
			Util.displayError(p, "You've already purchased this item!");
			return;
		}
		int slot = e.getSlot();

		if (slot < 18) {
			ShopItem shopItem = null;
			for (int i = 0; i < SLOT_ORDER.length; i++) {
				if (SLOT_ORDER[i] == slot) {
					shopItem = shopItems.get(i);
					break;
				}
			}
			int price = shopItem.getPrice();
			
			if (e.isRightClick()) {
				new EquipmentGlossaryInventory(p, shopItem.getEquipment(), this);
				return;
			}
			
			if (!data.hasCoins(price)) {
				Util.displayError(p, "You don't have enough coins! You need " + (price - data.getCoins()) + " more.");
				return;
			}
			shopItem.setPurchased(true);
			
			data.addCoins(-price);
			data.giveEquipment(
					shopItem.getEquipment(),
					SharedUtil.color("You spent <yellow>" + price + " coins</yellow> to purchase a(n) "),
					SharedUtil.color(
							"<yellow>" + p.getName() + "</yellow> spent <yellow>" + price
									+ " coins</yellow> to purchase a(n) "
					)
			);
			p.playSound(p, Sound.ENTITY_WANDERING_TRADER_YES, 1F, 1F);
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			
			ItemStack[] contents = inv.getContents();
			contents[slot] = shopItem.getItem(data);
			for (ShopItem si : shopItems) {
				si.updateLore(data, contents[si.getSlot()], false);
			}
			contents[22] = CoreInventory.createButton(
					Material.GOLD_NUGGET,
					Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)
			);
			inv.setContents(contents);
		} else {
			if (slot == 20 && e.getCurrentItem() != null) {
				new SpectateSelectInventory(data.getSession(), p, data, true);
				return;
			}
			if (e.getCursor().getType().isAir())
				return;
			if (slot == 18) {
				NBTItem nbti = new NBTItem(e.getCursor());
				Equipment eq = Equipment.get(nbti.getString("equipId"), false);
				if (eq.isCursed()) {
					Util.displayError(p, "Curses cannot be sold, they must be removed!");
					return;
				}
				data.addCoins(SELL_PRICE);
				inv.setItem(
						22,
						CoreInventory.createButton(
								Material.GOLD_NUGGET,
								Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)
						)
				);
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
				p.setItemOnCursor(null);
				data.getSession().broadcast(
						SharedUtil.color("<yellow>" + p.getName() + " </yellow>sold their ").append(eq.getHoverable())
								.append(Component.text("."))
				);
				
				ItemStack[] contents = inv.getContents();
				for (ShopItem si : shopItems) {
					si.updateLore(data, contents[si.getSlot()], false);
				}
				inv.setContents(contents);
				pinv.clearHighlights();
			} else if (slot == 19) {
				if (!data.hasCoins(REMOVE_CURSE_PRICE)) {
					Util.displayError(
							p,
							"You don't have enough coins! You need " + (REMOVE_CURSE_PRICE - data.getCoins()) + " more."
					);
					return;
				}

				data.addCoins(-REMOVE_CURSE_PRICE);
				NBTItem nbti = new NBTItem(e.getCursor());
				Equipment eq = Equipment.get(nbti.getString("equipId"), false);
				if (!eq.isCursed()) {
					Util.displayError(p, "Only cursed items may be removed this way!");
					return;
				}
				data.getSession().broadcast(
						SharedUtil.color("<yellow>" + p.getName() + " </yellow>purified their ")
								.append(eq.getHoverable()).append(Component.text("."))
				);
				eq.onPurify(data);
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
				p.setItemOnCursor(null);
				ItemStack[] contents = inv.getContents();
				for (ShopItem si : shopItems) {
					si.updateLore(data, contents[si.getSlot()], false);
				}
				inv.setContents(contents);
				pinv.clearHighlights();
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		p.playSound(p, Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
