package me.neoblade298.neorogue.session.chance;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.FightInfoInventory;
import me.neoblade298.neorogue.player.inventory.GlossaryInventory;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChanceInventory extends CoreInventory {
	private Session s;
	private ChanceInstance inst;
	private ChanceSet set;
	private ChanceStage stage;
	private PlayerSessionData data;
	private boolean asSpectator;

	public ChanceInventory(Player p, ChanceInstance inst, ChanceSet set, ChanceStage stage) {
		super(p, Bukkit.createInventory(p, 18, Component.text("Chance Event", NamedTextColor.BLUE)));
		this.set = set;
		this.inst = inst;
		this.s = inst.getSession();
		this.stage = stage;
		this.data = inst.getSession().getData(p.getUniqueId());
		InventoryListener.registerPlayerInventory(p, new PlayerSessionInventory(data));
		setupInventory();
	}

	public ChanceInventory(PlayerSessionData data, ChanceInstance inst, ChanceSet set, ChanceStage stage, Player spectator) {
		super(spectator, Bukkit.createInventory(data.getPlayer(), 18, Component.text(data.getData().getDisplay() + "'s Chance Event", NamedTextColor.BLUE)));
		this.set = set;
		this.inst = inst;
		this.data = data;
		this.s = inst.getSession();
		this.stage = stage;
		this.asSpectator = true;
		setupInventory();
	}
	
	private void setupInventory() {
		// Create title
		ItemStack[] contents = new ItemStack[18];
		ItemStack title = CoreInventory.createButton(set.getMaterial(), set.getDisplay());
		ItemMeta meta = title.getItemMeta();
		meta.lore(stage.description);
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
			contents[0] = CoreInventory.createButton(Material.ENCHANTED_BOOK, Component.text("Fight Info", NamedTextColor.BLUE));
		}
		inv.setContents(contents);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getRawSlot() == 0 && e.getCurrentItem() != null) {
			new FightInfoInventory(p, data, ((FightInstance) inst.getNextInstance()).getMap().getMobs());
		}
		Player p = (Player) e.getWhoClicked();
		UUID uuid = p.getUniqueId();
		Inventory inv = e.getClickedInventory();
		if (inv == null || inv.getType() != InventoryType.CHEST) return;
		
		
		ItemStack item = e.getCurrentItem();
		if (item == null) return;
		NBTItem nbti = new NBTItem(item);
		int num = nbti.getInteger("choice");
		if (num == 0) return;
		ChanceChoice choice = stage.choices.get(num - 1);
		ChanceInventory ci = this;

		if (e.isRightClick() && !choice.getTags().isEmpty()) {
			new BukkitRunnable() {
				public void run() {
					new GlossaryInventory(p, choice, ci);
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		if (asSpectator) return;
		
		if (!set.isIndividual() && !uuid.equals(s.getHost())) {
			if (!s.canSuggest()) return;
			s.setSuggestCooldown();
			s.broadcast(
				p.name().color(NamedTextColor.YELLOW)
				.append(Component.text(" suggests the choice ", NamedTextColor.GRAY))
				.append(choice.getItemWithoutConditions().displayName())
			);
			s.broadcastSound(Sound.ENTITY_ARROW_HIT_PLAYER);
			new BukkitRunnable() {
				public void run() {
					p.closeInventory();
				}
			}.runTask(NeoRogue.inst());
			return;
		}
		if (!choice.canChoose(s, inst, s.getData(uuid))) {
			Util.displayError(p, "You aren't eligible for this option!");
			return;
		}
		
		ChanceStage next = set.getStage(choice.choose(s, inst, data));
		inst.advanceStage(uuid, next);
		new BukkitRunnable() {
			public void run() {
				p.closeInventory();
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	private ItemStack getChoiceItem(int num) {
		ItemStack item = stage.choices.get(num).getItem(s, data);
		NBTItem nbti = new NBTItem(item);
		nbti.setInteger("choice", num + 1);
		return nbti.getItem();
	}
}
