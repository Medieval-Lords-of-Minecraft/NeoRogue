package me.neoblade298.neorogue.session;

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
	public static final int[] CHEAP_EQUIPS = new int[] { 18, 19, 20, 21, 22 },
		EXPENSIVE_EQUIPS = new int[] { 27, 28, 29, 30, 31 },
		CONSUMABLES = new int[] { 15, 16, 17 },
		GEMS = new int[] { 24, 25, 26 },
		ARTIFACTS = new int[] { 33, 34, 35 };
	public static final int SELL_PRICE = 25, REMOVE_CURSE_PRICE = 100,
		GOLD_ICON = 4, SELL_ICON = 0, PURIFY_ICON = 1, SPECTATE_ICON = 8;
	private PlayerSessionData data;
	private ShopContents shopItems;
	private Player spectator;
	
	public ShopInventory(PlayerSessionData data, ShopContents items) {
		super(
				data.getPlayer(),
				Bukkit.createInventory(data.getPlayer(), 36, Component.text("Shop", NamedTextColor.BLUE))
		);
		this.data = data;
		this.shopItems = items;
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		setupInventory();
	}
	
	public ShopInventory(PlayerSessionData data, ShopContents items, Player spectator) {
		super(
				spectator,
				Bukkit.createInventory(
						data.getPlayer(), 36,
						Component.text(data.getData().getDisplay() + "'s Shop", NamedTextColor.BLUE)
				)
		);
		this.data = data;
		this.shopItems = items;
		this.spectator = spectator;
		setupInventory();
	}

	// Call when reopening this inv without a constructor
	public void onOpenInventory() {
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		contents[GOLD_ICON] = CoreInventory.createButton(
				Material.GOLD_INGOT, Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)
		);
		contents[SELL_ICON] = CoreInventory.createButton(
				Material.GOLD_NUGGET, Component.text("Sell Items", NamedTextColor.RED),
				(TextComponent) NeoCore.miniMessage().deserialize(
						"Drag equipment here to sell it " + "for <yellow>" + SELL_PRICE + " coins</yellow>."
				), 250, NamedTextColor.GRAY
		);
		contents[PURIFY_ICON] = CoreInventory.createButton(
				Material.SUGAR, Component.text("Remove Curses", NamedTextColor.RED),
				(TextComponent) NeoCore.miniMessage().deserialize(
						"Drag cursed equipment here to remove it " + "in exchange for <yellow>" + REMOVE_CURSE_PRICE
								+ " coins</yellow>."
				), 250, NamedTextColor.GRAY
		);
		if (data.getSession().getParty().size() > 1)
			contents[SPECTATE_ICON] = CoreInventory
					.createButton(Material.SPYGLASS, Component.text("View other players' shops", NamedTextColor.GOLD));

		int idx = 0;
		for (int i : CHEAP_EQUIPS) {
			ShopItem si = shopItems.get(idx);
			contents[i] = si.getItem(data, idx);
			idx++;
		}
		for (int i : EXPENSIVE_EQUIPS) {
			ShopItem si = shopItems.get(idx);
			contents[i] = si.getItem(data, idx);
			idx++;
		}
		for (int i : CONSUMABLES) {
			ShopItem si = shopItems.get(idx);
			contents[i] = si.getItem(data, idx);
			idx++;
		}
		for (int i : GEMS) {
			ShopItem si = shopItems.get(idx);
			contents[i] = si.getItem(data, idx);
			idx++;
		}
		for (int i : ARTIFACTS) {
			ShopItem si = shopItems.get(idx);
			contents[i] = si.getItem(data, idx);
			idx++;
		}
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) {
				contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(""));
			}
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		Inventory iclicked = e.getClickedInventory();
		if (InventoryListener.getLowerInventory(p) == null) {
			InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		}
		PlayerSessionInventory pinv = (PlayerSessionInventory) InventoryListener.getLowerInventory(p);
		if (spectator != null) {
			e.setCancelled(true);
			return;
		}
		if (iclicked == null)
			return;
		
		// Clicked outside of shop inventory
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

		if (slot == SPECTATE_ICON && e.getCurrentItem() != null) {
			new SpectateSelectInventory(data.getSession(), p, data, true);
			return;
		}

		if (e.getCursor().getType().isAir() && slot >= 9) {
			ItemStack clicked = e.getCurrentItem();
			NBTItem nclicked = new NBTItem(clicked);
			int idx = nclicked.getInteger("idx");
			ShopItem shopItem = shopItems.get(idx);
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
			data.getSession().getInstance().updateBoardLines();
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
			contents[slot] = shopItem.getItem(data, idx);
			updateAll(contents);
			contents[GOLD_ICON] = CoreInventory.createButton(
					Material.GOLD_INGOT,
					Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)
			);
			inv.setContents(contents);
		}
		else {
			if (slot == SELL_ICON) {
				NBTItem nbti = new NBTItem(e.getCursor());
				Equipment eq = Equipment.get(nbti.getString("equipId"), false);
				if (eq.isCursed()) {
					Util.displayError(p, "Curses cannot be sold, they must be removed!");
					return;
				}
				data.addCoins(SELL_PRICE);
				inv.setItem(
						GOLD_ICON,
						CoreInventory.createButton(
								Material.GOLD_INGOT,
								Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)
						)
				);
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
				p.setItemOnCursor(null);
				data.getSession().broadcast(
						SharedUtil.color("<yellow>" + p.getName() + " </yellow>sold their ").append(eq.getHoverable())
								.append(Component.text("."))
				);
				
				data.getSession().getInstance().updateBoardLines();
				ItemStack[] contents = inv.getContents();
				updateAll(contents);
				inv.setContents(contents);
				pinv.clearHighlights();
			}
			else if (slot == PURIFY_ICON) {
				if (!data.hasCoins(REMOVE_CURSE_PRICE)) {
					Util.displayError(
							p,
							"You don't have enough coins! You need " + (REMOVE_CURSE_PRICE - data.getCoins()) + " more."
					);
					return;
				}

				if (e.getCursor() == null) {
					Util.displayError(p, "Drag the cursed item onto the purify option!");
					return;
				}
	
				data.addCoins(-REMOVE_CURSE_PRICE);
				NBTItem nbti = new NBTItem(e.getCursor());
				Equipment eq = Equipment.get(nbti.getString("equipId"), false);
				if (!eq.isCursed()) {
					Util.displayError(p, "Only cursed items may be removed this way!");
					return;
				}
				inv.setItem(GOLD_ICON, CoreInventory.createButton(Material.GOLD_INGOT,
						Component.text("You have " + data.getCoins() + " coins", NamedTextColor.YELLOW)));
				data.getSession().broadcast(
						SharedUtil.color("<yellow>" + p.getName() + " </yellow>purified their ")
								.append(eq.getHoverable()).append(Component.text("."))
				);
				eq.onPurify(data);
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
				p.setItemOnCursor(null);
				ItemStack[] contents = inv.getContents();
				updateAll(contents);
				inv.setContents(contents);
				pinv.clearHighlights();
				data.getSession().getInstance().updateBoardLines();
			}
		}
	}

	private void updateAll(ItemStack[] contents) {
		for (int i : CHEAP_EQUIPS) {
			updateSingle(contents[i]);
		}
		for (int i : EXPENSIVE_EQUIPS) {
			updateSingle(contents[i]);
		}
		for (int i : CONSUMABLES) {
			updateSingle(contents[i]);
		}
		for (int i : ARTIFACTS) {
			updateSingle(contents[i]);
		}
		for (int i : GEMS) {
			updateSingle(contents[i]);
		}
	}

	private void updateSingle(ItemStack item) {
		NBTItem nbti = new NBTItem(item);
		int idx = nbti.getInteger("idx");
		ShopItem si = shopItems.get(idx);
		si.update(data, item, false);
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
