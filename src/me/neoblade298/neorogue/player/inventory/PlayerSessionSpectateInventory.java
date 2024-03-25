package me.neoblade298.neorogue.player.inventory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class PlayerSessionSpectateInventory extends CoreInventory {
	private static final int[] ARMOR = new int[] { 0, 1, 2 };
	private static final int[] ACCESSORIES = new int[] { 3, 4, 5, 6, 7, 8 };
	private static final int[] HOTBAR = new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26 };
	private static final int[] FILLER = new int[] { 16, 28, 29, 30, 32, 33, 34 };
	private static final int[] KEYBINDS = new int[] { 9, 10, 11, 12, 13, 14, 15 };
	private static final int STATS = 27, TRASH = 35, OFFHAND = 17, ARTIFACTS = 31, SEE_OTHERS = 28, MAP = 29, STORAGE = 30;
	private static HashMap<Integer, EquipSlot> slotTypes = new HashMap<Integer, EquipSlot>();
	private static final DecimalFormat df = new DecimalFormat("#.##");

	private static final TextComponent instruct = Component.text("Drag a weapon, ability, or consumable",
			NamedTextColor.GRAY);
	private static final TextComponent instruct2 = Component.text("here to bind it!", NamedTextColor.GRAY);
	private static final TextComponent statsText = Component.text("Your stats:", NamedTextColor.GOLD);

	private PlayerSessionData data;
	private Player spectator;

	public PlayerSessionSpectateInventory(PlayerSessionData data, Player spectator) {
		super(spectator,
				Bukkit.createInventory(spectator, 36, Component.text("Equipment", NamedTextColor.BLUE)));
		this.data = data;
		this.spectator = spectator;
		spectator.playSound(spectator, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		setupInventory();
		Session s = data.getSession();
		if (s.getParty().containsKey(spectator.getUniqueId())) new PlayerSessionInventory(s.getParty().get(spectator.getUniqueId()));
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		// Import data from session data
		int iter = 0;
		for (int i : ARMOR) {
			slotTypes.put(i, EquipSlot.ARMOR);
			Equipment a = data.getEquipment(EquipSlot.ARMOR)[iter];
			contents[i] = a != null ? addNbt(a.getItem(), a.getId(), a.isUpgraded(), iter) : createArmorIcon(iter);
			iter++;
		}

		iter = 0;
		for (int i : ACCESSORIES) {
			slotTypes.put(i, EquipSlot.ACCESSORY);
			Equipment a = data.getEquipment(EquipSlot.ACCESSORY)[iter];
			contents[i] = a != null ? addNbt(a.getItem(), a.getId(), a.isUpgraded(), iter) : createAccessoryIcon(iter);
			iter++;
		}

		for (KeyBind bind : KeyBind.values()) {
			slotTypes.put(bind.getInventorySlot(), EquipSlot.KEYBIND);
			Equipment a = data.getEquipment(EquipSlot.KEYBIND)[bind.getDataSlot()];
			contents[bind.getInventorySlot() - 18] = a != null
					? addNbt(addBindLore(a.getItem(), bind.getInventorySlot(), bind.getDataSlot()), a.getId(),
							a.isUpgraded(), bind.getDataSlot())
					: addNbt(bind.getItem(), bind.getDataSlot());
		}

		slotTypes.put(OFFHAND, EquipSlot.OFFHAND);
		Equipment o = data.getEquipment(EquipSlot.OFFHAND)[0];
		contents[OFFHAND] = o != null ? addNbt(o.getItem(), o.getId(), o.isUpgraded(), 0) : createOffhandIcon();

		for (int i : HOTBAR) {
			slotTypes.put(i, EquipSlot.HOTBAR);
			Equipment eq = data.getEquipment(EquipSlot.HOTBAR)[i - 18];
			contents[i] = eq != null ? addNbt(addBindLore(eq.getItem(), i, i), eq.getId(), eq.isUpgraded(), i)
					: createHotbarIcon(i);
		}

		for (int i : FILLER) {
			contents[i] = CoreInventory.createButton(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
		}

		contents[STATS] = createStatsIcon(data);
		contents[STORAGE] = CoreInventory.createButton(Material.ENDER_CHEST, Component.text("Storage", NamedTextColor.GOLD));
		contents[TRASH] = addNbt(CoreInventory.createButton(Material.HOPPER,
				Component.text("Trash", NamedTextColor.GOLD), "Drag items here to trash them!", 250, NamedTextColor.GRAY),
				0);

		contents[ARTIFACTS] = addNbt(
				CoreInventory.createButton(Material.NETHER_STAR, Component.text("Artifacts", NamedTextColor.GOLD),
						"Click here to view all your artifacts!", 250, NamedTextColor.GRAY),
				0);
		if (data.getSession().getParty().size() > 1)
			contents[SEE_OTHERS] = CoreInventory.createButton(Material.SPYGLASS, Component.text("View other players", NamedTextColor.GOLD));
		contents[MAP] = CoreInventory.createButton(Material.FILLED_MAP, Component.text("Node Map", NamedTextColor.GOLD));
		inv.setContents(contents);
	}

	private static ItemStack createArmorIcon(int dataSlot) {
		return addNbt(CoreInventory.createButton(Material.YELLOW_STAINED_GLASS_PANE,
				Component.text("Armor Slot", NamedTextColor.YELLOW), "Drag an armor here to equip it!", 250,
				NamedTextColor.GRAY), dataSlot);
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
		TextComponent bound = Component.text("Bound to hotbar #" + (dataSlot + 1), NamedTextColor.YELLOW);
		return addNbt(CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE,
				Component.text("Hotbar Slot", NamedTextColor.RED), bound, instruct, instruct2), dataSlot);
	}

	private static ItemStack createStatsIcon(PlayerSessionData data) {
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
		return CoreInventory.createButton(Material.ARMOR_STAND, statsText, health, mana, stamina, mr, sr, coins);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		ItemStack cursor = e.getCursor();
		ItemStack clicked = e.getCurrentItem();
		e.setCancelled(true);
		int slot = e.getSlot();
		if (cursor.getType().isAir() && clicked == null) return;
		NBTItem nclicked = clicked != null ? new NBTItem(clicked) : null;

		if (slot == ARTIFACTS) {
			new BukkitRunnable() {
				public void run() {
					new ArtifactsInventory(data, spectator);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == SEE_OTHERS && data.getSession().getParty().size() > 1) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					new SpectateSelectInventory(data.getSession(), spectator, null, false);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == MAP) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					new NodeMapInventory(p, data.getSession());
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == STORAGE) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					new StorageInventory(data, spectator);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		// If right click with empty hand, open glossary
		if (e.isRightClick() && nclicked.hasTag("equipId") && cursor.getType().isAir()) {
			e.setCancelled(true);
			PlayerSessionSpectateInventory temp = this;
			new BukkitRunnable() {
				public void run() {
					new GlossaryInventory(p, Equipment.get(nclicked.getString("equipId"), false), temp);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
	}
	
	public AutoEquipResult attemptAutoEquip(EquipmentType type) {
		ItemStack[] contents = inv.getContents();
		int equipSlotIdx = 0;
		for (int[] slots : arrayFromEquipSlot(type)) {
			for (int s : slots) {
				ItemStack iter = contents[s];
				if (iter.getType().name().endsWith("PANE")) {
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
	public void handleInventoryClose(InventoryCloseEvent e) {}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private static ItemStack addNbt(ItemStack item, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("dataSlot", dataSlot);
		return nbti.getItem();
	}

	private static ItemStack addNbt(ItemStack item, String equipId, boolean isUpgraded, int dataSlot) {
		NBTItem nbti = new NBTItem(item);
		nbti.setString("equipId", equipId);
		nbti.setInteger("dataSlot", dataSlot);
		nbti.setBoolean("isUpgraded", isUpgraded);
		return nbti.getItem();
	}

	private static ItemStack addBindLore(ItemStack item, int invSlot, int dataSlot) {
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = meta.lore();
		EquipSlot slotType = slotTypes.get(invSlot);
		if (slotType == EquipSlot.KEYBIND) {
			lore.add(1,
					Component.text("Bound to ", NamedTextColor.YELLOW)
							.append(KeyBind.getBindFromSlot(invSlot).getDisplay())
							.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		else if (slotType == EquipSlot.HOTBAR) {
			lore.add(1, Component.text("Bound to Hotbar #" + (dataSlot - 17), NamedTextColor.YELLOW)
					.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public PlayerSessionData getData() {
		return data;
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
