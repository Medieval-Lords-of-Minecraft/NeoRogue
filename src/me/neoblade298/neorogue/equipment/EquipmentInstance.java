package me.neoblade298.neorogue.equipment;

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
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class EquipmentInstance extends PriorityAction {
	private static HashMap<Integer, Material> COOLDOWN_MATERIALS = new HashMap<Integer, Material>();

	protected ItemStack icon;
	protected Player p;
	protected PlayerFightData data;
	protected int slot, invSlot;
	protected Equipment eq;
	protected EquipSlot es;
	protected double staminaCost, manaCost, cooldown; // Base costs
	protected CastUsableEvent lastCastEvent; // Useful for non-standard cast types that may need to access final mana/stamina cost info for refunds, like Gravity
	protected TriggerCondition resourceUsageCondition;
	protected long nextUsable = 0L;
	protected BukkitTask cooldownTask;
	protected String cooldownTaskId;
	// If false, do not automatically trigger CAST_USABLE. MUST be done manually.
	// Useful for toggleable abilities and abilities that can fail post-trigger
	// (like ground lance requiring a block to be aimed at).
	protected boolean castsUsable = true;

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
		COOLDOWN_MATERIALS.put(40, Material.CANDLE);
	}

	public EquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
		super(eq.id);
		init(data, eq, slot, es);
	}

	public EquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, TriggerAction action) {
		super(eq.id, action);
		init(data, eq, slot, es);
	}

	public EquipmentInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es, TriggerAction action,
			TriggerCondition condition) {
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

	// Used when a player relogs to update the player reference
	public void updatePlayer(Player p) {
		this.p = data.getPlayer();
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

	public Player getPlayer() {
		return data.getPlayer();
	}

	@Override
	public boolean canTrigger(Player p, PlayerFightData data, Object in) {
		if (nextUsable >= System.currentTimeMillis()) {
			sendCooldownMessage(p);
			return false;
		}
		if (condition != null) {
			return condition.canTrigger(p, data, in);
		}
		return true;
	}

	public void sendCooldownMessage(Player p) {
		Util.msgRaw(p, eq.display.append(
				NeoCore.miniMessage().deserialize(" <red>cooldown: </red><yellow>" + getCooldownSeconds() + "s")));
	}

	public void setResourceUsageCondition(TriggerCondition cond) {
		this.resourceUsageCondition = cond;
	}

	public boolean shouldUseResource(Player p, PlayerFightData data, Object event) {
		return resourceUsageCondition == null || resourceUsageCondition.canTrigger(p, data, event);
	}

	public void updateIcon() {
		int cooldownSeconds = getCooldownSeconds();
		PlayerInventory inv = p.getInventory();
		if (cooldownSeconds <= 0) { // For setting icon when not on cooldown
			p.getInventory().setItem(invSlot, icon);
			return;
		}
		Material mat = COOLDOWN_MATERIALS.get(invSlot);
		if (inv.getItem(invSlot) == null)
			return;
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
		data.addTask(cooldownTask);
	}

	public double getBaseCooldown() {
		return cooldown;
	}

	public void addCooldown(double seconds) {
		nextUsable += seconds * 1000;
		updateIcon();
	}

	public void setCooldown(double seconds) {
		// Set cooldown to -1 to remove cooldown, otherwise updateIcon will assume it's
		// on 1s cooldown
		nextUsable = seconds == 0 ? -1 : System.currentTimeMillis() + (long)((int) (seconds * 1000));
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

	public double getStaminaCost() {
		return this.staminaCost;
	}

	public double getManaCost() {
		return this.manaCost;
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

	// Only run if your cast type is non-standard
	public void setLastCastEvent(CastUsableEvent ev) {
		this.lastCastEvent = ev;
	}
	public CastUsableEvent getLastCastEvent() {
		return this.lastCastEvent;
	}
}
