package me.neoblade298.neorogue.player.inventory;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class AchievementsMenuInventory extends CoreInventory {
	private static final int BACK = 10, GLOBAL = 11, WARRIOR = 13, THIEF = 14, ARCHER = 15, MAGE = 16;
	private PlayerData targetData;
	// Reopens the inventory the back button should return to. Null falls back to the main menu.
	private Runnable prevInventory;

	public AchievementsMenuInventory(Player p) {
		this(p, null, null);
	}

	// No different besides inventory title
	public AchievementsMenuInventory(Player spectator, PlayerData target) {
		this(spectator, target, null);
	}

	// Spectator view with a custom back target.
	public AchievementsMenuInventory(Player viewer, PlayerData target, Runnable prevInventory) {
		super(viewer, Bukkit.createInventory(viewer, 27, buildTitle(target)));
		this.targetData = target;
		this.prevInventory = prevInventory;
		setupInventory();
	}

	private static Component buildTitle(PlayerData target) {
		return target != null
				? Component.text(target.getDisplay() + "'s Achievements", NamedTextColor.AQUA)
				: Component.text("Achievements", NamedTextColor.AQUA);
	}

	private void setupInventory() {
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		PlayerData data = targetData != null ? targetData : PlayerManager.getPlayerData(p.getUniqueId());
		ItemStack[] contents = inv.getContents();
		contents[WARRIOR] = createClassButton(data, Material.IRON_SWORD,
				Component.text("Warrior", NamedTextColor.RED), EquipmentClass.WARRIOR);
		contents[THIEF] = createClassButton(data, Material.IRON_INGOT,
				Component.text("Thief", NamedTextColor.YELLOW), EquipmentClass.THIEF);
		contents[ARCHER] = createClassButton(data, Material.BOW,
				Component.text("Archer", NamedTextColor.GREEN), EquipmentClass.ARCHER);
		contents[MAGE] = createClassButton(data, Material.BLAZE_ROD,
				Component.text("Mage", NamedTextColor.BLUE), EquipmentClass.MAGE);
		contents[GLOBAL] = createClassButton(data, Material.NETHER_STAR,
				Component.text("Global", NamedTextColor.GOLD), null);
		contents[BACK] = CoreInventory.createButton(Material.BARRIER,
				Component.text("Back", NamedTextColor.RED));
		inv.setContents(contents);
	}

	private ItemStack createClassButton(PlayerData data, Material mat, Component name, EquipmentClass ec) {
		ItemStack item = CoreInventory.createButton(mat, (net.kyori.adventure.text.TextComponent) name);
		int level = data.getLevel(ec);
		int exp = data.getExp(ec);
		int required = PlayerData.getXpRequired(level);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.lore(List.of(
				Component.text("Level " + level, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE),
				Component.text("Exp: " + exp + "/" + required, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE)
		));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		PlayerData pd = targetData != null ? targetData : PlayerManager.getPlayerData(p.getUniqueId());
		if (pd == null) return;

		// Reopens this menu (preserving spectator context and back target) for the leaf's back button.
		Runnable back = () -> new AchievementsMenuInventory(p, targetData, prevInventory);
		switch (e.getSlot()) {
		case WARRIOR:
			new AchievementsInventory(p, pd, EquipmentClass.WARRIOR, targetData, back);
			break;
		case THIEF:
			new AchievementsInventory(p, pd, EquipmentClass.THIEF, targetData, back);
			break;
		case ARCHER:
			new AchievementsInventory(p, pd, EquipmentClass.ARCHER, targetData, back);
			break;
		case MAGE:
			new AchievementsInventory(p, pd, EquipmentClass.MAGE, targetData, back);
			break;
		case GLOBAL:
			new AchievementsInventory(p, pd, null, targetData, back);
			break;
		case BACK:
			if (prevInventory != null) prevInventory.run();
			else new MainMenuInventory(p);
			break;
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
