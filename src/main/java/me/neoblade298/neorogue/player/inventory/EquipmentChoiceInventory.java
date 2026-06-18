package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.reward.RewardInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EquipmentChoiceInventory extends CoreInventory {
	private static final int BACK = 8;
	private RewardInventory prev;
	private ArrayList<SessionEquipment> equips;
	private int prevSlot;
	private PlayerSessionData data;
	private Player spectator;

	public EquipmentChoiceInventory(PlayerSessionData data, Inventory inv, ArrayList<SessionEquipment> equips, RewardInventory prev, int prevSlot) {
		super(data.getPlayer(), inv);
		setup(data, inv, equips, prev, prevSlot);
	}

	public EquipmentChoiceInventory(PlayerSessionData data, Inventory inv, ArrayList<SessionEquipment> equips, RewardInventory prev, int prevSlot, Player spectator) {
		super(spectator, inv);
		setup(data, inv, equips, prev, prevSlot);
		this.spectator = spectator;
	}
	
	private void setup(PlayerSessionData data, Inventory inv, ArrayList<SessionEquipment> equips, RewardInventory prev, int prevSlot) {
		this.data = data;
		this.prev = prev;
		this.prevSlot = prevSlot;
		this.equips = equips;
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < equips.size(); i++) {
			contents[i] = equips.get(i).getItem();
		}
		contents[BACK] = CoreInventory.createButton(Material.BARRIER, Component.text("Back", NamedTextColor.RED));
		inv.setContents(contents);
	}
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory iclicked = e.getClickedInventory();
		if (iclicked == null || iclicked.getType() != InventoryType.CHEST) return;
		if (e.getCurrentItem() == null) return;
		
		int slot = e.getSlot();
		
		if (slot == BACK) {
			prev.openInventory();
			return;
		}
		if (spectator != null) return;
		
		if (slot < equips.size()) {
			if (e.isRightClick()) {
				new EquipmentGlossaryInventory(p, equips.get(slot).getEquipment(), this);
				return;
			}
			
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
			SessionEquipment se = equips.get(slot);
			data.giveEquipment(se);
			if (prev.claimReward(prevSlot)) {
				prev.openInventory();
			}
			else {
				p.playSound(p, Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
				p.closeInventory();
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
