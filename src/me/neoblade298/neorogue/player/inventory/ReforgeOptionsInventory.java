package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReforgeOptionsInventory extends CoreInventory {
	private PlayerSessionData data;
	private Equipment toReforge, reforgeWith;
	private ArrayList<Equipment> reforgeOptions = new ArrayList<Equipment>();
	public ReforgeOptionsInventory(PlayerSessionData data, Equipment toReforge, Equipment reforgeWith) {
		super(data.getPlayer(), Bukkit.createInventory(data.getPlayer(), 18, Component.text("Reforge Options", NamedTextColor.BLUE)));
		this.data = data;
		this.toReforge = toReforge;
		this.reforgeWith = reforgeWith;

		ItemStack[] contents = inv.getContents();
		Equipment[] options = toReforge.getReforgeOptions().get(reforgeWith.getUnupgraded());
		int offset = options.length - 5; // -5 for middle of inv, -1 for 0 offset at size 2
		contents[3] = toReforge.getItem();
		contents[5] = reforgeWith.getItem();
		for (int i = 0; i < options.length; i++) {
			Equipment eq = options[i];
			if (eq == null) {
				Bukkit.getLogger().warning("[NeoRogue] Failed to load reforge option " + options[i] + " for item " + toReforge.getId() + ", skipping");
				continue;
			}
			contents[(2 * i) - offset + 9] = eq.getItem();
			reforgeOptions.add(eq);
		}

		for (int i = 0; i < 9; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
		}
		for (int i = 9; i < 18; i++) {
			if (contents[i] != null) continue;
			contents[i] = CoreInventory.createButton(Material.RED_STAINED_GLASS_PANE, Component.text("Cancel", NamedTextColor.RED));
		}
		
		inv.setContents(contents);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getSlot() < 9) return;
		if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
			p.closeInventory();
		}
		else {
			if (e.isRightClick()) {
				new GlossaryInventory(p, getFromSlot(e.getSlot()), this);
				return;
			}
			Equipment reforged = getFromSlot(e.getSlot());
			p.playSound(p, Sound.BLOCK_ANVIL_USE, 1F, 1F);
			Component cmp = SharedUtil.color("<yellow>" + p.getName() + "</yellow> reforged their ").append(toReforge.getUnupgraded().getHoverable());
			if (!toReforge.getId().equals(reforgeWith.getId())) cmp = cmp.append(Component.text(", ").append(reforgeWith.getHoverable()));
			cmp = cmp.append(Component.text(" into a(n) ").append(reforged.getHoverable().append(Component.text("!"))));
			data.getSession().broadcast(cmp);
			
			data.giveEquipmentSilent(reforged);
			toReforge = null;
			reforgeWith = null;
			p.closeInventory();
		}
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		if (toReforge != null) {
			data.giveEquipment(toReforge, null, null);
			data.giveEquipment(reforgeWith, null, null);
		}
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		
	}
	
	private Equipment getFromSlot(int slot) {
		int offset = reforgeOptions.size() - 5;
		slot += offset;
		slot -= 9;
		return reforgeOptions.get(slot / 2);
	}
}
