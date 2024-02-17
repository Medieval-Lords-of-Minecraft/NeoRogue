package me.neoblade298.neorogue.player.inventory;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.*;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class SpectateSelectInventory extends CoreInventory {
	private HashMap<Integer, PlayerSessionData> players = new HashMap<Integer, PlayerSessionData>();
	private Session s;
	private boolean selectUnique;

	public SpectateSelectInventory(Session s, Player p, boolean selectUnique) {
		super(p, Bukkit.createInventory(p, 9, Component.text("Choose a player", NamedTextColor.BLUE)));
		this.s = s;
		this.selectUnique = selectUnique;
		p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
		int partySize = s.getParty().size();
		if (s.getParty().containsKey(p.getUniqueId())) partySize--;
		int idx = 5 - partySize;
		
		// Display all players that aren't you
		for (PlayerSessionData data : s.getParty().values()) {
			if (p.getUniqueId().equals(data.getUniqueId())) continue;
	        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
	        SkullMeta meta = (SkullMeta) skull.getItemMeta();
	        meta.setOwningPlayer(data.getPlayer() != null ? data.getPlayer() : Bukkit.getOfflinePlayer(data.getUniqueId()));
	        meta.displayName(Component.text(data.getData().getDisplay(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE));
	        skull.setItemMeta(meta);
			inv.setItem(idx, skull);
			players.put(idx, data);
			idx += 2;
		}
	}

	@Override
	public void handleInventoryClose(InventoryCloseEvent e) {
		
	}

	@Override
	public void handleInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(true);
	}

	@Override
	public void handleInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		if (e.getCurrentItem() == null) return;
		if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
		
		Instance inst = s.getInstance();
		if (!selectUnique && inst instanceof EditInventoryInstance) {
			new PlayerSessionInventory(players.get(e.getSlot()), p);
			return;
		}
		
		UUID viewed = players.get(e.getSlot()).getUniqueId();
		if (inst instanceof ChanceInstance) {
			((ChanceInstance) inst).spectatePlayer(p, viewed);
		}
		else if (inst instanceof ShopInstance) {
			((ShopInstance) inst).spectateShop(p, viewed);
		}
		else if (inst instanceof RewardInstance) {
			((RewardInstance) inst).spectateRewards(p, viewed);
		}
	}
}
