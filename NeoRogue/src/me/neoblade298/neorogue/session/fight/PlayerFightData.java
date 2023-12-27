package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;

public class PlayerFightData extends FightData {
	private PlayerSessionData sessdata;
	private HashMap<Trigger, HashMap<String, TriggerAction>> triggers = new HashMap<Trigger, HashMap<String, TriggerAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>();
	private HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>>();
	private Player p;
	private long nextAttack, nextOffAttack;

	private double stamina = 0, mana = 0;
	private double maxStamina, maxMana, maxHealth;
	private double staminaRegen, manaRegen;
	
	private FightStatistics stats = new FightStatistics();

	public PlayerFightData(FightInstance inst, PlayerSessionData data) {
		super(data.getPlayer(), inst);
		p = data.getPlayer();
		
		this.inst = inst;
		this.sessdata = data;

		// Initialize fight data
		int i = 0;
		for (Equipment acc : data.getEquipment(EquipSlot.ACCESSORY)) {
			if (acc == null) continue;
			acc.initialize(p, this, null, EquipSlot.ACCESSORY, i++);
		}
		i = 0;
		for (Equipment armor : data.getEquipment(EquipSlot.ARMOR)) {
			if (armor == null) continue;
			armor.initialize(p, this, null, EquipSlot.ARMOR, i++);
		}
		i = 0;
		for (Equipment hotbar : data.getEquipment(EquipSlot.HOTBAR)) {
			if (hotbar == null) continue;
			hotbar.initialize(p, this, Trigger.getFromHotbarSlot(i), EquipSlot.HOTBAR, i);
			i++;
		}
		i = 0;
		for (Equipment other : data.getEquipment(EquipSlot.KEYBIND)) {
			if (other == null) continue;
			other.initialize(p, this, KeyBind.getBindFromData(i).getTrigger(), EquipSlot.KEYBIND, i++);
		}
		i = 0;
		for (ArtifactInstance art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize(p, this, null, null, i++);
		}
		
		Equipment offhand = data.getEquipment(EquipSlot.OFFHAND)[0];
		if (offhand != null) {
			offhand.initialize(p, this, null, EquipSlot.OFFHAND, 0);
		}
		
		// Setup inventory
		PlayerInventory inv = p.getInventory();
		inv.clear();
		ItemStack[] contents = inv.getContents();
		
		for (i = 0; i < 9; i++) {
			if (data.getEquipment(EquipSlot.HOTBAR)[i] == null) continue;
			contents[i] = data.getEquipment(EquipSlot.HOTBAR)[i].getItem();
		}
		inv.setContents(contents);
		
		if (offhand != null) inv.setItemInOffHand(offhand.getItem());
		
		// Setup mana and hunger bar
		this.maxStamina = sessdata.getMaxStamina();
		this.maxMana = sessdata.getMaxMana();
		this.maxHealth = sessdata.getMaxHealth();
		updateStamina();
		updateMana();
		this.staminaRegen = sessdata.getStaminaRegen();
		this.manaRegen = sessdata.getManaRegen();
		addTickAction(new PlayerUpdateTickAction());
	}
	
	public PlayerSessionData getSessionData() {
		return sessdata;
	}
	
	public void cleanup(PlayerSessionData data) {
		super.cleanup();
		
		// Perform end of fight actions (currently only used for resetting damage ticks)
		for (Equipment acc : data.getEquipment(EquipSlot.ACCESSORY)) {
			if (acc == null) continue;
			acc.cleanup(p, this);
		}
		for (Equipment armor : data.getEquipment(EquipSlot.ARMOR)) {
			if (armor == null) continue;
			armor.cleanup(p, this);
		}
		for (Equipment hotbar : data.getEquipment(EquipSlot.HOTBAR)) {
			if (hotbar == null) continue;
			hotbar.cleanup(p, this);
		}
		for (Equipment other : data.getEquipment(EquipSlot.KEYBIND)) {
			if (other == null) continue;
			other.cleanup(p, this);
		}
		for (ArtifactInstance art : data.getArtifacts()) {
			if (art == null) continue;
			art.cleanup(p, this);
		}
		
		if (data.getEquipment(EquipSlot.OFFHAND)[0] != null) {
			data.getEquipment(EquipSlot.OFFHAND)[0].cleanup(p, this);
		}
	}

