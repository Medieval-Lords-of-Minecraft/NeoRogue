package me.neoblade298.neorogue.equipment;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EquipmentInstance extends PriorityAction {
	private static HashMap<Integer, Material> COOLDOWN_MATERIALS = new HashMap<Integer, Material>();
	private static final DecimalFormat df = new DecimalFormat("##.#");
	
	protected ItemStack icon;
	protected Player p;
	protected PlayerFightData data;
	protected int slot, invSlot;
	protected Equipment eq;
	protected EquipSlot es;
	protected double staminaCost, manaCost, tempStaminaCost = -1, tempManaCost = -1, nextStaminaCost = -1, nextManaCost = -1, cooldown, tempCooldown = -1;
	protected long nextUsable = 0L;
	protected BukkitTask cooldownTask;
	protected String cooldownTaskId;
	
	static {
		COOLDOWN_MATERIALS.put(0, Material.RED_CANDLE);
		COOLDOWN_MATERIALS.put(1, Material.ORANGE_CANDLE);
		COOLDOWN_MATERIALS.put(2, Material.YELLOW_CANDLE);
		COOLDOWN_MATERIALS.put(3, Material.LIME_CANDLE);
		COOLDOWN_MATERIALS.put(4, Material.GREEN_CANDLE);
		COOLDOWN_MATERIALS.put(5, Material.CYAN_CANDLE);
		COOLDOWN_MATERIALS.put(6, Material.LIGHT_BLUE_CANDLE);
		COOLDOWN_MATERIALS.put(7, Material.BLUE_CANDLE);
		COOLDOWN_MATERIALS.put(8, Material.PURPLE_CANDLE);
		COOLDOWN_MATERIALS.put(27, Material.MAGENTA_CANDLE);
		COOLDOWN_MATERIALS.put(28, Material.PINK_CANDLE);
		COOLDOWN_MATERIALS.put(29, Material.WHITE_CANDLE);
		COOLDOWN_MATERIALS.put(30, Material.LIGHT_GRAY_CANDLE);
		COOLDOWN_MATERIALS.put(31, Material.GRAY_CANDLE);
		COOLDOWN_MATERIALS.put(32, Material.BLACK_CANDLE);
		COOLDOWN_MATERIALS.put(33, Material.BROWN_CANDLE);
	}
	
	public EquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
		super(eq.id);
		init(data, eq, slot, es);
	}
	
	public EquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, TriggerAction action) {
		super(eq.id, action);
		init(data, eq, slot, es);
	}
	
	public EquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, TriggerAction action, TriggerCondition condition) {
		super(eq.id, action, condition);
		init(data, eq, slot, es);
	}

	private void init(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
		this.data = data;
		this.eq = eq;
		this.manaCost = eq.getProperties().get(PropertyType.MANA_COST);
		this.staminaCost = eq.getProperties().get(PropertyType.STAMINA_COST);
		this.cooldown = eq.getProperties().get(PropertyType.COOLDOWN);
		this.slot = slot;
		this.invSlot = EquipSlot.convertSlot(es, slot);
		this.es = es;
		this.p = data.getPlayer();
		this.icon = eq.getItem();
		updateIcon();
	}

	public void setIcon(ItemStack icon) {
		this.icon = icon;
		updateIcon();
	}
	
	public EquipSlot getEquipSlot() {
		return es;
	}
	
	// Apparently earthen tackle needs this
	public void setAction(TriggerAction action) {
		this.action = action;
	}

	public void setCondition(TriggerCondition cond) {
		this.condition = cond;
	}
	
	@Override
	public TriggerResult trigger(PlayerFightData data, Object inputs) {
		if (!data.isIgnoreCooldowns()) nextUsable = (long) (System.currentTimeMillis() + (getEffectiveCooldown() * 1000));
		data.addMana(-getEffectiveManaCost());
		data.addStamina(-getEffectiveStaminaCost());
		resetTempCosts();
		return action.trigger(data, inputs);
	}
	
	public Player getPlayer() {
		return data.getPlayer();
	}
	
	@Override
	public boolean canTrigger(Player p, PlayerFightData data) {
		if (nextUsable >= System.currentTimeMillis()) {
			sendCooldownMessage(p);
			return false;
		}
		if (data.getMana() < getEffectiveManaCost()) {
			Util.displayError(data.getPlayer(), "You need " + df.format(getEffectiveManaCost() - data.getMana()) + " more mana!");
			return false;
		}
		
		if (data.getStamina() < getEffectiveStaminaCost()) {
			Util.displayError(data.getPlayer(), "You need " + df.format(getEffectiveStaminaCost() - data.getStamina()) + " more stamina!");
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
	
	public void updateIcon() {
		int cooldownSeconds = getCooldownSeconds();
		PlayerInventory inv = p.getInventory();
		if (cooldownSeconds <= 0) {	// For setting icon when not on cooldown
			p.getInventory().setItem(invSlot, icon);
			return;
		}
		Material mat = COOLDOWN_MATERIALS.get(invSlot);
		if (inv.getItem(invSlot) == null) return;
		ItemStack item = inv.getItem(invSlot).withType(mat);
		item.setAmount(1);
		inv.setItem(invSlot, item);
		
		p.setCooldown(mat, cooldownSeconds * 20);

		if (cooldownTaskId != null) {
			cooldownTask.cancel();
			data.removeTask(cooldownTaskId);
		}
		
		cooldownTaskId = UUID.randomUUID().toString();
		cooldownTask = new BukkitRunnable() {
			public void run() {
				inv.setItem(invSlot, icon);
				cooldownTaskId = null;
				cooldownTask = null;
			}
		}.runTaskLater(NeoRogue.inst(), cooldownSeconds * 20);
		data.addTask(cooldownTaskId, cooldownTask);
	}
	
	public double getBaseCooldown() {
		return cooldown;
	}
	
	public void setTempCooldown(double cooldown) {
		this.tempCooldown = cooldown;
	}
	
	public void addCooldown(double seconds) {
		nextUsable += seconds * 1000;
		updateIcon();
	}
	
	public void setCooldown(int seconds) {
		nextUsable = System.currentTimeMillis() + (seconds * 1000);
		updateIcon();
	}
	
	public void reduceCooldown(double seconds) {
		nextUsable -= seconds * 1000;
		updateIcon();
	}
	
	public int getCooldownSeconds() {
		return (int) (Math.max(0, (nextUsable - System.currentTimeMillis() + 1000) / 1000));
	}
	
	public int getCooldownTicks() {
		return getCooldownSeconds() * 20;
	}
	
	public void setTempStaminaCost(double stamina) {
		this.tempStaminaCost = stamina;
	}
	
	public void setTempManaCost(double mana) {
		this.tempManaCost = mana;
	}
	
	public void setNextStaminaCost(double stamina) {
		this.nextStaminaCost = stamina;
	}
	
	public void setNextManaCost(double mana) {
		this.nextManaCost = mana;
	}
	
	public double getStaminaCost() {
		return this.staminaCost;
	}
	
	public double getTempStaminaCost() {
		return tempStaminaCost;
	}
	
	public double getEffectiveStaminaCost() {
		return tempStaminaCost == -1 ? staminaCost : tempStaminaCost;
	}

	public double getManaCost() {
		return this.manaCost;
	}
	
	public double getTempManaCost() {
		return tempManaCost;
	}
	
	public double getEffectiveManaCost() {
		return tempManaCost == -1 ? manaCost : tempManaCost;
	}
	
	public double getEffectiveCooldown() {
		return tempCooldown == -1 ? cooldown : tempCooldown;
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
	
	public void resetTempCosts() {
		tempStaminaCost = nextStaminaCost;
		tempManaCost = nextManaCost;
		tempCooldown = -1;
		nextStaminaCost = -1;
		nextManaCost = -1;
	}
}
