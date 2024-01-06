package me.neoblade298.neorogue.player.inventory;

import java.util.Iterator;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ArtifactsInventory extends CoreInventory {
	private static final int PREVIOUS = 4, NEXT = 6;
	public static final String PREV_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFjOTZhNWMzZDEzYzMxOTkxODNlMWJjN2YwODZmNTRjYTJhNjUyNzEyNjMwM2FjOGUyNWQ2M2UxNmI2NGNjZiJ9fX0=";
	public static final String NEXT_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzMzYWU4ZGU3ZWQwNzllMzhkMmM4MmRkNDJiNzRjZmNiZDk0YjM0ODAzNDhkYmI1ZWNkOTNkYThiODEwMTVlMyJ9fX0=";
	private PlayerSessionData data;
	private int page;
	public ArtifactsInventory(PlayerSessionData data) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), calculateInventorySize(data), Component.text("Artifacts", NamedTextColor.BLUE)));
		this.data = data;
		
		setupInventory();
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();
		TreeMap<String, ArtifactInstance> arts = data.getArtifacts();
		
		Iterator<String> iter = arts.navigableKeySet().iterator();
		// Get iterator on the right page
		for (int i = 0; i < page * 45; i++) {
			iter.next();
		}
		
		for (int i = 0; i < Math.max(contents.length - 9, 9) && iter.hasNext(); i++) {
			ArtifactInstance art = arts.get(iter.next());
			contents[i] = art.getItem();
		}
		
		if (arts.size() == 0) {
			for (int i = 0; i < contents.length; i++) {
				contents[i] = CoreInventory.createButton(Material.BARRIER, (TextComponent) SharedUtil.color("<red>No artifacts to display!"));
			}
		}
		
		if (page > 0) contents[contents.length - 9 + PREVIOUS] = CoreInventory.createButton(PREV_HEAD, Component.text("Previous Page"));
		if (page < arts.size() / 45) contents[contents.length - 9 + NEXT] = CoreInventory.createButton(NEXT_HEAD, Component.text("Next Page"));
		inv.setContents(contents);
	}
	
	private static int calculateInventorySize(PlayerSessionData data) {
		int size = data.getArtifacts().size();
		size = 9*(int) Math.ceil((double) size / 9);
		if (data.getArtifacts().size() > 45) size += 9; // Only add buttons if pagination is needed
		return Math.min(54, size);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		
		int slot = e.getRawSlot();
		if (e.getCurrentItem() == null) return;
		if (slot == inv.getSize() - 9 + NEXT) {
			inv.clear();
			page++;
			setupInventory();
		}
		else if (slot == inv.getSize() - 9 + PREVIOUS) {
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