	// Returns whether to cancel the event, which may or may not be ignored if it's an event that can be cancelled
	public boolean runActions(PlayerFightData data, Trigger trigger, Object[] inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					if (!ei.canTrigger(p, data)) {
						continue;
					}
				}
				TriggerResult tr = inst.trigger(data, inputs);
				if (tr.removeTrigger()) {
					int hotbar = Trigger.toHotbarSlot(trigger);
					if (hotbar == -1) {
						data.getPlayer().getInventory().setItem(hotbar, null);
					}
					iter.remove();
				}
				if (tr.cancelEvent()) cancel = true;
			}
			return cancel;
		}
		return false;
	}

	// Must be separate due to the same trigger doing a different thing based on slot (like weapons)
	public boolean runSlotBasedActions(PlayerFightData data, Trigger trigger, int slot, Object[] inputs) {
		if (!slotBasedTriggers.containsKey(slot)) return false;
		HashMap<Trigger, HashMap<String, TriggerAction>> triggers = slotBasedTriggers.get(slot);

		if (triggers.containsKey(trigger)) {
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					if (!ei.canTrigger(p, data)) {
						continue;
					}
				}
				TriggerResult tr = inst.trigger(data, inputs);
				if (tr.removeTrigger()) iter.remove();
			}
			return true;
		}
		return false;
	}
	
	public boolean hasTriggerAction(Trigger trigger) {
		return triggers.containsKey(trigger);
	}
	
	public void addSlotBasedTrigger(String id, int slot, Trigger trigger, TriggerAction action) {
		HashMap<Trigger, HashMap<String, TriggerAction>> triggers = slotBasedTriggers.getOrDefault(slot, 
				new HashMap<Trigger, HashMap<String, TriggerAction>>());
		slotBasedTriggers.put(slot, triggers);
		HashMap<String, TriggerAction> actions = triggers.getOrDefault(trigger, new HashMap<String, TriggerAction>());
		triggers.put(trigger, actions);
		addTrigger(id, actions, action);
	}

	public void addTrigger(String id, Trigger trigger, TriggerAction action) {
		HashMap<String, TriggerAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new HashMap<String, TriggerAction>();
		triggers.put(trigger, actions);
		addTrigger(id, actions, action);
	}
	
	private void addTrigger(String id, HashMap<String, TriggerAction> actions, TriggerAction action) {
		actions.put(id, action);

		if (action instanceof EquipmentInstance) {
			EquipmentInstance inst = (EquipmentInstance) action;
			equips.put(id, inst);
		}
	}
	
	public boolean isActive() {
		return !sessdata.isDead() && getPlayer() != null; // Not dead and online
	}

	public FightInstance getInstance() {
		return inst;
	}

	public Player getPlayer() {
		return sessdata.getData().getPlayer();
	}
	
	public FightStatistics getStats() {
		return stats;
	}
	
	public void addStamina(double amount) {
		this.stamina += amount;
		updateStamina();
	}
	
	public void addMaxStamina(double amount) {
		this.maxStamina += amount;
		updateStamina();
	}
	
	public void setStamina(double amount) {
		this.stamina = amount;
		updateStamina();
	}
	
	public double getStamina() {
		return this.stamina;
	}
	
	private void updateStamina() {
		this.stamina = Math.min(this.stamina, this.maxStamina);
		p.setFoodLevel((int) (this.stamina * 20 / sessdata.getMaxStamina()));
	}
	
	public void addHealth(double amount) {
		double curr = p.getHealth();
		double after = Math.min(this.maxHealth, curr + amount);
		p.setHealth(after);
	}
	
	public void addMana(double amount) {
		this.mana += amount;
		updateMana();
	}
	
	public void addMaxMana(double amount) {
		this.maxMana += amount;
		updateMana();
	}
	
	public void setMana(double amount) {
		this.mana = amount;
		updateMana();
	}
	
	public double getMana() {
		return mana;
	}
	
	private void updateMana() {
		this.mana = Math.min(this.mana, this.maxMana);
		p.setLevel((int) this.maxMana);
		float fraction = (float) (this.mana / this.maxMana);
		p.setExp(fraction);
	}
	
	private class PlayerUpdateTickAction extends TickAction {
		HashMap<Integer, EquipmentInstance> insts = new HashMap<Integer, EquipmentInstance>();
		
		public PlayerUpdateTickAction() {
			// Get the usable instances to tie cooldowns to
			for (int i = 0 ; i < 8; i++) {
				Trigger t = Trigger.getFromHotbarSlot(i);
				if (!triggers.containsKey(t)) continue;
				HashMap<String, TriggerAction> actions = triggers.get(t);
				for (TriggerAction action : actions.values()) {
					// Only the first valid usable instance is used as the cooldown
					if (action instanceof EquipmentInstance) {
						insts.put(i, (EquipmentInstance) action);
						break;
					}
				}
			}
		}
		@Override
		public TickResult run() {
			addMana(manaRegen);
			addStamina(p.isSprinting() ? -4 : staminaRegen);
			
			// Update hotbar cooldowns
			PlayerInventory inv = p.getInventory();
			for (Entry<Integer, EquipmentInstance> ent : insts.entrySet()) {
				inv.getItem(ent.getKey()).setAmount(Math.min(127, Math.max(1, ent.getValue().getCooldown())));
			}
			return TickResult.KEEP;
		}
	}
	
	public boolean canBasicAttack() {
		return nextAttack <= System.currentTimeMillis();
	}
	
	public void setBasicAttackCooldown(EquipSlot slot, EquipmentProperties props) {
		long attackCooldown = (long) (1000 / props.getAttackSpeed()) - 50; // Subtract 50 for tick differentials
		
		if (slot == EquipSlot.HOTBAR) this.nextAttack = System.currentTimeMillis() + attackCooldown;
		else this.nextOffAttack = System.currentTimeMillis() + attackCooldown;
	}
	
	public void setBasicAttackCooldown(EquipSlot slot, long cooldown) {
		if (slot == EquipSlot.HOTBAR) this.nextAttack = System.currentTimeMillis() + cooldown;
		else this.nextOffAttack = System.currentTimeMillis() + cooldown;
	}
	
	public void resetBasicAttackCooldown(EquipSlot slot) {
		if (slot == EquipSlot.HOTBAR) this.nextAttack = 0;
		else this.nextOffAttack = 0;
	}
	
	public boolean canBasicAttack(EquipSlot slot) {
		if (slot == EquipSlot.HOTBAR) return nextAttack <= System.currentTimeMillis();
		else return nextOffAttack <= System.currentTimeMillis();
	}

	public void addMaxHealth(double amount) {
		this.maxHealth += amount;
		getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHealth);
	}
}
