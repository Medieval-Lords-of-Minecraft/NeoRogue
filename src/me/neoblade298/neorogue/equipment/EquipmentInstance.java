package me.neoblade298.neorogue.equipment;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EquipmentInstance extends PriorityAction {
	private static HashMap<Integer, Material> COOLDOWN_MATERIALS = new HashMap<Integer, Material>();
	
	protected Player p;
	protected TriggerAction action;
	protected TriggerCondition condition;
	protected int slot;
	protected Equipment eq;
	protected EquipSlot es;
	protected double staminaCost, manaCost, tempStaminaCost = -1, tempManaCost = -1, cooldown;
	protected long nextUsable = 0L;
	
	static {
		COOLDOWN_MATERIALS.put(0, Material.RED_CANDLE);
		COOLDOWN_MATERIALS.put(1, Material.ORANGE_CANDLE);
		COOLDOWN_MATERIALS.put(2, Material.YELLOW_CANDLE);
		COOLDOWN_MATERIALS.put(3, Material.LIME_CANDLE);
		COOLDOWN_MATERIALS.put(4, Material.GREEN_CANDLE);
		COOLDOWN_MATERIALS.put(5, Material.CYAN_CANDLE);
		COOLDOWN_MATERIALS.put(6, Material.BLUE_CANDLE);
		COOLDOWN_MATERIALS.put(7, Material.PURPLE_CANDLE);
		COOLDOWN_MATERIALS.put(8, Material.MAGENTA_CANDLE);
	}
	
	public EquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es) {
		super(eq.id);
		this.p = p;
		this.eq = eq;
		this.manaCost = eq.getProperties().get(PropertyType.MANA_COST);
		this.staminaCost = eq.getProperties().get(PropertyType.STAMINA_COST);
		this.cooldown = eq.getProperties().get(PropertyType.COOLDOWN);
		this.slot = slot;
		this.es = es;
	}
	
	public EquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es, TriggerAction action) {
		this(p, eq, slot, es);
		this.action = action;
	}
	
	public EquipmentInstance(Player p, Equipment eq, int slot, EquipSlot es, TriggerAction action, TriggerCondition condition) {
		this(p, eq, slot, es, action);
		this.condition = condition;
	}
	
	public EquipSlot getEquipSlot() {
		return es;
	}
	
	// Apparently earthen tackle needs this
	public void setAction(TriggerAction action) {
		this.action = action;
	}
	
	@Override
	public TriggerResult trigger(PlayerFightData data, Object inputs) {
		nextUsable = (long) (System.currentTimeMillis() + (cooldown * 1000));
		data.addMana(tempManaCost != -1 ? -tempManaCost : -manaCost);
		data.addStamina(tempStaminaCost != -1 ? -tempStaminaCost : -staminaCost);
		tempStaminaCost = -1;
		tempManaCost = -1;
		return action.trigger(data, inputs);
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public boolean canTrigger(Player p, PlayerFightData data) {
		if (nextUsable >= System.currentTimeMillis()) {
			sendCooldownMessage(p);
			return false;
		}
		if (data.getMana() <= (tempManaCost != -1 ? tempManaCost : manaCost)) {
			Util.displayError(data.getPlayer(), "Not enough mana!");
			return false;
		}
		
		if (data.getStamina() <= (tempStaminaCost != -1 ? tempStaminaCost : staminaCost)) {
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
	
	public static void updateSlot(Player p, PlayerInventory inv, EquipmentInstance inst) {
		updateSlot(p, inv, inst.es, inst.slot, inst.getCooldownSeconds());
	}
	
	public static void updateSlot(Player p, PlayerInventory inv, EquipSlot es, int slot, int cooldownSeconds) {
		if (es != EquipSlot.HOTBAR) return;
		Material mat = COOLDOWN_MATERIALS.get(slot);
		inv.setItem(slot, new ItemStack(mat));
		p.setCooldown(mat, cooldownSeconds * 20);
	}
	
	public void addCooldown(int seconds) {
		nextUsable += seconds * 1000;
		updateSlot(p, p.getInventory(), es, slot, getCooldownSeconds());
	}
	
	public void setCooldown(int seconds) {
		nextUsable = System.currentTimeMillis() + (seconds * 1000);
		updateSlot(p, p.getInventory(), es, slot, seconds);
	}
	
	public void reduceCooldown(int seconds) {
		nextUsable -= seconds * 1000;
		updateSlot(p, p.getInventory(), es, slot, getCooldownSeconds());
	}
	
	public int getCooldownSeconds() {
		return (int) (Math.max(0, nextUsable - System.currentTimeMillis()) / 1000);
	}
	
	public int getCooldownTicks() {
		return getCooldownSeconds() * 20;
	}
	
	public void setStaminaCost(double stamina) {
		this.staminaCost = stamina;
	}
	
	public void setTempStaminaCost(double stamina) {
		this.tempStaminaCost = stamina;
	}
	
	public void setTempManaCost(double mana) {
		this.tempManaCost = mana;
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