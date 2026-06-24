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
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.unlock.UnlockNode;
import me.neoblade298.neorogue.player.unlock.UnlockRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class UnlocksMenuInventory extends CoreInventory {
	private static final int BACK = 10, GLOBAL = 11, WARRIOR = 13, THIEF = 14, ARCHER = 15, MAGE = 16;
	private PlayerData targetData;

	public UnlocksMenuInventory(Player p) {
		super(p, Bukkit.createInventory(p, 27, Component.text("Unlocks", NamedTextColor.LIGHT_PURPLE)));
		setupInventory();
	}

	// No different besides inventory title
	public UnlocksMenuInventory(Player spectator, PlayerData target) {
		super(spectator, Bukkit.createInventory(spectator, 27,
				Component.text(target.getDisplay() + "'s Unlocks", NamedTextColor.LIGHT_PURPLE)));
		this.targetData = target;
		setupInventory();
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
		ItemStack item = CoreInventory.createButton(mat, (TextComponent) name);
		ItemMeta meta = item.getItemMeta();
		meta.setMaxStackSize(64);
		item.setItemMeta(meta);
		ArrayList<UnlockNode> nodes = UnlockRegistry.getNodesForClass(ec);
		int available = 0;
		int points = data.getPoints(ec);
		for (UnlockNode node : nodes) {
			if (data.hasUnlockNode(node.getId())) continue;
			if (node.getCost() <= points && node.checkRequirementsMet(data)) {
				available++;
			}
		}
		int level = data.getLevel(ec);
		int exp = data.getExp(ec);
		int required = PlayerData.getXpRequired(level);
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Level " + level, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE));
		lore.add(Component.text("Exp: " + exp + "/" + required, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, State.FALSE));
		if (available > 0) {
			item.setAmount(Math.min(available, 64));
			lore.add(Component.text(available + " unlock" + (available > 1 ? "s" : "") + " available!",
					NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, State.FALSE));
		}
		meta = item.getItemMeta();
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;

		switch (e.getSlot()) {
		case WARRIOR:
			new UnlockClassInventory(p, EquipmentClass.WARRIOR, targetData);
			break;
		case THIEF:
			new UnlockClassInventory(p, EquipmentClass.THIEF, targetData);
			break;
		case ARCHER:
			new UnlockClassInventory(p, EquipmentClass.ARCHER, targetData);
			break;
		case MAGE:
			new UnlockClassInventory(p, EquipmentClass.MAGE, targetData);
			break;
		case GLOBAL:
			new UnlockClassInventory(p, null, targetData);
			break;
		case BACK:
			new MainMenuInventory(p);
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
