package me.neoblade298.neorogue.session.reward;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CoinsReward implements Reward {
	private int amount;
	
	public CoinsReward(int amount) {
		this.amount = amount;
	}
	
	public CoinsReward(String str) {
		this.amount = Integer.parseInt(str);
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		data.addCoins(amount);
		data.getPlayer().playSound(data.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
		Util.msg(data.getPlayer(), 
				NeoCore.miniMessage().deserialize("You claimed your reward of <yellow>" + amount
						+ " coins</yellow>. You now have <yellow>" + data.getCoins() + "</yellow>."));
		return true;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack item = new ItemStack(Material.GOLD_NUGGET);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(amount + " coins", NamedTextColor.YELLOW));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String serialize() {
		return "coins:" + amount;
	}

}
