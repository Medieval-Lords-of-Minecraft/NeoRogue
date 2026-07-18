package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;
import java.util.HashMap;
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
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.caravan.CaravanAction;
import me.neoblade298.neorogue.player.caravan.CaravanUpgrade;
import me.neoblade298.neorogue.player.caravan.CaravanUpgradeRegistry;
import me.neoblade298.neorogue.session.reward.RunReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

// GUI for buying caravan upgrades with VaultUnlocked currency. Upgrades and their slots/costs are
// configured in caravan.yml. Each upgrade is purchasable once, gated behind its prerequisites.
public class CaravanUpgradesInventory extends CoreInventory {
	private final PlayerData pd;
	private final HashMap<Integer, CaravanUpgrade> slotToUpgrade = new HashMap<Integer, CaravanUpgrade>();

	public CaravanUpgradesInventory(Player p, PlayerData pd) {
		super(p, Bukkit.createInventory(p, 54, Component.text("Caravan Upgrades", NamedTextColor.DARK_AQUA)));
		this.pd = pd;
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		render();
	}

	private void render() {
		inv.clear();
		slotToUpgrade.clear();
		int autoSlot = 0;
		for (CaravanUpgrade up : CaravanUpgradeRegistry.getUpgrades()) {
			int slot = up.getSlot();
			if (slot < 0 || slot >= inv.getSize() || slotToUpgrade.containsKey(slot)) {
				// No/invalid/taken slot: auto-place into the next free slot.
				while (autoSlot < inv.getSize() && slotToUpgrade.containsKey(autoSlot)) autoSlot++;
				if (autoSlot >= inv.getSize()) continue;
				slot = autoSlot;
			}
			slotToUpgrade.put(slot, up);
			inv.setItem(slot, buildUpgradeItem(up));
		}
	}

	private ItemStack buildUpgradeItem(CaravanUpgrade up) {
		boolean purchased = pd.hasPurchasedUpgrade(up.getId());
		boolean reqMet = up.requirementsMet(pd);
		boolean canAfford = RunReward.hasBalance(p.getUniqueId(), up.getCost());
		boolean available = !purchased && reqMet && canAfford;

		// Material + title color by state, matching the unlock menu:
		// green = purchased, yellow = available, orange = soft-locked (can't afford), red = missing prereqs.
		Material mat;
		NamedTextColor titleColor;
		if (purchased) {
			mat = Material.GREEN_STAINED_GLASS_PANE;
			titleColor = NamedTextColor.GREEN;
		} else if (available) {
			mat = Material.YELLOW_STAINED_GLASS_PANE;
			titleColor = NamedTextColor.YELLOW;
		} else if (reqMet) {
			mat = Material.ORANGE_STAINED_GLASS_PANE;
			titleColor = NamedTextColor.GOLD;
		} else {
			mat = Material.RED_STAINED_GLASS_PANE;
			titleColor = NamedTextColor.RED;
		}

		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(line(Component.text(up.getTitle(), titleColor)));

		List<Component> lore = new ArrayList<Component>();
		for (String d : up.getDescription()) {
			lore.add(line(Component.text(d, NamedTextColor.GRAY)));
		}
		if (!up.getActions().isEmpty()) {
			lore.add(Component.empty());
			for (CaravanAction action : up.getActions()) {
				lore.add(line(Component.text("\u2022 ", NamedTextColor.DARK_GRAY).append(action.describe())));
			}
		}

		lore.add(Component.empty());
		lore.add(line(Component.text("Cost: ", NamedTextColor.GRAY)
				.append(Component.text(RunReward.formatMoney(up.getCost()),
						canAfford ? NamedTextColor.YELLOW : NamedTextColor.RED))));
		if (purchased) {
			lore.add(line(Component.text("\u2714 Purchased", NamedTextColor.GREEN)));
		} else if (available) {
			lore.add(line(Component.text("Available", NamedTextColor.YELLOW)));
		} else if (!reqMet) {
			lore.add(line(Component.text("Locked (missing prerequisites)", NamedTextColor.RED)));
		} else {
			lore.add(line(Component.text("Locked (can't afford)", NamedTextColor.GOLD)));
		}

		// Prerequisite list, coloured per group like the unlock menu.
		if (!up.getRequires().isEmpty()) {
			lore.add(Component.empty());
			lore.add(line(Component.text("Requires:", NamedTextColor.WHITE)));
			for (String[] orGroup : up.getRequires()) {
				boolean anyMet = false;
				StringBuilder names = new StringBuilder();
				for (int i = 0; i < orGroup.length; i++) {
					if (pd.hasPurchasedUpgrade(orGroup[i])) anyMet = true;
					CaravanUpgrade r = CaravanUpgradeRegistry.get(orGroup[i]);
					if (i > 0) names.append(" or ");
					names.append(r != null ? r.getTitle() : orGroup[i]);
				}
				lore.add(line(Component.text(" - " + names, anyMet ? NamedTextColor.GREEN : NamedTextColor.RED)));
			}
		}

		if (available) {
			lore.add(Component.empty());
			lore.add(line(Component.text("Click to purchase", NamedTextColor.YELLOW)));
		}

		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private static Component line(Component c) {
		return c.decoration(TextDecoration.ITALIC, State.FALSE);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) return;
		CaravanUpgrade up = slotToUpgrade.get(e.getSlot());
		if (up == null) return;
		if (pd.hasPurchasedUpgrade(up.getId())) {
			Util.displayError(p, "You already own this upgrade!");
			return;
		}
		if (!up.requirementsMet(pd)) {
			Util.displayError(p, "You haven't unlocked the prerequisites for this upgrade!");
			return;
		}
		if (!RunReward.isEnabled()) {
			Util.displayError(p, "The economy is unavailable right now!");
			return;
		}
		if (!RunReward.charge(p.getUniqueId(), up.getCost())) {
			Util.displayError(p, "You can't afford this upgrade!");
			return;
		}
		up.apply(pd);
		p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.4F);
		Util.msgRaw(p, "<green>Purchased <yellow>" + up.getTitle() + "</yellow> for <yellow>"
				+ RunReward.formatMoney(up.getCost()) + "</yellow>!");
		render();
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
