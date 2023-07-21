package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;

public abstract class EquipmentInstance {
	private Equipment eq;
	private long lastUsed = 0L;
	private int cooldown = 0;
	
	public abstract boolean trigger(Object[] inputs);
	public boolean canTrigger() {
		return lastUsed + (cooldown * 1000) > System.currentTimeMillis();
	}
	
	public String getDisplay() {
		return eq.display;
	}
	
	public void sendCooldownMessage(Player p) {
		int cooldownOver = (int) ((lastUsed / 1000) + cooldown);
		int now = (int) (System.currentTimeMillis() / 1000);
		Util.msg(p, "&e" + eq.display + " &ccooldown: &e" + (cooldownOver - now) + "s");
	}
}
