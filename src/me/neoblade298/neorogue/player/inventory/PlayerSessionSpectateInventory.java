package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerSessionSpectateInventory extends CoreInventory {
	private static final int ARTIFACTS = convertSlot(PlayerSessionInventory.ARTIFACTS),
		SEE_OTHERS = convertSlot(PlayerSessionInventory.SEE_OTHERS),
		STORAGE = convertSlot(PlayerSessionInventory.STORAGE);

	private PlayerSessionData data;
	private Player spectator;

	public PlayerSessionSpectateInventory(PlayerSessionData data, Player spectator) {
		super(spectator,
				Bukkit.createInventory(spectator, 36, Component.text("Equipment", NamedTextColor.BLUE)));
		this.data = data;
		this.spectator = spectator;
		spectator.playSound(spectator, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		PlayerSessionInventory.setupInventory(inv, data, true);
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
					new EquipmentGlossaryInventory(p, Equipment.get(nclicked.getString("equipId"), false), temp);
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
