package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.TreeMultiset;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;

public class PlayerFightData extends FightData {
	private PlayerSessionData sessdata;
	private HashMap<Trigger, TreeMultiset<PriorityAction>> triggers = new HashMap<Trigger, TreeMultiset<PriorityAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>(); // Useful for modifying cooldowns
	private HashMap<Integer, HashMap<Trigger, TreeMultiset<PriorityAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, TreeMultiset<PriorityAction>>>();
	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	private Player p;
	private long nextAttack, nextOffAttack;

	private double stamina, mana;
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
		i = -1;
		for (Equipment hotbar : data.getEquipment(EquipSlot.HOTBAR)) {
			i++;
			if (hotbar == null) continue;
			hotbar.initialize(p, this, Trigger.getFromHotbarSlot(i), EquipSlot.HOTBAR, i);
		}
		i = 0;
		for (Equipment other : data.getEquipment(EquipSlot.KEYBIND)) {
			if (other == null) continue;
			other.initialize(p, this, KeyBind.getBindFromData(i).getTrigger(), EquipSlot.KEYBIND, i++);
		}
		i = 0;
		for (ArtifactInstance art : data.getArtifacts().values()) {
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
			if (!hasTriggerAction(Trigger.getFromHotbarSlot(i)) && 
					!slotBasedTriggers.containsKey(i)) continue;
			contents[i] = data.getEquipment(EquipSlot.HOTBAR)[i].getItem();
		}
		inv.setContents(contents);
		
		if (offhand != null) inv.setItemInOffHand(offhand.getItem());
		
		// Setup mana and hunger bar
		this.maxStamina = sessdata.getMaxStamina();
		this.maxMana = sessdata.getMaxMana();
		this.maxHealth = sessdata.getMaxHealth();
		this.mana = sessdata.getStartingMana();
		this.stamina = sessdata.getStartingStamina();
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
		for (ArtifactInstance art : data.getArtifacts().values()) {
			if (art == null) continue;
			art.cleanup(p, this);
		}
		
		if (data.getEquipment(EquipSlot.OFFHAND)[0] != null) {
			data.getEquipment(EquipSlot.OFFHAND)[0].cleanup(p, this);
		}
		
		for (Listener l : listeners) {
			HandlerList.unregisterAll(l);
		}
	}

	// Returns whether to cancel the event, which may or may not be ignored if it's an event that can be cancelled
	public boolean runActions(PlayerFightData data, Trigger trigger, Object inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			Iterator<PriorityAction> iter = triggers.get(trigger).iterator();
			while (iter.hasNext()) {
				PriorityAction inst = iter.next();
				TriggerResult tr;

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					CastUsableEvent ev = new CastUsableEvent(ei);
					runActions(data, Trigger.CAST_USABLE, ev);
					ei = ev.getInstance();
					if (!ei.canTrigger(p, data)) {
						continue;
					}
					tr = ei.trigger(data, inputs);
				}
				else {
					tr = inst.trigger(data, inputs);
				}
				
				
				if (tr.removeTrigger()) {
					int hotbar = Trigger.toHotbarSlot(trigger);
					if (hotbar != -1) {
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
	public boolean runSlotBasedActions(PlayerFightData data, Trigger trigger, int slot, Object inputs) {
		if (!slotBasedTriggers.containsKey(slot)) return false;
		HashMap<Trigger, TreeMultiset<PriorityAction>> triggers = slotBasedTriggers.get(slot);

		if (triggers.containsKey(trigger)) {
			Iterator<PriorityAction> iter = triggers.get(trigger).iterator();
			while (iter.hasNext()) {
				PriorityAction inst = iter.next();

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
		addSlotBasedTrigger(id, slot, trigger, new PriorityAction(action));
	}
	
	public void addSlotBasedTrigger(String id, int slot, Trigger trigger, PriorityAction action) {
		HashMap<Trigger, TreeMultiset<PriorityAction>> triggers = slotBasedTriggers.getOrDefault(slot, 
				new HashMap<Trigger, TreeMultiset<PriorityAction>>());
		slotBasedTriggers.put(slot, triggers);
		TreeMultiset<PriorityAction> actions = triggers.getOrDefault(trigger, TreeMultiset.create());
		triggers.put(trigger, actions);
		addTrigger(id, actions, action);
		TreeMultiset.create();
	}

	public void addTrigger(String id, Trigger trigger, TriggerAction action) {
		addTrigger(id, trigger, new PriorityAction(action));
	}

	public void addTrigger(String id, Trigger trigger, PriorityAction action) {
		TreeMultiset<PriorityAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: TreeMultiset.create();
		triggers.put(trigger, actions);
		addTrigger(id, actions, action);
	}
	
	private void addTrigger(String id, TreeMultiset<PriorityAction> actions, PriorityAction action) {
		actions.add(action);
		
		if (action instanceof Listener) {
			Listener l = (Listener) action;
			Bukkit.getPluginManager().registerEvents(l, NeoRogue.inst());
			listeners.add(l);
		}

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
	
	public double getMaxStamina() {
		return maxStamina;
	}
	
	private void updateStamina() {
		this.stamina = Math.min(this.stamina, this.maxStamina);
		p.setFoodLevel((int) (this.stamina * 14 / sessdata.getMaxStamina()) + 6);
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
	
	public double getMaxMana() {
		return maxMana;
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
				TreeMultiset<PriorityAction> actions = triggers.get(t);
				for (PriorityAction action : actions) {
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
