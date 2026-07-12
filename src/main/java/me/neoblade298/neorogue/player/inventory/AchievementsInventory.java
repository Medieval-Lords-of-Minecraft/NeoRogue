package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class AchievementsInventory extends CoreInventory {
	private static final int BACK = 0;
	private static final int PROGRESS_START = 2, PROGRESS_END = 8;
	private static final int ITEMS_START = 18, ITEMS_END = 44;
	private static final int PAGE_SIZE = ITEMS_END - ITEMS_START + 1; // 27
	private static final int PREVIOUS = 48, NEXT = 50;

	private int page;
	private List<AchievementProgress> sorted;
	private Player spectator;
	private PlayerData targetData;

	public AchievementsInventory(Player p, PlayerData pd, EquipmentClass ec) {
		this(p, pd, ec, null);
	}

	public AchievementsInventory(Player viewer, PlayerData pd, EquipmentClass ec, PlayerData targetData) {
		super(viewer, Bukkit.createInventory(viewer, 54, buildTitle(pd, ec)));
		this.sorted = buildSortedList(pd, ec);
		this.targetData = targetData;
		this.spectator = targetData != null ? viewer : null;
		setupInventory();
	}

	private static Component buildTitle(PlayerData pd, EquipmentClass ec) {
		String prefix = ec != null ? ec.getDisplay() : "Global";
		return Component.text(prefix + " Achievements", NamedTextColor.WHITE);
	}

	private static List<AchievementProgress> buildSortedList(PlayerData pd, EquipmentClass ec) {
		List<Achievement> visible = AchievementManager.getForScope(ec);
		List<AchievementProgress> list = new ArrayList<>(visible.size());
		for (Achievement ach : visible) {
			list.add(getProgress(pd, ach, ec));
		}
		// Achievements are shown in registration order (see AchievementManager's list)
		return list;
	}

	private static AchievementProgress getProgress(PlayerData pd, Achievement ach, EquipmentClass ec) {
		if (ec != null) {
			return pd.getClassAchievementProgress(ach.getId(), ec);
		}
		return pd.getGlobalAchievementProgress(ach.getId());
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();

		// Back button
		contents[BACK] = CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED));

		// Progress bar (slots 2-8)
		fillProgressBar(contents);

		// Achievement items (slots 18-44)
		int start = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE && start + i < sorted.size(); i++) {
			contents[ITEMS_START + i] = sorted.get(start + i).toItemStack();
		}

		// Pagination (slots 45-53)
		int totalPages = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));
		if (page > 0) {
			contents[PREVIOUS] = CoreInventory.createButton(ArtifactsInventory.PREV_HEAD,
					Component.text("Previous Page", NamedTextColor.YELLOW));
		}
		if (page < totalPages - 1) {
			contents[NEXT] = CoreInventory.createButton(ArtifactsInventory.NEXT_HEAD,
					Component.text("Next Page", NamedTextColor.YELLOW));
		}

		inv.setContents(contents);
	}

	private void fillProgressBar(ItemStack[] contents) {
		int totalMastery = 0, totalMaxMastery = 0;
		int completeCount = 0;
		for (AchievementProgress prog : sorted) {
			totalMastery += prog.getMastery();
			totalMaxMastery += prog.getMaxMastery();
			if (prog.isComplete()) completeCount++;
		}
		double achievedPct = totalMaxMastery > 0 ? (double) totalMastery / totalMaxMastery : 0;
		double masteredPct = sorted.size() > 0 ? (double) completeCount / sorted.size() : 0;

		int paneCount = PROGRESS_END - PROGRESS_START + 1; // 7
		int achievedSlots = (int) (achievedPct * paneCount);
		int masteredSlots = (int) (masteredPct * paneCount);

		int achievedDisplay = (int) (achievedPct * 100);
		int masteredDisplay = (int) (masteredPct * 100);
		Component paneName = Component.text(achievedDisplay + "% achieved", NamedTextColor.YELLOW)
				.append(Component.text(" | ", NamedTextColor.GRAY))
				.append(Component.text(masteredDisplay + "% mastered", NamedTextColor.GREEN))
				.decoration(TextDecoration.ITALIC, State.FALSE);

		for (int i = 0; i < paneCount; i++) {
			Material mat;
			if (i < masteredSlots) {
				mat = Material.GREEN_STAINED_GLASS_PANE;
			} else if (i < achievedSlots) {
				mat = Material.YELLOW_STAINED_GLASS_PANE;
			} else {
				mat = Material.RED_STAINED_GLASS_PANE;
			}
			ItemStack pane = new ItemStack(mat);
			ItemMeta meta = pane.getItemMeta();
			meta.displayName(paneName);
			pane.setItemMeta(meta);
			contents[PROGRESS_START + i] = pane;
		}
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		int slot = e.getRawSlot();
		if (slot == BACK) {
			if (spectator != null) {
				new AchievementsMenuInventory(spectator, targetData);
			} else {
				new AchievementsMenuInventory(p);
			}
			return;
		}

		int totalPages = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));
		if (slot == PREVIOUS && page > 0) {
			inv.clear();
			page--;
			setupInventory();
		} else if (slot == NEXT && page < totalPages - 1) {
			inv.clear();
			page++;
			setupInventory();
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
