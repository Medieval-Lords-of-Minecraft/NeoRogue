package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.Mob.MobType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BestiaryInventory extends CoreInventory {
	private static final int ITEMS_PER_PAGE = 45;
	private static final int PAGE_LEFT = 45, REGION = 47, BACK = 49, TYPE = 51, PAGE_RIGHT = 53;
	private static final List<RegionType> REGIONS = Arrays.asList(null, RegionType.LOW_DISTRICT,
			RegionType.HARVEST_FIELDS, RegionType.FROZEN_WASTES, RegionType.MEADOWOOD);
	private static final List<MobType> TYPES = Arrays.asList(null, MobType.NORMAL, MobType.MINIBOSS, MobType.BOSS);

	private final CoreInventory prev;
	private int page;
	private RegionType region;
	private MobType type;

	public BestiaryInventory(Player viewer, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, 54, Component.text("Bestiary", NamedTextColor.DARK_GREEN)));
		this.prev = prev;
		refresh();
	}

	private void refresh() {
		List<Mob> matches = getMatches();
		int maxPage = Math.max(0, (matches.size() - 1) / ITEMS_PER_PAGE);
		page = Math.min(page, maxPage);
		ItemStack[] contents = new ItemStack[54];
		int start = page * ITEMS_PER_PAGE;
		for (int slot = 0; slot < ITEMS_PER_PAGE && start + slot < matches.size(); slot++) {
			contents[slot] = matches.get(start + slot).getBestiaryItemDisplay();
		}
		if (page > 0) contents[PAGE_LEFT] = CoreInventory.createButton(ArtifactsInventory.PREV_HEAD,
				Component.text("Previous Page", NamedTextColor.YELLOW));
		contents[REGION] = createRegionFilter(matches.size());
		contents[BACK] = CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED));
		contents[TYPE] = createTypeFilter(matches.size());
		if (page < maxPage) contents[PAGE_RIGHT] = CoreInventory.createButton(ArtifactsInventory.NEXT_HEAD,
				Component.text("Next Page", NamedTextColor.YELLOW));
		inv.setContents(contents);
	}

	private List<Mob> getMatches() {
		return Mob.getAll().stream()
				.filter(mob -> region == null || mob.isInRegion(region))
				.filter(mob -> type == null || mob.getType() == type)
				.sorted(Comparator.comparing(Mob::getType).thenComparing(Mob::getId))
				.collect(Collectors.toList());
	}

	private ItemStack createRegionFilter(int matches) {
		ItemStack item = CoreInventory.createButton(Material.COMPASS,
				Component.text("Region: " + regionLabel(region), NamedTextColor.LIGHT_PURPLE));
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = new ArrayList<Component>();
		for (RegionType option : REGIONS) lore.add(optionLine(regionLabel(option), option == region));
		appendFilterFooter(lore, matches);
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack createTypeFilter(int matches) {
		ItemStack item = CoreInventory.createButton(Material.IRON_SWORD,
				Component.text("Type: " + typeLabel(type), NamedTextColor.LIGHT_PURPLE));
		ItemMeta meta = item.getItemMeta();
		List<Component> lore = new ArrayList<Component>();
		for (MobType option : TYPES) lore.add(optionLine(typeLabel(option), option == type));
		appendFilterFooter(lore, matches);
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private Component optionLine(String label, boolean active) {
		return line(Component.text((active ? "\u25B6 " : "  ") + label,
				active ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY));
	}

	private void appendFilterFooter(List<Component> lore, int matches) {
		lore.add(line(Component.text(matches + " matches", NamedTextColor.DARK_GRAY)));
		lore.add(Component.empty());
		lore.add(line(Component.text("Click to cycle", NamedTextColor.YELLOW)));
	}

	private Component line(Component component) {
		return component.decoration(TextDecoration.ITALIC, false);
	}

	private String regionLabel(RegionType value) {
		return value == null ? "All" : value.getDisplay();
	}

	private String typeLabel(MobType value) {
		if (value == null) return "All";
		if (value == MobType.NORMAL) return "Standard";
		if (value == MobType.MINIBOSS) return "Miniboss";
		return "Boss";
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) return;
		int slot = event.getSlot();
		if (slot >= 0 && slot < ITEMS_PER_PAGE && event.isRightClick() && event.getCurrentItem() != null) {
			String mobId = NBT.get(event.getCurrentItem(),
					nbt -> nbt.getKeys().contains("mobId") ? nbt.getString("mobId") : null);
			Mob mob = mobId == null ? null : Mob.get(mobId);
			if (mob != null && !mob.getTags().isEmpty()) new MobGlossaryInventory(p, mob, this);
			return;
		}

		switch (slot) {
		case PAGE_LEFT:
			if (page > 0) page--;
			break;
		case REGION:
			region = REGIONS.get((REGIONS.indexOf(region) + 1) % REGIONS.size());
			page = 0;
			break;
		case BACK:
			if (prev != null) prev.openInventory();
			return;
		case TYPE:
			type = TYPES.get((TYPES.indexOf(type) + 1) % TYPES.size());
			page = 0;
			break;
		case PAGE_RIGHT:
			if ((page + 1) * ITEMS_PER_PAGE < getMatches().size()) page++;
			break;
		default:
			return;
		}
		refresh();
		Sounds.turnPage.play(p, p);
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent event) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}
}