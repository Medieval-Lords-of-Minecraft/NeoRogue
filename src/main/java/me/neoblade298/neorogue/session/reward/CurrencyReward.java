package me.neoblade298.neorogue.session.reward;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neorogue.player.PlayerSessionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CurrencyReward implements Reward {
	private int amount;
	
	public CurrencyReward(int amount) {
		this.amount = amount;
	}
	
	public CurrencyReward(String str) {
		this.amount = Integer.parseInt(str);
	}

	@Override
	public boolean claim(PlayerSessionData data, int slot, RewardInventory inv) {
		data.addCurrency(amount);
		data.getPlayer().playSound(data.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
		return true;
	}

	@Override
	public ItemStack getIcon(PlayerSessionData data) {
		ItemStack item = new ItemStack(Material.NETHERITE_SCRAP);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(amount + " " + PlayerSessionData.CURRENCY, NamedTextColor.YELLOW));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String serialize() {
		return "coins:" + amount;
	}

}
