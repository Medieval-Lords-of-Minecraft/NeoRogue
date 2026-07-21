package me.neoblade298.neorogue.player.inventory;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBT;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import net.kyori.adventure.text.Component;

public class GlossaryViewInventory extends GlossaryInventory {

	public GlossaryViewInventory(Player p, ArrayList<Equipment> equips, Component title, CoreInventory prev) {
		super(p, calculateSize(equips.size()), title, prev);

		Session session = SessionManager.getSession(p);
		PlayerSessionData data = session != null ? session.getData(p.getUniqueId()) : null;
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < equips.size() && i < contents.length; i++) {
			contents[i] = equips.get(i).getChoiceItem(data);
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return;
		if (!e.isRightClick()) return;
		String equipId = NBT.get(e.getCurrentItem(), nbt -> nbt.getKeys().contains("equipId") ? nbt.getString("equipId") : null);
		if (equipId == null) return;
		openOther = false;
		new BukkitRunnable() {
			public void run() {
				new EquipmentGlossaryInventory(p, Equipment.get(equipId, false), GlossaryViewInventory.this);
			}
		}.runTask(NeoRogue.inst());
	}

	protected static int calculateSize(int numItems) {
		int size = numItems + (9 - numItems % 9);
		if (size < 9) size = 9;
		if (size > 54) size = 54;
		return size;
	}
}
