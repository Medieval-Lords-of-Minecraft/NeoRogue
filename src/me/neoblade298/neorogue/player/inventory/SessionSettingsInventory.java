package me.neoblade298.neorogue.player.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.notoriety.NotorietySetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SessionSettingsInventory extends CoreInventory {
	private Session s;
	private boolean isHost;

	public SessionSettingsInventory(Player p, Session s) {
		super(p, Bukkit.createInventory(p, 9, Component.text("Session Settings", NamedTextColor.BLUE)));
		this.s = s;
		isHost = s.getHost().equals(p.getUniqueId());
		setupInventory();
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();


		// Separator between regular and notoriety settings
		for (int i = 9; i < 18; i++) {
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text("Notoriety Settings", NamedTextColor.GOLD),
				Component.text("Increase your notoriety to increase difficulty in exchange for", NamedTextColor.GRAY),
				Component.text("higher exp gain and bragging rights!", NamedTextColor.GRAY),
				Component.text("Complete a run at max notoriety to increase your notoriety limit.", NamedTextColor.GRAY),
				Component.text("Current Notoriety: ", NamedTextColor.GOLD).append(Component.text(s.getNotoriety() + " / " + s.getMaxNotoriety(), NamedTextColor.WHITE)));
		}

		int iter = 18;
		for (NotorietySetting setting : NotorietySetting.settings) {
			contents[iter++] = setting.getItem();
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (!isHost) return;
		if (e.getCurrentItem() == null) return;
		
		NBTItem clicked = new NBTItem(e.getCurrentItem());

		// Notoriety settings
		if (clicked.hasTag("id")) {
			int id = clicked.getInteger("id");
			NotorietySetting setting = NotorietySetting.getById(id);
			if (e.isLeftClick()) {
				setting.increase(s);
			}
			else if (e.isRightClick()) {
				setting.decrease(s);
			}
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
