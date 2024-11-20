package me.neoblade298.neorogue.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.area.Area;
import me.neoblade298.neorogue.session.Session;

public class MapViewer {
	protected UUID uuid;
	protected Session s;
	private int mapPosition, mapSlot;
	private boolean shouldRenderMap = true, isViewingMap = false;
	private ItemStack hiddenMapItem;
	
	public MapViewer(Session s, UUID uuid) {
		this.uuid = uuid;
		this.s = s;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public Session getSession() {
		return s;
	}
	
	public void scrollMapUp() {
		Player p = Bukkit.getPlayer(uuid);
		mapPosition = Math.min(Area.ROW_COUNT - 5, mapPosition + 4);
		shouldRenderMap = true;
		Sounds.turnPage.play(p, p);
	}
	
	public void scrollMapDown() {
		Player p = Bukkit.getPlayer(uuid);
		mapPosition = Math.max(0, mapPosition - 4);
		shouldRenderMap = true;
		Sounds.turnPage.play(p, p);
	}
	
	public void setViewingMap(boolean isViewingMap) {
		this.isViewingMap = isViewingMap;
	}
	
	public boolean isViewingMap() {
		return isViewingMap;
	}
	
	public void stopViewingMap() {
		Player p = Bukkit.getPlayer(uuid);
		Sounds.turnPage.play(p, p);
		PlayerInventory inv = p.getInventory();
		inv.setItem(EquipmentSlot.OFF_HAND, inv.getItem(mapSlot));
		inv.setItem(mapSlot, hiddenMapItem);
		hiddenMapItem = null;
		isViewingMap = false;
	}
	
	public void setMapSlot(int mapSlot) {
		this.mapSlot = mapSlot;
	}
	
	public int getMapSlot() {
		return mapSlot;
	}
	
	public void setMapPosition(int mapPosition) {
		this.mapPosition = mapPosition;
	}
	
	public int getMapPosition() {
		return mapPosition;
	}
	
	public void setShouldMapRender(boolean shouldRenderMap) {
		this.shouldRenderMap = shouldRenderMap;
	}
	
	public void setHiddenMapItem(ItemStack item) {
		this.hiddenMapItem = item;
	}
	
	public ItemStack getHiddenMapItem() {
		return this.hiddenMapItem;
	}
	
	public boolean shouldRenderMap() {
		return shouldRenderMap;
	}
}
