package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.FightInfoInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fights.FightInstance;

public class ChanceInventory extends CoreInventory {
	private Session s;
	private ChanceInstance inst;
	private ChanceSet set;
	private ChanceStage stage;

	public ChanceInventory(Player p, ChanceInstance inst, ChanceSet set, ChanceStage stage) {
		super(p, Bukkit.createInventory(p, 18, "§9Chance Event"));
		this.set = set;
		this.inst = inst;
		this.s = inst.getSession();
		this.stage = stage;
		setupInventory();
	}
	
	private void setupInventory() {
		// Create title
		ItemStack[] contents = new ItemStack[18];
		ItemStack title = CoreInventory.createButton(set.getMaterial(), set.getDisplay());
		ItemMeta meta = title.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.addAll(stage.description);
		meta.setLore(lore);
		title.setItemMeta(meta);
		contents[4] = title;
		
		// Setup choices (currently only supports up to 9)
		int size = stage.choices.size();
		int offset = 4 - (size / 2);
		if (stage.choices.size() % 2 == 0) {
			for (int i = 0; i < size / 2; i++) {
				contents[offset + i + 9] = getChoiceItem(i);
			}
			for (int i = (size / 2); i < size; i++) {
				contents[offset + i + 10] = getChoiceItem(i);
			}
		}
		else {
			for (int i = 0; i < size; i++) {
				contents[offset + i + 9] = getChoiceItem(i);
			}
		}
		
		// Specific setup for if the chance led to a fight instance
		if (inst.getNextInstance() instanceof FightInstance) {
			contents[0] = CoreInventory.createButton(Material.ENCHANTED_BOOK, "&9Fight Info");
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		Inventory inv = e.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.CHEST) return;
		
		if (e.getRawSlot() == 0 && e.getCurrentItem() != null) {
			new FightInfoInventory(p, ((FightInstance) inst.getNextInstance()).getMap().getMobs());
		}
		
		ItemStack item = e.getCurrentItem();
		if (item == null) return;
		NBTItem nbti = new NBTItem(item);
		int num = nbti.getInteger("choice");
		if (num == 0) return;
		inst.advanceStage(stage.choices.get(num - 1).choose(s, inst));
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private ItemStack getChoiceItem(int num) {
		ItemStack item = stage.choices.get(num).getItem(s);
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("choice", num + 1);
		return nbti.getItem();
	}
}