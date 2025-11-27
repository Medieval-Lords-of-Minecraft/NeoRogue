package me.neoblade298.neorogue.player.inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SessionSettingsInventory extends CoreInventory {
	private static SoundContainer click = new SoundContainer(Sound.UI_BUTTON_CLICK);


	private Session s;
	private boolean isHost;
	private LobbyInstance inst;
	private HashMap<Integer, Integer> valuesBefore = new HashMap<Integer, Integer>();
	private HashSet<Integer> changedSettings = new HashSet<Integer>();

	public SessionSettingsInventory(Player p, Session s, LobbyInstance inst) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Session Settings", NamedTextColor.BLUE)));
		this.s = s;
		isHost = s.getHost().equals(p.getUniqueId());
		this.inst = inst;
		setupInventory();

		for (Entry<Integer, SessionSetting> entry : SessionSetting.settings.entrySet()) {
			valuesBefore.put(entry.getKey(), entry.getValue().getValue(s));
		}
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();

		contents[0] = SessionSetting.settings.get(0).getItem(s);


		// Separator between regular and notoriety settings
		for (int i = 9; i < 18; i++) {
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text("Notoriety Settings", NamedTextColor.GOLD),
				Component.text("Increase your notoriety to increase difficulty in exchange for", NamedTextColor.GRAY),
				Component.text("higher exp gain and bragging rights!", NamedTextColor.GRAY),
				Component.text("Complete a run at max notoriety to increase your notoriety limit.", NamedTextColor.GRAY),
				Component.text("Current Notoriety: ", NamedTextColor.GOLD).append(Component.text(s.getNotoriety() + " / " + s.getMaxNotoriety(), NamedTextColor.WHITE)));
		}

		for (int i = 18; SessionSetting.settings.containsKey(i); i++) {
			contents[i] = SessionSetting.settings.get(i).getItem(s);
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (!isHost) return;
		if (e.getCurrentItem() == null) return;
		
		NBTItem clicked = new NBTItem(e.getCurrentItem());

		if (clicked.hasTag("id")) {
			int id = clicked.getInteger("id");
			SessionSetting setting = SessionSetting.getById(id);
			click.play(p, p, Audience.ORIGIN);
			
			if (e.isLeftClick()) {
				setting.leftClick(s);
			}
			else if (e.isRightClick()) {
				setting.rightClick(s);
			}
			inv.setItem(e.getSlot(), setting.getItem(s));
			int newValue = setting.getValue(s);
			if (newValue != valuesBefore.get(id)) {
				changedSettings.add(id);
			}
			else {
				changedSettings.remove(id);
			}
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (!isHost) return;
		if (changedSettings.size() > 0) {
			inst.broadcast("The party host has changed the following settings:");
			for (int id : changedSettings) {
				SessionSetting setting = SessionSetting.getById(id);
				String oldValue = valuesBefore.get(id).toString();
				String newValue = "" + setting.getValue(s);

				// Hardcoded for session setting 0 (endless mode) because I'm lazy
				if (id == 0) {
					oldValue = oldValue.equals("1") ? "Enabled" : "Disabled";
					newValue = newValue.equals("1") ? "Enabled" : "Disabled";
				}
				inst.broadcast(Component.text("- ", NamedTextColor.GRAY).append(Component.text(setting.getTitle(), NamedTextColor.GOLD))
						.append(Component.text(": "))
						.append(Component.text(oldValue, NamedTextColor.YELLOW)
						.append(Component.text(" -> "))
						.append(Component.text(newValue, NamedTextColor.YELLOW))));
			}
		}
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
