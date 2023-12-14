package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.equipment.*;

public class PlayerFightData extends FightData {
	private PlayerSessionData sessdata;
	private HashMap<Trigger, HashMap<String, TriggerAction>> triggers = new HashMap<Trigger, HashMap<String, TriggerAction>>();
	private HashMap<String, UsableInstance> equips = new HashMap<String, UsableInstance>();
	private HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, HashMap<String, TriggerAction>>>();
	private Player p;

	private double stamina = 0, mana = 0;
	private double staminaRegen, manaRegen;
	
	private FightStatistics stats = new FightStatistics();

	public PlayerFightData(FightInstance inst, PlayerSessionData data) {
		super(data.getPlayer(), inst);
		p = data.getPlayer();
		
		this.inst = inst;
		this.sessdata = data;

		// Initialize fight data
		int i = 0;
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.initialize(p, this, null, i++);
		}
		i = 0;
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.initialize(p, this, null, i++);
		}
		for (i = 0; i < data.getHotbar().length; i++) {
			HotbarCompatible hotbar = data.getHotbar()[i];
			if (hotbar == null) continue;
			hotbar.initialize(p, this, Trigger.getFromHotbarSlot(i), i);
		}
		for (i = 0; i < data.getOtherBinds().length; i++) {
			Usable other = data.getOtherBinds()[i];
			if (other == null) continue;
			other.initialize(p, this, KeyBind.getBindFromData(i).getTrigger(), -1);
		}
		i = 0;
		for (ArtifactInstance art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize(p, this, null, i++);
		}
		
		if (data.getOffhand() != null) {
			data.getOffhand().initialize(p, this, null, 0);
		}
		
		// Setup inventory
		PlayerInventory inv = p.getInventory();
		inv.clear();
		ItemStack[] contents = inv.getContents();
		
		for (i = 0; i < 9; i++) {
			if (data.getHotbar()[i] == null) continue;
			contents[i] = data.getHotbar()[i].getItem();
		}
		inv.setContents(contents);
		
		if (data.getOffhand() != null) inv.setItemInOffHand(data.getOffhand().getItem());
		
		// Setup mana and hunger bar
		updateStamina();
		updateMana();
		this.staminaRegen = sessdata.getStaminaRegen();
		this.manaRegen = sessdata.getManaRegen();
		addTickAction(new ManaStaminaTickAction());
	}
	
	public PlayerSessionData getSessionData() {
		return sessdata;
	}
	
	public void cleanup(PlayerSessionData data) {
		super.cleanup();
		
		// Perform end of fight actions (currently only used for resetting damage ticks)
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.cleanup(p, this);
		}
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.cleanup(p, this);
		}
		for (int i = 0; i < data.getHotbar().length; i++) {
			HotbarCompatible hotbar = data.getHotbar()[i];
			if (hotbar == null) continue;
			hotbar.cleanup(p, this);
		}
		for (int i = 0; i < data.getOtherBinds().length; i++) {
			Usable other = data.getOtherBinds()[i];
			if (other == null) continue;
			other.cleanup(p, this);
		}
		for (ArtifactInstance art : data.getArtifacts()) {
			if (art == null) continue;
			art.cleanup(p, this);
		}
		
		if (data.getOffhand() != null) {
			data.getOffhand().cleanup(p, this);
		}
	}

	// Returns whether to cancel the event, which may or may not be ignored if it's an event that can be cancelled
	public boolean runActions(PlayerFightData data, Trigger trigger, Object[] inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof UsableInstance) {
					UsableInstance ui = (UsableInstance) inst;
					if (!ui.canTrigger(p, data)) {
						continue;
					}
				}
				TriggerResult tr = inst.trigger(data, inputs);
				if (tr.removeTrigger()) iter.remove();
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

				if (inst instanceof UsableInstance) {
					UsableInstance ui = (UsableInstance) inst;
					if (!ui.canTrigger(p, data)) {
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

		if (action instanceof UsableInstance) {
			UsableInstance inst = (UsableInstance) action;
			equips.put(id, inst);
		}
	}
	
	public boolean isActive() {
		return sessdata.isDead() || getPlayer() == null;
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
	
	public void setStamina(double amount) {
		this.stamina = amount;
		updateStamina();
	}
	
	public double getStamina() {
		return this.stamina;
	}
	
	private void updateStamina() {
		this.stamina = Math.min(this.stamina, sessdata.getMaxStamina());
		p.setFoodLevel((int) (this.stamina * 20 / sessdata.getMaxStamina()));
	}
	
	public void addHealth(double amount) {
		double curr = p.getHealth();
		double after = Math.max(sessdata.getMaxHealth(), curr + amount);
		p.setHealth(after);
	}
	
	public void addMana(double amount) {
		this.mana += amount;
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
		this.mana = Math.min(this.mana, sessdata.getMaxMana());
		p.setLevel((int) sessdata.getMaxMana());
		float fraction = (float) (this.mana / sessdata.getMaxMana());
		p.setExp(fraction);
	}
	
	private class ManaStaminaTickAction extends TickAction {
		@Override
		public TickResult run() {
			addMana(manaRegen);
			addStamina(p.isSprinting() ? -4 : staminaRegen);
			return TickResult.KEEP;
		}
	}
}
