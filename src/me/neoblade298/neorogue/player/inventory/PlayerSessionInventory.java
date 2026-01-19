package me.neoblade298.neorogue.player.inventory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.inventories.CorePlayerInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.EditInventoryInstance;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class PlayerSessionInventory extends CorePlayerInventory implements ShiftClickableInventory {
	private static final int[] ARMOR = new int[] { 18, 19, 20 };
	private static final int[] ACCESSORIES = new int[] { 21, 22, 23, 24, 25, 26 };
	private static final int[] HOTBAR = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] FILLER = new int[] { 11, 12, 14, 15, 16, 17, 34 };
	private static final int[] KEYBINDS = new int[] { 27, 28, 29, 30, 31, 32, 33 };
	public static final int STATS = 9, TRASH = 17, STORAGE = 10, OFFHAND = 35, ARTIFACTS = 13, SEE_OTHERS = 11, MAP = 40, SETTINGS = 12;
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
			Equipment a = data.getEquipment(EquipSlot.ARMOR)[iter];
			contents[(i + offset) % inv.getSize()] = a != null ? addNbt(a.getItem(), a.getId(), a.isUpgraded(), iter) : createArmorIcon(iter);
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
			Equipment a = data.getEquipment(EquipSlot.ACCESSORY)[iter];
			contents[(i + offset) % inv.getSize()] = a != null ? addNbt(a.getItem(), a.getId(), a.isUpgraded(), iter) : createAccessoryIcon(iter);
			iter++;
		}

		for (KeyBind bind : KeyBind.values()) {
			slotTypes.put(bind.getInventorySlot(), EquipSlot.KEYBIND);
			Equipment a = data.getEquipment(EquipSlot.KEYBIND)[bind.getDataSlot()];
			if (a == null && data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
				contents[bind.getInventorySlot()] = createMaxedAbilitiesIcon(data, bind.getDataSlot());
				continue;
			}
			contents[bind.getInventorySlot()] = a != null
					? addNbt(addBindLore(a.getItem(), bind.getInventorySlot(), bind.getDataSlot()), a.getId(),
							a.isUpgraded(), bind.getDataSlot())
					: addNbt(bind.getItem(), bind.getDataSlot());
		}

		slotTypes.put(OFFHAND, EquipSlot.OFFHAND);
		Equipment o = data.getEquipment(EquipSlot.OFFHAND)[0];
		contents[(OFFHAND + offset) % inv.getSize()] = o != null ? addNbt(o.getItem(), o.getId(), o.isUpgraded(), 0) : createOffhandIcon();

		for (int i : HOTBAR) {
			slotTypes.put(i, EquipSlot.HOTBAR);
			Equipment eq = data.getEquipment(EquipSlot.HOTBAR)[i];
			if (eq == null && data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
				contents[(i + offset) % inv.getSize()] = createMaxedAbilitiesIcon(data, i);
				continue;
			}
			contents[(i + offset) % inv.getSize()] = eq != null ? addNbt(addBindLore(eq.getItem(), i, i), eq.getId(), eq.isUpgraded(), i)
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

		if (!(data.getSession().getInstance() instanceof NodeSelectInstance)) {
			contents[(MAP + offset) % inv.getSize()] = CoreInventory.createButton(Material.FILLED_MAP, Component.text("Node Map", NamedTextColor.GOLD));
			MapMeta meta = (MapMeta) contents[(MAP + offset) % inv.getSize()].getItemMeta();
			MapView map = Bukkit.getMap(EditInventoryInstance.MAP_ID);
			meta.setMapView(map);
			contents[(MAP + offset) % inv.getSize()].setItemMeta(meta);
		}
		inv.setContents(contents);
	}

	private static ItemStack createArmorIcon(int dataSlot) {
		return addNbt(CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE,
				Component.text("Armor Slot", NamedTextColor.YELLOW), "Drag an armor here to equip it!", 250,
				NamedTextColor.GRAY), dataSlot);
	}

	private static ItemStack createMaxedAbilitiesIcon(PlayerSessionData data, int dataSlot) {
		ItemStack item = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text("Out of abilities", NamedTextColor.RED),
				Component.text("You have equipped " + data.getAbilitiesEquipped() + " / " +data.getMaxAbilities() + " abilities", NamedTextColor.GRAY));
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
			KeyBind kb = KeyBind.getBindFromSlot(dataSlot);
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

	private static ItemStack createHotbarIcon(int dataSlot) {
		TextComponent bound = Component.text("Bound to Hotbar #" + (dataSlot + 1), NamedTextColor.YELLOW);
		return addNbt(CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE,
				Component.text("Hotbar Slot", NamedTextColor.RED), bound, instruct, instruct2), dataSlot);
	}

	private static ItemStack createStatsIcon(PlayerSessionData data) {
		TextComponent cls = Component.text("Class: ", NamedTextColor.GOLD)
				.append(Component.text(data.getPlayerClass().getDisplay(), NamedTextColor.WHITE));
		TextComponent health = Component.text("Health: ", NamedTextColor.GOLD)
				.append(Component.text(df.format(data.getHealth()) + " / " + data.getMaxHealth(), NamedTextColor.WHITE));
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
		TextComponent health = Component.text("Enemy Health Scaling: ", NamedTextColor.GOLD)
				.append(Component.text("Lv " + s.getEnemyHealthScale(), NamedTextColor.WHITE));
		TextComponent dmg = Component.text("Enemy Damage Scaling: ", NamedTextColor.GOLD)
				.append(Component.text("Lv " + s.getEnemyDamageScale(), NamedTextColor.WHITE));
		TextComponent gold = Component.text("Coin reduction: ", NamedTextColor.GOLD)
				.append(Component.text("Lv " + s.getCoinReduction(), NamedTextColor.WHITE));
		TextComponent time = Component.text("Fight Time Reduction: ", NamedTextColor.GOLD)
				.append(Component.text("Lv " + s.getFightTimeReduction(), NamedTextColor.WHITE));
		return CoreInventory.createButton(Material.ARMOR_STAND, Component.text("Your Notoriety", NamedTextColor.GOLD), health, dmg, gold, time);
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
		if (cursor.getType().isAir() && clicked == null) return;

		NBTItem ncursor = !cursor.getType().isAir() ? new NBTItem(cursor) : null;
		NBTItem nclicked = clicked != null ? new NBTItem(clicked) : null;
		Player p = (Player) e.getWhoClicked();

		if (slot == TRASH && clicked != null) {
			e.setCancelled(true);
			if (Equipment.get(ncursor.getString("equipId"), false).isCursed()) {
				Util.displayError(p, "You can't trash cursed items!");
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
				p.setItemOnCursor(null);
				data.sendToStorage(eq);
				p.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F);
				clearHighlights();
			}
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

		if (cursor.getType().isAir() && clicked != null) {
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
				if (isBindable(type)) clicked = removeBindLore(clicked);
				setHighlights(eq.getType());
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
			if (eq.containsReforgeOption(eqedId)) {
				if (!Equipment.canReforge(eq, eqed)) {
					displayError("At least one of the items must be upgraded to reforge!", false);
					return;
				}
				new BukkitRunnable() {
					public void run() {
						p.setItemOnCursor(null);
						EquipSlot type = slotTypes.get(slot);
						removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
						inv.setItem(slot, iconFromEquipSlot(type, slot));
						handleInventoryClose();
						new ReforgeOptionsInventory(data, eq, eqed);
					}
				}.runTask(NeoRogue.inst());
				return;
			}
			else if (eqed != null && eqed.containsReforgeOption(eqId)) {
				if (!Equipment.canReforge(eq, eqed)) {
					displayError("At least one of the items must be upgraded to reforge!", false);
					return;
				}
				new BukkitRunnable() {
					public void run() {
						p.setItemOnCursor(null);
						EquipSlot type = slotTypes.get(slot);
						removeEquipment(type, nclicked.getInteger("dataSlot"), slot, e.getClickedInventory());
						inv.setItem(slot, iconFromEquipSlot(type, slot));
						handleInventoryClose();
						new ReforgeOptionsInventory(data, eqed, eq);
					}
				}.runTask(NeoRogue.inst());
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

			// If swapping equipment with equipment, remove that equipment
			if (!nclicked.hasTag("equipId")) {
				p.setItemOnCursor(null);
			}
			else {
				if (isBindable(type)) clicked = removeBindLore(clicked);
				data.removeEquipment(type, nclicked.getInteger("dataSlot"));
				p.setItemOnCursor(clicked);
				setHighlights(Equipment.get(nclicked.getString("equipId"), false).getType());
			}

			data.setEquipment(type, nclicked.getInteger("dataSlot"), eq);
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
			if (isBindable(type)) cursor = addBindLore(cursor, slot, nclicked.getInteger("dataSlot"));
			inv.setItem(slot, addNbt(cursor, nclicked.getInteger("dataSlot")));
			clearHighlights();
		}
		else {
			p.playSound(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1F);
		}
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
					createMaxedAbilitiesIcon(data, nbti.getInteger("dataSlot"));
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
		data.setEquipment(result.es, nauto.getInteger("dataSlot"), eq);
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

		if (clicked == null) return;
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
		Util.msg(p, error);
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
		case ACCESSORY: return createAccessoryIcon(slot - 21);
		case ARMOR: return createArmorIcon(slot - 18);
		case HOTBAR: if (data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
			return createMaxedAbilitiesIcon(data, slot);
		}
		else {
			return createHotbarIcon(slot);
		}
		case KEYBIND: 
		if (data.getAbilitiesEquipped() >= data.getMaxAbilities()) {
			return createMaxedAbilitiesIcon(data, slot);
		}
		else {
		KeyBind bind = KeyBind.getBindFromSlot(slot - 18);
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
