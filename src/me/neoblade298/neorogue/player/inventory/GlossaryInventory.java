package me.neoblade298.neorogue.player.inventory;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.Mob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GlossaryInventory extends CoreInventory {
	private CoreInventory prev;
	private boolean openOther = true;
	
	private static final int BASIC = 0, UPGRADED = 1, REFORGE_OFFSET = 3;
	public GlossaryInventory(Player viewer, Mob mob, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, calculateSize(mob.getTags().size()),
				Component.text("Glossary: ").append(LegacyComponentSerializer.legacyAmpersand()
						.deserialize(NeoRogue.mythicApi.getMythicMob(mob.getId()).getDisplayName().get()))));
		this.prev = prev;
		Sounds.turnPage.play(p, p);
		
		// Glossary tags
		ItemStack[] contents = inv.getContents();
		Iterator<GlossaryIcon> iter = mob.getTags().iterator();
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 9; col++) {
				if (!iter.hasNext()) break;
				contents[(row * 9) + col] = iter.next().getIcon();
			}
		}
		inv.setContents(contents);
	}
	public GlossaryInventory(Player viewer, Equipment eq, CoreInventory prev) {
		super(viewer, Bukkit.createInventory(viewer, calculateSize(eq), Component.text("Glossary: ").append(eq.getUnupgraded().getDisplay())));
		this.prev = prev;
		eq = eq.getUnupgraded();
		Sounds.turnPage.play(p, p);
		
		ItemStack[] contents = inv.getContents();
		contents[BASIC] = eq.getUnupgraded().getItem();
		contents[UPGRADED] = eq.canUpgrade() ? eq.getUpgraded().getItem() : null;
		
		// Glossary tags
		Iterator<GlossaryIcon> iter = eq.getTags().iterator();
		for (int row = 1; row < 6; row++) {
			for (int col = 0; col < 5; col++) {
				if (!iter.hasNext()) break;
				contents[(row * 9) + col] = iter.next().getIcon();
			}
		}
		
		int reforgeLabel = REFORGE_OFFSET;
		if (!eq.getReforgeParents().isEmpty()) {
			reforgeLabel += 9;
			contents[REFORGE_OFFSET] = CoreInventory.createButton(Material.IRON_BLOCK, Component.text("Reforge Parents:", NamedTextColor.YELLOW));
			int idx = 0;
			for (Equipment parent : eq.getReforgeParents()) {
				contents[REFORGE_OFFSET + ++idx] = parent.getItem();
			}
		}
		
		// Reforge options
		if (eq.getReforgeOptions().isEmpty()) {
			contents[reforgeLabel] = CoreInventory.createButton(Material.BARRIER, Component.text("No reforge options", NamedTextColor.RED));
		}
		else {
			contents[reforgeLabel] = CoreInventory.createButton(Material.GOLD_BLOCK, Component.text("Reforge Options:", NamedTextColor.YELLOW));
			int row = 0;
			for (Entry<Equipment, Equipment[]> ent : eq.getReforgeOptions().entrySet()) {
				int col = reforgeLabel + 1;
				contents[(row * 9) + col++] = ent.getKey().getItem();
				for (Equipment option : ent.getValue()) {
					contents[(row * 9) + col++] = option.getItem();
				}
				row++;
			}
		}
		inv.setContents(contents);
	}
	
	private static int calculateSize(int numTags) {
		return numTags + (9 - numTags % 9);
	}
	
	private static int calculateSize(Equipment eq) {
		int leftHeight = 1 + ((eq.getTags().size() + 3) / 4);
		int rightHeight = eq.getReforgeOptions().size() + (!eq.getReforgeParents().isEmpty() ? 1 : 0);
		return 9 * Math.max(leftHeight, rightHeight);
	}
	
	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (!e.isRightClick()) return;
		if (e.getCurrentItem() == null) return;
		NBTItem nbti = new NBTItem(e.getCurrentItem());
		if (!nbti.getKeys().contains("equipId")) return;
		openOther = false;
		new BukkitRunnable() {
			public void run() {
				new GlossaryInventory(p, Equipment.get(nbti.getString("equipId"), false), prev);
			}
		}.runTask(NeoRogue.inst());
	}
	
	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		// Don't open the previous inventory if we're opening another glossary page
		if (openOther) {
			new BukkitRunnable() {
				public void run() {
					if (prev != null) prev.openInventory();
				}
			}.runTask(NeoRogue.inst());
		}
	}
	
	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}
}
