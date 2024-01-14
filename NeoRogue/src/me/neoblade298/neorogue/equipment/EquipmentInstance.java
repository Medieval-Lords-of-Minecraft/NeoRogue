package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EquipmentInstance extends PriorityAction {
	protected TriggerAction action;
	protected TriggerCondition condition;
	protected int slot;
	protected Equipment eq;
	protected double staminaCost, manaCost, cooldown;
	private long lastUsed = 0L;
	
	public EquipmentInstance(Equipment eq, int slot) {
		this.eq = eq;
		this.manaCost = eq.getProperties().getManaCost();
		this.staminaCost = eq.getProperties().getStaminaCost();
		this.cooldown = eq.getProperties().getCooldown();
		this.slot = slot;
	}
	
	public EquipmentInstance(Equipment eq, int slot, TriggerAction action) {
		this(eq, slot);
		this.action = action;
	}
	
	public EquipmentInstance(Equipment eq, int slot, TriggerAction action, TriggerCondition condition) {
		this(eq, slot, action);
		this.condition = condition;
	}
	
	// Apparently earthen tackle needs this
	public void setAction(TriggerAction action) {
		this.action = action;
	}
	
	@Override
	public TriggerResult trigger(PlayerFightData data, Object inputs) {
		lastUsed = System.currentTimeMillis();
		data.addMana(-manaCost);
		data.addStamina(-staminaCost);
		return action.trigger(data, inputs);
	}
	
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
		if (condition != null) {
			return condition.canTrigger(p, data);
		}
		return true;
	}
	
	public void sendCooldownMessage(Player p) {
		Util.msgRaw(p, eq.display.append(NeoCore.miniMessage().deserialize(" <red>cooldown: </red><yellow>" + getCooldownSeconds() + "s")));
	}
	
	public void reduceCooldown(int seconds) {
		lastUsed -= seconds * 1000;
	}
	
	public int getCooldownSeconds() {
		int nextUse = (int) ((lastUsed / 1000) + cooldown);
		int now = (int) (System.currentTimeMillis() / 1000);
		return Math.max(0, nextUse - now);
	}
	
	public int getCooldownTicks() {
		return getCooldownSeconds() * 20;
	}
	
	public void setStaminaCost(double stamina) {
		this.staminaCost = stamina;
	}
	
	public double getStaminaCost() {
		return staminaCost;
	}
	
	public void setManaCost(double mana) {
		this.manaCost = mana;
	}

	public double getManaCost() {
		return manaCost;
	}
	
	public Equipment getEquipment() {
		return eq;
	}
	
	public TriggerAction getAction() {
		return action;
	}
	
	public int getSlot() {
		return slot;
	}
}
