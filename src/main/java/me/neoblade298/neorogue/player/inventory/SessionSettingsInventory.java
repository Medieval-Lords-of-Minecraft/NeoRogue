package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.instances.LobbyInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import me.neoblade298.neorogue.session.settings.SessionSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class SessionSettingsInventory extends CoreInventory {
	private static SoundContainer click = new SoundContainer(Sound.UI_BUTTON_CLICK);


	private Session s;
	private boolean isHost;
	private LobbyInstance inst;
	private HashMap<Integer, Integer> valuesBefore = new HashMap<Integer, Integer>();
	private HashSet<Integer> changedSettings = new HashSet<Integer>();
	private int notorietyBefore;

	public SessionSettingsInventory(Player p, Session s, LobbyInstance inst) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Session Settings", NamedTextColor.BLUE)));
		this.s = s;
		isHost = s.getHost().equals(p.getUniqueId());
		this.inst = inst;
		this.notorietyBefore = s.getNotoriety();
		setupInventory();

		for (Entry<Integer, SessionSetting> entry : SessionSetting.settings.entrySet()) {
			valuesBefore.put(entry.getKey(), entry.getValue().getValue(s));
		}
	}
	
	private void setupInventory() {
		ItemStack[] contents = inv.getContents();

		contents[0] = SessionSetting.settings.get(0).getItem(s);

		// Notoriety row: down arrow (12), icon (13), up arrow (14), glass panes elsewhere
		for (int i = 9; i < 18; i++) {
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
		}
		contents[13] = createNotorietyIcon();
		if (isHost) {
			contents[12] = createDownArrow();
			contents[14] = createUpArrow();
		} else {
			contents[12] = createDisabledArrow();
			contents[14] = createDisabledArrow();
		}

		inv.setContents(contents);
	}

	private ItemStack createDisabledArrow() {
		ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Notoriety", NamedTextColor.GRAY));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Only the host can adjust notoriety", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack createDownArrow() {
		int notoriety = s.getNotoriety();
		ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Decrease Notoriety", NamedTextColor.RED)
				.decoration(TextDecoration.ITALIC, State.FALSE));
		ArrayList<Component> lore = new ArrayList<>();
		if (notoriety > 0) {
			NotorietySetting removing = NotorietySetting.settings.get(notoriety - 1);
			lore.add(Component.text("Would remove:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
			for (TextComponent line : removing.getHeader()) {
				lore.add(Component.text(" ", NamedTextColor.DARK_GRAY).append(line).decoration(TextDecoration.ITALIC, false));
			}
		} else {
			lore.add(Component.text("Already at minimum!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("action", "notoriety-down");
		return nbti.getItem();
	}

	private ItemStack createNotorietyIcon() {
		int notoriety = s.getNotoriety();
		ItemStack item = new ItemStack(Material.OMINOUS_BOTTLE);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Notoriety: ", NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, State.FALSE)
				.append(Component.text(notoriety + " / " + s.getMaxNotoriety(inst.getHostClass()), NamedTextColor.WHITE)));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("XP Bonus: ", NamedTextColor.GRAY)
				.append(Component.text("+" + s.getNotorietyXpBonusPercent() + "%", NamedTextColor.GREEN))
				.decoration(TextDecoration.ITALIC, false));
		lore.add(Component.empty().decoration(TextDecoration.ITALIC, false));
		if (notoriety > 0) {
			lore.add(Component.text("Active Effects:", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
			for (int i = 0; i < notoriety; i++) {
				ArrayList<TextComponent> headerLines = NotorietySetting.settings.get(i).getHeader();
				for (int j = 0; j < headerLines.size(); j++) {
					String prefix = j == 0 ? "- " : "  ";
					lore.add(Component.text(prefix, NamedTextColor.DARK_GRAY)
							.append(headerLines.get(j))
							.decoration(TextDecoration.ITALIC, false));
				}
			}
		} else {
			lore.add(Component.text("No active effects", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		}
		meta.lore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack createUpArrow() {
		int notoriety = s.getNotoriety();
		int max = s.getMaxNotoriety(inst.getHostClass());
		ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("Increase Notoriety", NamedTextColor.GREEN)
			.decoration(TextDecoration.ITALIC, State.FALSE));
		ArrayList<Component> lore = new ArrayList<>();
		if (notoriety < max) {
			NotorietySetting adding = NotorietySetting.settings.get(notoriety);
			lore.add(Component.text("Would add:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
			for (TextComponent line : adding.getHeader()) {
				lore.add(Component.text(" ", NamedTextColor.DARK_GRAY).append(line).decoration(TextDecoration.ITALIC, false));
			}
			lore.add(Component.empty());
			for (TextComponent line : adding.getLore()) {
				lore.add(line.decoration(TextDecoration.ITALIC, true).color(NamedTextColor.GRAY));
			}
		} else {
			lore.add(Component.text("Already at maximum!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		}
		meta.lore(lore);
		item.setItemMeta(meta);
		NBTItem nbti = new NBTItem(item);
		nbti.setString("action", "notoriety-up");
		return nbti.getItem();
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (!isHost) return;
		if (e.getCurrentItem() == null) return;
		if (e.getAction() == InventoryAction.NOTHING) return;
		
		NBTItem clicked = new NBTItem(e.getCurrentItem());

		if (clicked.hasTag("action")) {
			String action = clicked.getString("action");
			int notoriety = s.getNotoriety();
			if (action.equals("notoriety-down") && notoriety > 0) {
				click.play(p, p, Audience.ORIGIN);
				s.setNotoriety(notoriety - 1);
			} else if (action.equals("notoriety-up") && notoriety < s.getMaxNotoriety(inst.getHostClass())) {
				click.play(p, p, Audience.ORIGIN);
				s.setNotoriety(notoriety + 1);
			}
			// Refresh all 3 notoriety items
			inv.setItem(12, createDownArrow());
			inv.setItem(13, createNotorietyIcon());
			inv.setItem(14, createUpArrow());
		} else if (clicked.hasTag("id")) {
			int id = clicked.getInteger("id");
			SessionSetting setting = SessionSetting.getById(id);
			
			if (e.isLeftClick() && setting.canLeftClick(s)) {
				click.play(p, p, Audience.ORIGIN);
				setting.leftClick(s);
			}
			else if (e.isRightClick() && setting.canRightClick(s)) {
				click.play(p, p, Audience.ORIGIN);
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
		boolean hasChanges = changedSettings.size() > 0 || s.getNotoriety() != notorietyBefore;
		if (hasChanges) {
			inst.broadcast("The party host has changed the following settings:");
			if (s.getNotoriety() != notorietyBefore) {
				inst.broadcast(Component.text("- ", NamedTextColor.GRAY).append(Component.text("Notoriety", NamedTextColor.GOLD))
						.append(Component.text(": "))
						.append(Component.text(String.valueOf(notorietyBefore), NamedTextColor.YELLOW))
						.append(Component.text(" -> "))
						.append(Component.text(String.valueOf(s.getNotoriety()), NamedTextColor.YELLOW)));
			}
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
