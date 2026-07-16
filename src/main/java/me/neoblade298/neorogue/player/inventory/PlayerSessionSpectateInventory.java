package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerSessionSpectateInventory extends CoreInventory {
	private static final int ARTIFACTS = convertSlot(PlayerSessionInventory.ARTIFACTS),
		STORAGE = convertSlot(PlayerSessionInventory.STORAGE),
		ACHIEVEMENTS = 5,
		UNLOCKS = 7;

	private PlayerSessionData data;
	private Player spectator;

	public PlayerSessionSpectateInventory(PlayerSessionData data, Player spectator) {
		super(spectator,
				Bukkit.createInventory(spectator, 36, Component.text("Equipment", NamedTextColor.BLUE)));
		this.data = data;
		this.spectator = spectator;
		spectator.playSound(spectator, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		PlayerSessionInventory.setupInventory(inv, data, true);
		inv.setItem(ACHIEVEMENTS, CoreInventory.createButton(Material.DIAMOND, Component.text("Achievements", NamedTextColor.AQUA)));
		inv.setItem(UNLOCKS, CoreInventory.createButton(Material.ENDER_EYE, Component.text("Unlocks", NamedTextColor.LIGHT_PURPLE)));
		Session s = data.getSession();
		if (s.getParty().containsKey(spectator.getUniqueId())) new PlayerSessionInventory(s.getParty().get(spectator.getUniqueId()));
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		ItemStack cursor = e.getCursor();
		ItemStack clicked = e.getCurrentItem();
		e.setCancelled(true);
		int slot = e.getSlot();
		if (cursor.getType().isAir() && clicked == null) return;
		String clickedEquipId = clicked != null ? NBT.get(clicked, nbt -> nbt.hasTag("equipId") ? nbt.getString("equipId") : null) : null;

		if (slot == ARTIFACTS) {
			new BukkitRunnable() {
				public void run() {
					new ArtifactsInventory(data, spectator);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == ACHIEVEMENTS) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					PlayerData targetData = PlayerManager.getPlayerData(data.getPlayer().getUniqueId());
					if (targetData != null) new AchievementsMenuInventory(spectator, targetData);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		else if (slot == UNLOCKS) {
			e.setCancelled(true);
			new BukkitRunnable() {
				public void run() {
					PlayerData targetData = PlayerManager.getPlayerData(data.getPlayer().getUniqueId());
					if (targetData != null) new UnlocksMenuInventory(spectator, targetData);
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
		if (e.isRightClick() && clickedEquipId != null && cursor.getType().isAir()) {
			e.setCancelled(true);
			PlayerSessionSpectateInventory temp = this;
			new BukkitRunnable() {
				public void run() {
					new EquipmentGlossaryInventory(p, Equipment.get(clickedEquipId, false), temp);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
	}

	private static int convertSlot(int slot) {
		return (slot + 27) % 36;
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	public PlayerSessionData getData() {
		return data;
	}
}
