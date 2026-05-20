package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ArtifactsInventory extends CoreInventory {
	private static final int PREVIOUS = 4, NEXT = 6;
	private static final int PAGE_SIZE_WITH_NAV = 36;
	private static final int MAX_PER_PAGE_NO_NAV = 45;
	private static final String RUBY_SHARD = "RubyShard", RUBY_CLUSTER = "RubyCluster", RUBY_GEM = "RubyGem";
	private static final String SAPPHIRE_SHARD = "SapphireShard", SAPPHIRE_CLUSTER = "SapphireCluster", SAPPHIRE_GEM = "SapphireGem";
	private static final String EMERALD_SHARD = "EmeraldShard", EMERALD_CLUSTER = "EmeraldCluster", EMERALD_GEM = "EmeraldGem";
	public static final String PREV_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFjOTZhNWMzZDEzYzMxOTkxODNlMWJjN2YwODZmNTRjYTJhNjUyNzEyNjMwM2FjOGUyNWQ2M2UxNmI2NGNjZiJ9fX0=";
	public static final String NEXT_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzMzYWU4ZGU3ZWQwNzllMzhkMmM4MmRkNDJiNzRjZmNiZDk0YjM0ODAzNDhkYmI1ZWNkOTNkYThiODEwMTVlMyJ9fX0=";
	private PlayerSessionData data;
	private int page;
	private int regularCount;
	public ArtifactsInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), calculateInventorySize(data), Component.text("Artifacts", NamedTextColor.BLUE)));
		this.data = data;
		Session s = data.getSession();
		if (s.getParty().containsKey(p.getUniqueId())) new PlayerSessionInventory(s.getParty().get(p.getUniqueId()));
		
		setupInventory();
	}
	public ArtifactsInventory(PlayerSessionData data, Player spectator) {
		super(spectator, Bukkit.createInventory(spectator, calculateInventorySize(data), Component.text("Artifacts", NamedTextColor.BLUE)));
		this.data = data;
		
		setupInventory();
	}
	
	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		ItemStack[] contents = inv.getContents();
		TreeMap<String, ArtifactInstance> arts = data.getArtifacts();

		ArrayList<ArtifactInstance> regular = new ArrayList<ArtifactInstance>();
		Iterator<String> iter = arts.navigableKeySet().iterator();
		while (iter.hasNext()) {
			String id = iter.next();
			if (isGemArtifactId(id)) continue;
			regular.add(arts.get(id));
		}
		regularCount = regular.size();

		boolean hasPagination = hasPagination();
		int aggregateStart = contents.length - (hasPagination ? 18 : 9);
		int regularSlots = aggregateStart;
		int pageSize = hasPagination ? PAGE_SIZE_WITH_NAV : regularSlots;
		int start = page * pageSize;
		for (int i = 0; i < regularSlots && start + i < regular.size(); i++) {
			contents[i] = regular.get(start + i).getItem();
		}

		if (arts.size() == 0) {
			for (int i = 0; i < regularSlots; i++) {
				contents[i] = CoreInventory.createButton(Material.BARRIER, (TextComponent) SharedUtil.color("<red>No artifacts to display!"));
			}
		}

		fillAggregateRow(contents, aggregateStart, arts);

		if (hasPagination) {
			int totalPages = (int) Math.ceil((double) regularCount / PAGE_SIZE_WITH_NAV);
			if (page > 0) contents[contents.length - 9 + PREVIOUS] = CoreInventory.createButton(PREV_HEAD, Component.text("Previous Page"));
			if (page < totalPages - 1) contents[contents.length - 9 + NEXT] = CoreInventory.createButton(NEXT_HEAD, Component.text("Next Page"));
		}
		inv.setContents(contents);
	}
	
	private void fillAggregateRow(ItemStack[] contents, int rowStart, TreeMap<String, ArtifactInstance> arts) {
		for (int i = rowStart; i < rowStart + 9 && i < contents.length; i++) {
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}

		contents[rowStart + 2] = createAggregateItem("Ruby Artifacts", NamedTextColor.RED, RUBY_GEM,
				getAmount(arts, RUBY_SHARD), getAmount(arts, RUBY_CLUSTER), getAmount(arts, RUBY_GEM));
		contents[rowStart + 4] = createAggregateItem("Sapphire Artifacts", NamedTextColor.BLUE, SAPPHIRE_GEM,
				getAmount(arts, SAPPHIRE_SHARD), getAmount(arts, SAPPHIRE_CLUSTER), getAmount(arts, SAPPHIRE_GEM));
		contents[rowStart + 6] = createAggregateItem("Emerald Artifacts", NamedTextColor.GREEN, EMERALD_GEM,
				getAmount(arts, EMERALD_SHARD), getAmount(arts, EMERALD_CLUSTER), getAmount(arts, EMERALD_GEM));
	}

	private ItemStack createAggregateItem(String title, NamedTextColor color, String iconId, int shard, int cluster, int gem) {
		Equipment icon = Equipment.get(iconId, false);
		ItemStack item = icon != null ? icon.getItem() : CoreInventory.createButton(Material.BARRIER, Component.text(title));
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(title, color).decoration(TextDecoration.ITALIC, State.FALSE));
		meta.lore(List.of(
				Component.text("Shard: ", NamedTextColor.GRAY).append(Component.text(shard, NamedTextColor.WHITE)),
				Component.text("Cluster: ", NamedTextColor.GRAY).append(Component.text(cluster, NamedTextColor.WHITE)),
				Component.text("Gem: ", NamedTextColor.GRAY).append(Component.text(gem, NamedTextColor.WHITE))
		));
		item.setItemMeta(meta);
		item.setAmount(1);
		return item;
	}

	private int getAmount(TreeMap<String, ArtifactInstance> arts, String id) {
		ArtifactInstance inst = arts.get(id);
		return inst == null ? 0 : inst.getAmount();
	}

	private boolean hasPagination() {
		return regularCount > MAX_PER_PAGE_NO_NAV;
	}

	private static boolean isGemArtifactId(String id) {
		return RUBY_SHARD.equals(id) || RUBY_CLUSTER.equals(id) || RUBY_GEM.equals(id)
				|| SAPPHIRE_SHARD.equals(id) || SAPPHIRE_CLUSTER.equals(id) || SAPPHIRE_GEM.equals(id)
				|| EMERALD_SHARD.equals(id) || EMERALD_CLUSTER.equals(id) || EMERALD_GEM.equals(id);
	}

	private static int calculateInventorySize(PlayerSessionData data) {
		int regularSize = countRegularArtifacts(data.getArtifacts());
		if (regularSize > MAX_PER_PAGE_NO_NAV) {
			return 54;
		}
		int rows = (int) Math.ceil((double) regularSize / 9) + 1;
		return Math.min(54, Math.max(18, rows * 9));
	}

	private static int countRegularArtifacts(TreeMap<String, ArtifactInstance> arts) {
		int count = 0;
		for (String id : arts.navigableKeySet()) {
			if (!isGemArtifactId(id)) count++;
		}
		return count;
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		
		int slot = e.getRawSlot();
		if (e.getCurrentItem() == null) return;
		
		ItemStack item = e.getCurrentItem();
		NBTItem nclicked = new NBTItem(item);
		if (e.isRightClick() && nclicked.hasTag("equipId") && e.getCursor().getType().isAir()) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					new EquipmentGlossaryInventory(p, Equipment.get(nclicked.getString("equipId"), false), null);
				}
			}.runTask(NeoRogue.inst());
			return;
		}

		if (!hasPagination()) return;
		int totalPages = (int) Math.ceil((double) regularCount / PAGE_SIZE_WITH_NAV);
		if (slot == inv.getSize() - 9 + NEXT && page < totalPages - 1) {
			inv.clear();
			page++;
			setupInventory();
		}
		else if (slot == inv.getSize() - 9 + PREVIOUS && page > 0) {
			inv.clear();
			page--;
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
