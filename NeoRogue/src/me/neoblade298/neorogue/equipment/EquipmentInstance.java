package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.TriggerAction;

public abstract class EquipmentInstance implements TriggerAction {
	private Equipment eq;
	private long lastUsed = 0L;
	protected int cooldown = 0;
	
	public EquipmentInstance(Equipment eq) {
		this.eq = eq;
	}
	
	public boolean trigger(Object[] inputs) {
		lastUsed = System.currentTimeMillis();
		return run(inputs);
	}
	public abstract boolean run(Object[] inputs);
	public boolean canTrigger() {
		return lastUsed + (cooldown * 1000) < System.currentTimeMillis();
	}
	
	public String getDisplay() {
		return eq.display;
	}
	
	public void sendCooldownMessage(Player p) {
		int cooldownOver = (int) ((lastUsed / 1000) + cooldown);
		int now = (int) (System.currentTimeMillis() / 1000);
		Util.msgRaw(p, "&e" + eq.display + " &ccooldown: &e" + (cooldownOver - now) + "s");
	}
	
	public Equipment getEquipment() {
		return eq;
	}
}
