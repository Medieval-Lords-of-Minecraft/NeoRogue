package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBT;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class EquipmentGlossaryBrowserInventory extends GlossaryInventory {
	private static final int ITEMS_PER_PAGE = 36;
	private static final int CLASS = 37, DROPPABLE = 38, RARITY = 39, REFORGE = 40,
			TAG = 41, TYPE = 42, UNLOCKED = 43, PAGE_LEFT = 45, SEARCH = 49, PAGE_RIGHT = 53;
	private static final String SEARCH_INPUT = "query";

	private int page;
	private EquipmentClass equipmentClass;
	private Boolean droppable;
	private Rarity rarity;
	private ReforgeType reforgeType;
	private GlossaryTag tag;
	private EquipmentType equipmentType;
	private Boolean unlocked;
	private String search = "";

	public EquipmentGlossaryBrowserInventory(Player viewer) {
		this(viewer, null);
	}

	public EquipmentGlossaryBrowserInventory(Player viewer, CoreInventory prev) {
		super(viewer, 54, Component.text("Equipment", NamedTextColor.GOLD), prev);
		refresh();
	}

	private void refresh() {
		List<Equipment> matches = getMatches();
		int maxPage = Math.max(0, (matches.size() - 1) / ITEMS_PER_PAGE);
		page = Math.min(page, maxPage);

		Session session = SessionManager.getSession(p);
		PlayerSessionData sessionData = session != null ? session.getData(p.getUniqueId()) : null;
		ItemStack[] contents = new ItemStack[54];
		int start = page * ITEMS_PER_PAGE;
		for (int slot = 0; slot < ITEMS_PER_PAGE && start + slot < matches.size(); slot++) {
			contents[slot] = matches.get(start + slot).getChoiceItem(sessionData);
		}

		if (page > 0) {
			contents[PAGE_LEFT] = CoreInventory.createButton(ArtifactsInventory.PREV_HEAD,
					Component.text("Previous Page", NamedTextColor.YELLOW));
		}
		contents[CLASS] = createFilterButton(Material.IRON_SWORD, "Class", display(equipmentClass), matches.size());
		contents[DROPPABLE] = createFilterButton(Material.CHEST, "Droppable", display(droppable), matches.size());
		contents[RARITY] = createFilterButton(Material.NETHER_STAR, "Rarity", display(rarity), matches.size());
		contents[REFORGE] = createFilterButton(Material.ANVIL, "Reforge Type", display(reforgeType), matches.size());
		contents[TAG] = createFilterButton(Material.NAME_TAG, "Tag", display(tag), matches.size());
		contents[TYPE] = createFilterButton(Material.ITEM_FRAME, "Type", display(equipmentType), matches.size());
		contents[UNLOCKED] = createFilterButton(Material.TRIPWIRE_HOOK, "Unlocked", display(unlocked), matches.size());
		contents[SEARCH] = createSearchButton(matches.size());
		if (page < maxPage) {
			contents[PAGE_RIGHT] = CoreInventory.createButton(ArtifactsInventory.NEXT_HEAD,
					Component.text("Next Page", NamedTextColor.YELLOW));
		}
		inv.setContents(contents);
	}

	private List<Equipment> getMatches() {
		PlayerData playerData = PlayerManager.getPlayerData(p.getUniqueId());
		return Equipment.getAll().stream()
				.filter(eq -> matchesSearch(eq))
				.filter(eq -> equipmentClass == null || Arrays.asList(eq.getEquipmentClasses()).contains(equipmentClass))
				.filter(eq -> droppable == null || isDroppable(eq) == droppable)
				.filter(eq -> rarity == null || eq.getRarity() == rarity)
				.filter(eq -> reforgeType == null || reforgeType.filter.test(eq))
				.filter(eq -> tag == null || eq.getTags().contains(tag))
				.filter(eq -> equipmentType == null || eq.getType() == equipmentType)
				.filter(eq -> unlocked == null || UnlockRegistry.isEquipmentUnlockedFor(playerData, eq.getId()) == unlocked)
				.sorted(Comparator.comparingInt((Equipment eq) -> eq.getRarity().getValue()).thenComparing(Equipment::getId))
				.collect(Collectors.toList());
	}

	private boolean matchesSearch(Equipment eq) {
		if (search.isEmpty()) return true;
		String id = eq.getId().toLowerCase(Locale.ROOT);
		String name = PlainTextComponentSerializer.plainText().serialize(eq.getDisplay()).toLowerCase(Locale.ROOT);
		return id.contains(search) || name.contains(search);
	}

	private boolean isDroppable(Equipment eq) {
		return eq.canDrop() && (eq.getReforgeParents().isEmpty() || eq.overridesReforgeDrop());
	}

	private ItemStack createFilterButton(Material material, String name, String value, int matches) {
		ItemStack item = CoreInventory.createButton(material,
				Component.text(name + ": " + value, NamedTextColor.LIGHT_PURPLE));
		ItemMeta meta = item.getItemMeta();
		meta.lore(List.of(
				line(Component.text("\u25B6 " + value, NamedTextColor.WHITE)),
				line(Component.text(matches + " matches", NamedTextColor.DARK_GRAY)),
				Component.empty(),
				line(Component.text("Left click: next", NamedTextColor.YELLOW)),
				line(Component.text("Right click: previous", NamedTextColor.YELLOW))));
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack createSearchButton(int matches) {
		ItemStack item = CoreInventory.createButton(Material.COMPASS, Component.text("Search", NamedTextColor.AQUA));
		ItemMeta meta = item.getItemMeta();
		String selected = search.isEmpty() ? "None" : search;
		meta.lore(List.of(
				line(Component.text("Query: ", NamedTextColor.GRAY).append(Component.text(selected, NamedTextColor.WHITE))),
				line(Component.text(matches + " matches", NamedTextColor.DARK_GRAY)),
				Component.empty(),
				line(Component.text("Click to search", NamedTextColor.GREEN))));
		item.setItemMeta(meta);
		return item;
	}

	private Component line(Component component) {
		return component.decoration(TextDecoration.ITALIC, false);
	}

	private String display(Object value) {
		if (value == null) return "All";
		if (value instanceof EquipmentClass) return ((EquipmentClass) value).getDisplay();
		if (value instanceof EquipmentType) return ((EquipmentType) value).getDisplay();
		if (value instanceof Rarity) return ((Rarity) value).name().substring(0, 1)
				+ ((Rarity) value).name().substring(1).toLowerCase();
		if (value instanceof GlossaryTag) return formatEnum((GlossaryTag) value);
		if (value instanceof ReforgeType) return formatEnum((ReforgeType) value);
		return ((Boolean) value) ? "Yes" : "No";
	}

	private String formatEnum(Enum<?> value) {
		String name = value.name().toLowerCase().replace('_', ' ');
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private <T> T cycle(List<T> values, T current, boolean backwards) {
		int index = values.indexOf(current);
		int offset = backwards ? -1 : 1;
		return values.get(Math.floorMod(index + offset, values.size()));
	}

	private List<GlossaryTag> getAvailableTags() {
		ArrayList<GlossaryTag> tags = new ArrayList<GlossaryTag>();
		tags.add(null);
		for (GlossaryTag candidate : GlossaryTag.values()) {
			boolean available = Equipment.getAll().stream()
					.filter(eq -> equipmentClass == null || Arrays.asList(eq.getEquipmentClasses()).contains(equipmentClass))
					.anyMatch(eq -> eq.getTags().contains(candidate));
			if (available) tags.add(candidate);
		}
		return tags;
	}

	private void openSearchDialog() {
		ActionButton searchButton = ActionButton.builder(Component.text("Search", NamedTextColor.GREEN))
				.action(DialogAction.customClick((response, audience) -> {
					String responseText = response.getText(SEARCH_INPUT);
					search = responseText == null ? "" : responseText.trim().toLowerCase(Locale.ROOT);
					page = 0;
					reopenFromDialog();
				}, ClickCallback.Options.builder().uses(1).build()))
				.build();
		ActionButton cancelButton = ActionButton.builder(Component.text("Cancel", NamedTextColor.RED))
				.action(DialogAction.customClick((response, audience) -> reopenFromDialog(),
						ClickCallback.Options.builder().uses(1).build()))
				.build();
		Dialog dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(Component.text("Search Equipment"))
						.canCloseWithEscape(false)
						.inputs(List.of(DialogInput.text(SEARCH_INPUT, Component.text("Name or ID"))
								.initial(search)
								.maxLength(64)
								.build()))
						.build())
				.type(DialogType.confirmation(searchButton, cancelButton)));
		p.showDialog(dialog);
	}

	private void reopenFromDialog() {
		new BukkitRunnable() {
			@Override
			public void run() {
				openOther = true;
				refresh();
				openInventory();
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		int slot = event.getSlot();
		if (slot >= 0 && slot < ITEMS_PER_PAGE && event.isLeftClick() && event.getCurrentItem() != null) {
			String equipId = NBT.get(event.getCurrentItem(),
					nbt -> nbt.getKeys().contains("equipId") ? nbt.getString("equipId") : null);
			if (equipId == null) return;
			openOther = false;
			new BukkitRunnable() {
				@Override
				public void run() {
					new EquipmentGlossaryInventory(p, Equipment.get(equipId, false),
							EquipmentGlossaryBrowserInventory.this);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		boolean backwards = event.isRightClick();
		switch (slot) {
		case PAGE_LEFT:
			if (page > 0) page--;
			break;
		case CLASS:
			equipmentClass = cycle(withAll(EquipmentClass.values()), equipmentClass, backwards);
			if (!getAvailableTags().contains(tag)) tag = null;
			page = 0;
			break;
		case DROPPABLE:
			droppable = cycle(Arrays.asList(null, true, false), droppable, backwards);
			page = 0;
			break;
		case RARITY:
			rarity = cycle(withAll(Rarity.values()), rarity, backwards);
			page = 0;
			break;
		case REFORGE:
			reforgeType = cycle(withAll(ReforgeType.values()), reforgeType, backwards);
			page = 0;
			break;
		case TAG:
			tag = cycle(getAvailableTags(), tag, backwards);
			page = 0;
			break;
		case TYPE:
			equipmentType = cycle(withAll(EquipmentType.values()), equipmentType, backwards);
			page = 0;
			break;
		case UNLOCKED:
			unlocked = cycle(Arrays.asList(null, true, false), unlocked, backwards);
			page = 0;
			break;
		case SEARCH:
			openOther = false;
			openSearchDialog();
			return;
		case PAGE_RIGHT:
			if ((page + 1) * ITEMS_PER_PAGE < getMatches().size()) page++;
			break;
		default:
			return;
		}
		refresh();
		Sounds.turnPage.play(p, p);
	}

	private <T> List<T> withAll(T[] values) {
		ArrayList<T> options = new ArrayList<T>();
		options.add(null);
		options.addAll(Arrays.asList(values));
		return options;
	}

	private enum ReforgeType {
		PARENT(eq -> !eq.getReforgeOptions().isEmpty()),
		CHILD(eq -> !eq.getReforgeParents().isEmpty()),
		NONE(eq -> eq.getReforgeOptions().isEmpty() && eq.getReforgeParents().isEmpty());

		private final Predicate<Equipment> filter;

		ReforgeType(Predicate<Equipment> filter) {
			this.filter = filter;
		}
	}
}