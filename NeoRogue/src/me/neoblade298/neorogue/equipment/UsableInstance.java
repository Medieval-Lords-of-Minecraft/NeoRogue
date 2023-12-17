package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public abstract class UsableInstance implements TriggerAction {
	protected Usable u;
	protected double staminaCost, manaCost, cooldown;
	private long lastUsed = 0L;
	
	public UsableInstance(Usable u) {
		this.u = u;
		this.manaCost = u.manaCost;
		this.staminaCost = u.staminaCost;
		this.cooldown = u.cooldown;
	}
	
	@Override
	public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
		lastUsed = System.currentTimeMillis();
		data.addMana(-manaCost);
		data.addStamina(-staminaCost);
		return run(data, inputs);
	}
	
	public abstract TriggerResult run(PlayerFightData data, Object[] inputs);
	
	public boolean canTrigger(Player p, PlayerFightData data) {
		if (lastUsed + (cooldown * 1000) >= System.currentTimeMillis()) {
			sendCooldownMessage(p);
			return false;
		}
		if (data.getMana() <= manaCost) {
			Util.displayError(data.getPlayer(), "Not enough mana!");
			return false;
		}
		
		if (data.getStamina() <= staminaCost) {
			Util.displayError(data.getPlayer(), "Not enough stamina!");
			return false;
		}
		return true;
	}
	
	public void sendCooldownMessage(Player p) {
		Util.msgRaw(p, u.display.append(NeoCore.miniMessage().deserialize(" <red>cooldown: </red><yellow>" + getCooldown() + "s")));
	}
	
	public void reduceCooldown(int seconds) {
		lastUsed -= seconds * 1000;
	}
	
	public int getCooldown() {
		int nextUse = (int) ((lastUsed / 1000) + cooldown);
		int now = (int) (System.currentTimeMillis() / 1000);
		return Math.max(0, nextUse - now);
	}
}
