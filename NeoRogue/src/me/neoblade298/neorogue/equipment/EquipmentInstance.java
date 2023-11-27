package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.player.TriggerAction;

public abstract class EquipmentInstance implements TriggerAction {
	private Equipment eq;
	private long lastUsed = 0L;
	protected int cooldown = 0;
	private boolean isCancelled = false;
	
	public EquipmentInstance(Equipment eq) {
		this.eq = eq;
	}
	
	@Override
	public boolean trigger(Object[] inputs) {
		lastUsed = System.currentTimeMillis();
		return run(inputs);
	}
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}
	public abstract boolean run(Object[] inputs);
	
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	public boolean canTrigger() {
		return lastUsed + (cooldown * 1000) < System.currentTimeMillis();
	}
	
	public String getDisplay() {
		return eq.display;
	}
	
	public void sendCooldownMessage(Player p) {
		int cooldownOver = (int) ((lastUsed / 1000) + cooldown);
		int now = (int) (System.currentTimeMillis() / 1000);
		Util.msgRaw(p, NeoCore.miniMessage().deserialize("<yellow>" + eq.display + " <red>cooldown: </red>" + (cooldownOver - now) + "s"));
	}
	
	public Equipment getEquipment() {
		return eq;
	}
}
