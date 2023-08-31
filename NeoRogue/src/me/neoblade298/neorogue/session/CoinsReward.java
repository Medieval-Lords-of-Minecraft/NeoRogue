package me.neoblade298.neorogue.session;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class CoinsReward implements Reward {
	private int amount;
	
	public CoinsReward(int amount) {
		this.amount = amount;
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		data.addCoins(amount);
		data.getPlayer().playSound(data.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
		Util.msg(data.getPlayer(), "You claimed your reward of &e" + amount + " coins&7. You now have &e" + data.getCoins() + "&7.");
		return true;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack item = new ItemStack(Material.GOLD_NUGGET);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§e" + amount + " coins");
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String serialize() {
		return "coins-" + amount;
	}

}
