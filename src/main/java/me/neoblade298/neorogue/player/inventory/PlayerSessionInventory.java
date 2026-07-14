package me.neoblade298.neorogue.player.inventory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.inventories.CorePlayerInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.instances.EditInventoryInstance;
import me.neoblade298.neorogue.session.instances.NodeSelectInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class PlayerSessionInventory extends CorePlayerInventory implements ShiftClickableInventory {
	private static final int[] ARMOR = new int[] { 18, 19, 20, 21 };
	private static final int[] ACCESSORIES = new int[] { 22, 23, 24, 25, 26 };
	private static final int[] HOTBAR = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] FILLER = new int[] { 11, 12, 14, 15, 16, 17, 34 };
	private static final int[] KEYBINDS = new int[] { 27, 28, 29, 30, 31, 32, 33 };
	public static final int STATS = 9, TRASH = 17, STORAGE = 10, OFFHAND = 35, ARTIFACTS = 13, SEE_OTHERS = 11, MAP = 40, SETTINGS = 12, REFORGES = 15, LEAVE = 16;
	private static HashMap<Integer, EquipSlot> slotTypes = new HashMap<Integer, EquipSlot>();
	private static final DecimalFormat df = new DecimalFormat("#.##");

	private static final TextComponent instruct = Component.text("Drag a weapon, ability, or consumable",
			NamedTextColor.GRAY);
	private static final TextComponent instruct2 = Component.text("here to bind it!", NamedTextColor.GRAY);
	private static final TextComponent statsText = Component.text("Your stats:", NamedTextColor.GOLD);

	private HashSet<Integer> highlighted = new HashSet<Integer>();
	private PlayerSessionData data;

	public PlayerSessionInventory(PlayerSessionData data) {
		super(data.getPlayer());
		this.data = data;

		// If you open your inv and try to pick the map up, put map back into offhand
		if (data.isViewingMap()) {
			data.stopViewingMap();
		}
		data.trigger(SessionTrigger.OPEN_SESSION_INVENTORY, null);
	}

	public PlayerSessionInventory(Player viewer, PlayerSessionData data) {
		super(viewer);
		this.data = data;
	}

	public static void setupInventory(Inventory inv, PlayerSessionData data) {
		setupInventory(inv, data, false);
	}
	
	// Use offset when setting up another player's inventory for spectating (0 index is at top of inv for spectate, not bottom)
	public static void setupInventory(Inventory inv, PlayerSessionData data, boolean isSpectating) {
		int offset = isSpectating ? 27 : 0;
		ItemStack[] contents = inv.getContents();

		// Import data from session data
		int iter = 0;
		for (int i : ARMOR) {
			slotTypes.put(i, EquipSlot.ARMOR);
			if (iter >= data.getArmorSlots()) {
				contents[(i + offset) % inv.getSize()] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
				iter++;
				continue;
			}
			SessionEquipment a = data.getSessionEquipment(EquipSlot.ARMOR)[iter];
			contents[(i + offset) % inv.getSize()] = a != null ? addNbt(a.getItem(), a.getEquipment().getId(), a.getEquipment().isUpgraded(), iter) : createArmorIcon(iter);
			iter++;
		}

		iter = 0;
		for (int i : ACCESSORIES) {
			slotTypes.put(i, EquipSlot.ACCESSORY);
			if (iter >= data.getAccessorySlots()) {
				contents[(i + offset) % inv.getSize()] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
				iter++;
				continue;
			}
			SessionEquipment a = data.getSessionEquipment(EquipSlot.ACCESSORY)[iter];
			contents[(i + offset) % inv.getSize()] = a != null ? addNbt(a.getItem(), a.getEquipment().getId(), a.getEquipment().isUpgraded(), iter) : createAccessoryIcon(iter);
			iter++;
		}

		for (KeyBind bind : KeyBind.values()) {
			int i = (bind.getInventorySlot() + offset) % inv.getSize();
			slotTypes.put(bind.getInventorySlot(), EquipSlot.KEYBIND);
			SessionEquipment eq = data.getSessionEquipment(EquipSlot.KEYBIND)[bind.getDataSlot()];
			if (eq == null && data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
				contents[i] = createMaxedAbilitiesIcon(data, bind.getDataSlot(), bind);
				continue;
			}
			contents[i] = eq != null
					? addNbt(addBindLore(eq.getItem(), i, bind.getDataSlot()), eq.getEquipment().getId(),
							eq.getEquipment().isUpgraded(), bind.getDataSlot())
					: addNbt(bind.getItem(), bind.getDataSlot());
		}

		slotTypes.put(OFFHAND, EquipSlot.OFFHAND);
		SessionEquipment o = data.getSessionEquipment(EquipSlot.OFFHAND)[0];
		contents[(OFFHAND + offset) % inv.getSize()] = o != null ? addNbt(o.getItem(), o.getEquipment().getId(), o.getEquipment().isUpgraded(), 0) : createOffhandIcon();

		for (int i : HOTBAR) {
			slotTypes.put(i, EquipSlot.HOTBAR);
			SessionEquipment eq = data.getSessionEquipment(EquipSlot.HOTBAR)[i];
			if (eq == null && data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
				contents[(i + offset) % inv.getSize()] = createMaxedAbilitiesIcon(data, i, null);
				continue;
			}
			contents[(i + offset) % inv.getSize()] = eq != null ? addNbt(addBindLore(eq.getItem(), i, i), eq.getEquipment().getId(), eq.getEquipment().isUpgraded(), i)
					: createHotbarIcon(i);
		}

		for (int i : FILLER) {
			contents[(i + offset) % inv.getSize()] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
		}

		contents[(STATS + offset) % inv.getSize()] = createStatsIcon(data);
		contents[(STORAGE + offset) % inv.getSize()] = CoreInventory.createButton(Material.ENDER_CHEST, Component.text("Storage", NamedTextColor.GOLD));
		contents[(TRASH + offset) % inv.getSize()] = addNbt(CoreInventory.createButton(Material.HOPPER,
				Component.text("Trash", NamedTextColor.GOLD), "Drag items here to trash them!", 250, NamedTextColor.GRAY),
				0);
		contents[(SETTINGS + offset) % inv.getSize()] = createSettingsIcon(data);

		contents[(ARTIFACTS + offset) % inv.getSize()] = addNbt(
				CoreInventory.createButton(Material.NETHER_STAR, Component.text("Artifacts", NamedTextColor.GOLD),
						"Click here to view all your artifacts!", 250, NamedTextColor.GRAY),
				0);
		if (data.getSession().getParty().size() > 1)
			contents[(SEE_OTHERS + offset) % inv.getSize()] = CoreInventory.createButton(Material.SPYGLASS, Component.text("View other players", NamedTextColor.GOLD));

		int reforgeCount = 0;
		for (PlayerSessionData.ReforgePairData pair : data.computeAvailableReforges()) {
			for (Equipment r : pair.getResults()) {
				if (r != null) reforgeCount++;
			}
		}
		if (reforgeCount > 0) {
			contents[(REFORGES + offset) % inv.getSize()] = createReforgesIcon(reforgeCount);
		}

		if (!(data.getSession().getInstance() instanceof NodeSelectInstance) && !isSpectating) {
			contents[(MAP + offset) % inv.getSize()] = CoreInventory.createButton(Material.FILLED_MAP, Component.text("Node Map", NamedTextColor.GOLD));
			MapMeta meta = (MapMeta) contents[(MAP + offset) % inv.getSize()].getItemMeta();
			MapView map = Bukkit.getMap(EditInventoryInstance.MAP_ID);
			meta.setMapView(map);
			contents[(MAP + offset) % inv.getSize()].setItemMeta(meta);
		}

		if (!isSpectating) {
			contents[(LEAVE + offset) % inv.getSize()] = createLeaveIcon();
		}
		inv.setContents(contents);
	}

	private static ItemStack createLeaveIcon() {
		return CoreInventory.createButton(Material.COMPASS, Component.text("Save & Quit", NamedTextColor.RED),
				"Saves and quit your run! Only the host can reload it.", 250, NamedTextColor.GRAY);
	}

	private static ItemStack createArmorIcon(int dataSlot) {
		return addNbt(CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE,
				Component.text("Armor Slot", NamedTextColor.YELLOW), "Drag an armor here to equip it!", 250,
				NamedTextColor.GRAY), dataSlot);
	}

	private static ItemStack createMaxedAbilitiesIcon(PlayerSessionData data, int dataSlot, @Nullable KeyBind bind) {
		ItemStack item = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text("Out of abilities", NamedTextColor.RED),
				Component.text("You have equipped " + data.getAbilitiesEquipped() + " / " +data.getMaxAbilities() + " abilities", NamedTextColor.GRAY));

				if (bind != null) {
					List<Component> lore = item.lore();
					lore.add(Component.text("Bound to ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE).append(bind.getDisplay()));
					item.lore(lore);
				}
				NBTItem nbti = new NBTItem(item);
				nbti.setBoolean("maxed", true);
				nbti.setBoolean("openSlot", true);
				return addNbt(nbti.getItem(), dataSlot);
	}

	public static ItemStack createIcon(EquipSlot slot, int dataSlot) {
		switch (slot) {
		case ACCESSORY:
			return createAccessoryIcon(dataSlot);
		case ARMOR:
			return createArmorIcon(dataSlot);
		case HOTBAR:
			return createHotbarIcon(dataSlot);
		case KEYBIND:
			KeyBind kb = KeyBind.getBindFromData(dataSlot);
			return addNbt(kb.getItem(), kb.getDataSlot());
		case OFFHAND:
			return createOffhandIcon();
		default:
			return null;
		}
	}

	public static ItemStack createAccessoryIcon(int dataSlot) {
		return addNbt(CoreInventory.createButton(Material.LIME_STAINED_GLASS_PANE,
				Component.text("Accessory Slot", NamedTextColor.GREEN), "Drag an accessory here to equip it!", 250,
				NamedTextColor.GRAY), dataSlot);
	}

	public static ItemStack createOffhandIcon() {
		return addNbt(CoreInventory.createButton(Material.WHITE_STAINED_GLASS_PANE,
				Component.text("Offhand Slot", NamedTextColor.WHITE), "Drag an offhand here to equip it!", 250,
				NamedTextColor.GRAY), 0);
	}

	private static ItemStack createReforgesIcon(int count) {
		ItemStack item = CoreInventory.createButton(Material.ANVIL,
				Component.text("Available Reforges", NamedTextColor.GOLD),
				"Click to view " + count + " available reforge" + (count != 1 ? "s" : "") + "!", 250, NamedTextColor.GRAY);
		item.setAmount(Math.min(count, 64));
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.UNBREAKING, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		return item;
	}

	private static ItemStack createHotbarIcon(int dataSlot) {
		TextComponent bound = Component.text("Bound to Hotbar #" + (dataSlot + 1), NamedTextColor.YELLOW);
		return addNbt(CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE,
				Component.text("Hotbar Slot", NamedTextColor.RED), bound, instruct, instruct2), dataSlot);
	}

	private static ItemStack createStatsIcon(PlayerSessionData data) {
		TextComponent cls = Component.text("Class: ", NamedTextColor.GOLD)
				.append(Component.text(data.getPlayerClass().getDisplay(), NamedTextColor.WHITE));
		TextComponent health = Component.text("Health: ", NamedTextColor.GOLD)
				.append(Component.text(df.format(data.getHealth()) + " / " + df.format(data.getMaxHealth()), NamedTextColor.WHITE));
		TextComponent mana = Component.text("Max Mana: ", NamedTextColor.GOLD)
				.append(Component.text(df.format(data.getMaxMana()), NamedTextColor.WHITE));
		TextComponent stamina = Component.text("Max Stamina: ", NamedTextColor.GOLD)
				.append(Component.text(df.format(data.getMaxStamina()), NamedTextColor.WHITE));
		TextComponent mr = Component.text("Mana Regen: ", NamedTextColor.GOLD)
				.append(Component.text(df.format(data.getManaRegen()), NamedTextColor.WHITE));
		TextComponent sr = Component.text("Stamina Regen: ", NamedTextColor.GOLD)
				.append(Component.text(df.format(data.getStaminaRegen()), NamedTextColor.WHITE));
		TextComponent coins = Component.text("Coins: ", NamedTextColor.GOLD)
				.append(Component.text(data.getCoins(), NamedTextColor.WHITE));
		return CoreInventory.createButton(Material.ARMOR_STAND, statsText, cls, health, mana, stamina, mr, sr, coins);
	}

	private static ItemStack createSettingsIcon(PlayerSessionData data) {
		Session s = data.getSession();
		int notoriety = s.getNotoriety();
		ItemStack item = new ItemStack(Material.OMINOUS_BOTTLE);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Notoriety: ", NamedTextColor.GOLD)
				.append(Component.text(notoriety + " / " + s.getMaxNotoriety(), NamedTextColor.WHITE))
				.decoration(TextDecoration.ITALIC, false));
		java.util.ArrayList<Component> lore = new java.util.ArrayList<>();
		lore.add(Component.text("XP Bonus: ", NamedTextColor.GRAY)
				.append(Component.text("+" + s.getNotorietyXpBonusPercent() + "%", NamedTextColor.GREEN))
				.decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text("Money Bonus: ", NamedTextColor.GRAY)
				.append(Component.text("+" + s.getNotorietyMoneyBonusPercent() + "%", NamedTextColor.GREEN))
				.decoration(TextDecoration.ITALIC, false));
		if (notoriety > 0) {
			lore.add(Component.empty());
			lore.add(Component.text("Active Effects:", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
			for (int i = 0; i < notoriety; i++) {
				java.util.ArrayList<net.kyori.adventure.text.TextComponent> headerLines = NotorietySetting.settings.get(i).getHeader();
				for (int j = 0; j < headerLines.size(); j++) {
					String prefix = j == 0 ? "- " : "  ";
					lore.add(Component.text(prefix, NamedTextColor.DARK_GRAY)
							.append(headerLines.get(j))
							.decoration(TextDecoration.ITALIC, false));
				}
			}
		}
		meta.lore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		ItemStack cursor = e.getCursor();
		ItemStack clicked = e.getCurrentItem();
		int slot = e.getSlot();
		if (slot >= 36 || slot < 0) {
			e.setCancelled(true);
			return;
		}
		if (cursor.getType().isAir() && (clicked == null || clicked.getType().isAir())) return;

		NBTItem ncursor = !cursor.getType().isAir() ? new NBTItem(cursor) : null;
		NBTItem nclicked = clicked != null && !clicked.getType().isAir() ? new NBTItem(clicked) : null;
		Player p = (Player) e.getWhoClicked();

		if (slot == TRASH && !cursor.getType().isAir()) {
			e.setCancelled(true);
			Equipment eq = Equipment.get(ncursor.getString("equipId"), false);
			if (eq == null) {
				Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " tried to trash non-equipment item: " + cursor);
				return;
			}
			if (eq.isCursed()) {
				Util.displayError(p, "You can't trash cursed items!");
				return;
			}
			SessionEquipment[] storageSnapshot = null;
			CoreInventory upper = InventoryListener.getUpperInventory(p);
			if (upper instanceof StorageInventory) {
				storageSnapshot = ((StorageInventory) upper).getLiveStorageSnapshot();
			}
			String restriction = data.getRemovalRestriction(eq, storageSnapshot, false, "trash");
			if (restriction != null) {
				Util.displayError(p, restriction);
				return;
			}
			clearHighlights();
			p.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F);
			p.setItemOnCursor(null);
			return;
		}
		else if (slot == ARTIFACTS) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new ArtifactsInventory(data);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == REFORGES) {
			e.setCancelled(true);
			if (clicked != null && clicked.getType() == Material.ANVIL) {
				new BukkitRunnable() {
					public void run() {
						handleInventoryClose();
						new AvailableReforgesInventory(data);
					}
				}.runTask(NeoRogue.inst());
			}
			return;
		}
		else if (slot == SEE_OTHERS && data.getSession().getParty().size() > 1) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new SpectateSelectInventory(data.getSession(), p, data, false);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == MAP) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new NodeMapInventory(p, data.getSession());
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == STORAGE) {
			e.setCancelled(true);
			
			if (cursor.getType().isAir()) {
				new BukkitRunnable() {
					public void run() {
						handleInventoryClose();
						new StorageInventory(data);
					}
				}.runTask(NeoRogue.inst());
			}
			// Should not be usable if the storage inventory is open
			else if (!(InventoryListener.getUpperInventory(p) instanceof StorageInventory)) {
				Equipment eq = Equipment.get(ncursor.getString("equipId"), ncursor.getBoolean("isUpgraded"));
				if (eq.isCursed()) {
					displayError("You can't unequip cursed items!", false);
					return;
				}
				SessionEquipment se = SessionEquipment.fromItem(cursor);
				p.setItemOnCursor(null);
				data.sendToStorage(se != null ? se : new SessionEquipment(eq));
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F);
				clearHighlights();
			}
			return;
		}
		else if (slot == LEAVE) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					SessionManager.leaveSession(p);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		InventoryAction action = e.getAction();
		if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.COLLECT_TO_CURSOR) {
			e.setCancelled(true);
			return;
		}
		else if (action == InventoryAction.DROP_ONE_SLOT || action == InventoryAction.DROP_ALL_SLOT) {
			handleInventoryDrop(e);
			return;
		}

		// If right click with empty hand, open glossary
		if (e.isRightClick() && nclicked.hasTag("equipId") && cursor.getType().isAir()) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					handleInventoryClose();
					new EquipmentGlossaryInventory(p, Equipment.get(nclicked.getString("equipId"), false), null);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		
		// Shift click out logic
		if (e.isShiftClick() && clicked != null) {
			EquipSlot type = slotTypes.get(slot);
			e.setCancelled(true);
			if (!nclicked.hasTag("equipId")) return;
			CoreInventory upper = InventoryListener.getUpperInventory(p);
			if (upper == null) return;
			if (upper instanceof ShiftClickableInventory) {
				Equipment eq = Equipment.get(nclicked.getString("equipId"), false);
				if (eq.isCursed()) {
					displayError("You can't unequip cursed items!", false);
					return;
				}
				ShiftClickableInventory sci = (ShiftClickableInventory) upper;
				if (!sci.canShiftClickIn(clicked)) return;
				if (isBindable(type)) clicked = removeBindLore(clicked);
				removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
				sci.handleShiftClickIn(e, clicked);

				if (eq.getType() == EquipmentType.ABILITY && data.getAbilitiesEquipped() == data.getMaxAbilities() - 1) {
					setupInventory(p.getInventory(), data);
				}
			}
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
			return;
		}

		if (cursor.getType().isAir() && !clicked.getType().isAir()) {
			// Only allow picking up equipment
			if (!nclicked.hasTag("equipId")) {
				e.setCancelled(true);
			}
			// Remove gear
			else {
				Equipment eq = Equipment.get(nclicked.getString("equipId"), false);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
				e.setCancelled(true);
				EquipSlot type = slotTypes.get(slot);
				removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
				setHighlights(eq.getType());
				if (isBindable(type)) clicked = removeBindLore(clicked);
				p.setItemOnCursor(clicked);
			}
		}

		// Swap an item with another item
		else if (!cursor.getType().isAir() && clicked != null) {
			e.setCancelled(true);

			String eqId = ncursor.getString("equipId");
			String eqedId = nclicked.getString("equipId");
			Equipment eq = Equipment.get(eqId, ncursor.getBoolean("isUpgraded"));
			Equipment eqed = Equipment.get(eqedId, nclicked.getBoolean("isUpgraded"));

			// Reforged item check
			Equipment[] reforgePair = Equipment.resolveReforgePair(eq, eqed);
			if (reforgePair != null) {
				if (!Equipment.canReforge(reforgePair[0], reforgePair[1], data.getSession())) {
					String msg = NotorietySetting.REFORGE_REQUIRES_BOTH.isActive(data.getSession())
							? "Both items must be upgraded to reforge!"
							: "At least one of the items must be upgraded to reforge!";
					displayError(msg, false);
					return;
				}
				handleReforge(e, reforgePair[0], reforgePair[1], slot, nclicked);
				return;
			}

			// Wildcard reforge check (e.g. Transmutation): reforge any item into any of its results
			Equipment[] wildcardPair = Equipment.resolveWildcardReforge(eq, eqed);
			if (wildcardPair != null) {
				handleWildcardReforge(e, wildcardPair[0], wildcardPair[1], slot, nclicked);
				return;
			}

			if (!nclicked.hasTag("dataSlot")) return;
			if (eq.getType() == EquipmentType.ABILITY && (eqed == null || eqed.getType() != EquipmentType.ABILITY)
					&& !data.canEquipAbility()) {
				displayError("You can only equip " + data.getMaxAbilities() + " abilities!", true);
				return;
			}

			EquipSlot type = slotTypes.get(slot);
			if (!eq.canEquip(type)) {
				displayError("You can't equip this item in this slot!", false);
				return;
			}

			// Not swapping equipment, remove the cursor item
			boolean isSwapping = nclicked.hasTag("equipId");
			if (!isSwapping) {
				p.setItemOnCursor(null);
			}
			// If swapping equipment with equipment, remove that equipment
			else {
				if (isBindable(type)) clicked = removeBindLore(clicked);
				clearHighlights();
				data.removeEquipment(type, nclicked.getInteger("dataSlot"));
				p.setItemOnCursor(clicked);
				setHighlights(Equipment.get(nclicked.getString("equipId"), false).getType());
			}

			// Place equipment into clicked slot, preserving session metadata (durability, etc.) from the item
			SessionEquipment placed = SessionEquipment.fromItem(cursor);
			data.setEquipment(type, nclicked.getInteger("dataSlot"), placed != null ? placed : new SessionEquipment(eq));
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
			if (isBindable(type))
				cursor = addBindLore(cursor, slot, nclicked.getInteger("dataSlot"));
			inv.setItem(slot, addNbt(cursor, nclicked.getInteger("dataSlot")));
			if (!isSwapping) clearHighlights();
		}
		else {
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
		}
	}

	private void handleReforge(InventoryClickEvent e, Equipment primary, Equipment secondary, int slot, NBTItem nclicked) {
		new BukkitRunnable() {
			public void run() {
				p.setItemOnCursor(null);
				EquipSlot type = slotTypes.get(slot);
				removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
				inv.setItem(slot, iconFromEquipSlot(type, slot));
				handleInventoryClose();
				new ReforgeOptionsInventory(data, primary, secondary);
			}
		}.runTask(NeoRogue.inst());
		return;
	}

	private void handleWildcardReforge(InventoryClickEvent e, Equipment target, Equipment wildcard, int slot, NBTItem nclicked) {
		new BukkitRunnable() {
			public void run() {
				p.setItemOnCursor(null);
				EquipSlot type = slotTypes.get(slot);
				removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
				inv.setItem(slot, iconFromEquipSlot(type, slot));
				handleInventoryClose();
				new WildcardReforgeInventory(data, target, wildcard);
			}
		}.runTask(NeoRogue.inst());
		return;
	}
	
	public void clearHighlights() {
		ItemStack[] contents = inv.getContents();
		for (int i : highlighted) {
			if (contents[i].getType() != Material.GLASS_PANE) continue;
			contents[i] = iconFromEquipSlot(slotTypes.get(i), i);
		}
		highlighted.clear();
		inv.setContents(contents);
	}
	
	public void setHighlights(EquipmentType type) {
		ItemStack[] contents = inv.getContents();
		for (int[] slots : arrayFromEquipSlot(type)) {
			for (int s : slots) {
				ItemStack iter = contents[s];
				NBTItem nbti = new NBTItem(iter);

				if (nbti.hasTag("equipId") || !nbti.hasTag("openSlot")) continue;
				if (type == EquipmentType.ABILITY && !data.canEquipAbility()) {
					int dataSlot = nbti.getInteger("dataSlot");
					boolean isBind = KeyBind.isKeybindSlot(s);
					createMaxedAbilitiesIcon(data, dataSlot, isBind ? KeyBind.getBindFromSlot(s) : null);
				}
				else {
					contents[s] = iter.withType(Material.GLASS_PANE);
					highlighted.add(s);
				}

			}
		}
		inv.setContents(contents);
	}
	
	public AutoEquipResult attemptAutoEquip(EquipmentType type) {
		ItemStack[] contents = inv.getContents();
		int equipSlotIdx = 0;
		for (int[] slots : arrayFromEquipSlot(type)) {
			for (int s : slots) {
				ItemStack iter = contents[s];
				if (iter.getType() != Material.BLACK_STAINED_GLASS_PANE && iter.getType().name().endsWith("PANE")) {
					return new AutoEquipResult(type.getSlots()[equipSlotIdx], s);
				}
			}
			equipSlotIdx++;
		}
		return null;
	}
	
	public class AutoEquipResult {
		protected int slot;
		protected EquipSlot es;
		protected AutoEquipResult(EquipSlot es, int slot) {
			this.slot = slot;
			this.es = es;
		}
	}
	
	@Override
	public void handleShiftClickIn(InventoryClickEvent ev, ItemStack item) {
		NBTItem nbti = new NBTItem(item);
		Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
		AutoEquipResult result = attemptAutoEquip(eq.getType());
		ItemStack autoItem = inv.getItem(result.slot);
		NBTItem nauto = new NBTItem(autoItem);
		SessionEquipment placed = SessionEquipment.fromItem(item);
		data.setEquipment(result.es, nauto.getInteger("dataSlot"), placed != null ? placed : new SessionEquipment(eq));
		p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
		if (isBindable(result.es)) item = addBindLore(item, result.slot, nauto.getInteger("dataSlot"));
		inv.setItem(result.slot, addNbt(item, nauto.getInteger("dataSlot")));

		if (eq.getType() == EquipmentType.ABILITY && !data.canEquipAbility()) {
			// Lazy, just re-setup entire inventory
			setupInventory(p.getInventory(), data);
		}
	}
	
	@Override
	public boolean canShiftClickIn(ItemStack item) {
		NBTItem nbti = new NBTItem(item);
		Equipment eq = Equipment.get(nbti.getString("equipId"), nbti.getBoolean("isUpgraded"));
		switch (eq.getType()) {
			case ABILITY:
				if (!data.canEquipAbility()) {
					displayError("You can't equip any more abilities!", false);
					return false;
				}
				break;
			case ARMOR:
				if (data.getArmorEquipped() >= data.getArmorSlots()) {
					displayError("You can't equip any more armor!", false);
					return false;
				}
				break;
			case ACCESSORY:
				if (data.getAccessoriesEquipped() >= data.getAccessorySlots()) {
					displayError("You can't equip any more accessories!", false);
					return false;
				}
				break;
			default:
		}
		AutoEquipResult result = attemptAutoEquip(eq.getType());
		return result != null;
	}

	private void handleInventoryDrop(InventoryClickEvent e) {
		ItemStack clicked = e.getCurrentItem();
		int slot = e.getRawSlot();

		if (clicked.getType().isAir()) return;
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		NBTItem nclicked = new NBTItem(clicked);

		if (!nclicked.hasTag("equipId")) {
			e.setCancelled(true);
			return;
		}
		else {
			e.setCancelled(true);
			EquipSlot type = slotTypes.get(slot);
			if (isBindable(type)) clicked = removeBindLore(clicked);
			p.getWorld().dropItem(p.getLocation(), clicked).setPickupDelay(40);
			removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		handleInventoryClose();
	}
	
	public void handleInventoryClose() {
		clearHighlights();
		InventoryListener.unregisterPlayerInventory(p);
		
		if (p.getItemOnCursor().getType().isAir()) return;
		ItemStack clicked = p.getItemOnCursor();
		NBTItem nclicked = new NBTItem(clicked);
		data.giveEquipmentSilent(Equipment.get(nclicked.getString("equipId"), nclicked.getBoolean("isUpgraded")));
		p.setItemOnCursor(null);
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private static ItemStack addNbt(ItemStack item, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("dataSlot", dataSlot);
		nbti.setBoolean("openSlot", true); // Differentiates with available equippable slots and just empty panes in inventory
		return nbti.getItem();
	}

	private static ItemStack addNbt(ItemStack item, String equipId, boolean isUpgraded, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", equipId);
		nbti.setInteger("dataSlot", dataSlot);
		nbti.setBoolean("isUpgraded", isUpgraded);
		nbti.setBoolean("openSlot", true);
		return nbti.getItem();
	}

	private void removeEquipment(EquipSlot type, int dataSlot, int invSlot, Inventory inv) {
		ItemStack icon = createIcon(type, dataSlot);
		data.removeEquipment(type, dataSlot);
		inv.setItem(invSlot, icon);
	}

	private static ItemStack addBindLore(ItemStack item, int invSlot, int dataSlot) {
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		EquipSlot slotType = slotTypes.get(invSlot);
		if (slotType == EquipSlot.KEYBIND) {
			lore.add(1,
					Component.text("Bound to ", NamedTextColor.YELLOW)
							.append(KeyBind.getBindFromSlot(invSlot - 18).getDisplay())
							.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		else if (slotType == EquipSlot.HOTBAR) {
			lore.add(1, Component.text("Bound to Hotbar #" + (dataSlot + 1), NamedTextColor.YELLOW)
					.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private static ItemStack removeBindLore(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		lore.remove(1);
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private void displayError(String error, boolean closeInventory) {
		p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.7F);
		Util.msgRaw(p, error);
		if (closeInventory) p.closeInventory();
	}

	private boolean isBindable(EquipSlot type) {
		return type == EquipSlot.KEYBIND || type == EquipSlot.HOTBAR;
	}

	public PlayerSessionData getData() {
		return data;
	}
	
	private ItemStack iconFromEquipSlot(EquipSlot es, int slot) {
		switch (es) {
		case ACCESSORY: return createAccessoryIcon(slot - 22);
		case ARMOR: return createArmorIcon(slot - 18);
		case HOTBAR: if (data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
			return createMaxedAbilitiesIcon(data, slot, null);
		}
		else {
			return createHotbarIcon(slot);
		}
		case KEYBIND: 
		KeyBind bind = KeyBind.getBindFromSlot(slot - 18);
		if (data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
			return createMaxedAbilitiesIcon(data, bind.getDataSlot(), bind);
		}
		else {
			return addNbt(bind.getItem(), bind.getDataSlot());
		}
		case OFFHAND: return createOffhandIcon();
		default: return null; // should never happen
		}
	}

	private int[][] arrayFromEquipSlot(EquipmentType type) {
		EquipSlot[] eqs = type.getSlots();
		int[][] slots = new int[eqs.length][];
		int idx = 0;
		for (EquipSlot es : eqs) {
			switch (es) {
			case ACCESSORY:
				slots[idx++] = ACCESSORIES;
				break;
			case ARMOR:
				slots[idx++] = ARMOR;
				break;
			case HOTBAR:
				slots[idx++] = HOTBAR;
				break;
			case KEYBIND:
				slots[idx++] = KEYBINDS;
				break;
			case OFFHAND:
				slots[idx++] = new int[] { OFFHAND };
				break;
			default:
				return null; // This should never happen
			}
		}
		return slots;
	}
}
