package me.neoblade298.neorogue.session;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class EquipmentReward implements Reward {
	private int amount;
	
	public EquipmentReward(int amount) {
		this.amount = amount;
	}

	@Override
	public boolean claim(PlayerSessionData data) {
		data.addCoins(amount);
		Util.msg(data.getPlayer(), "You claimed your reward of &e" + amount + " coins&7. You now have &e" + data.getCoins() + "&7.");
		return false;
	}

}
