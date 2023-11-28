package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import me.neoblade298.neorogue.player.KeyBind;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.TriggerAction;
import me.neoblade298.neorogue.equipment.*;

public class PlayerFightData extends FightData {
	private PlayerSessionData sessdata;
	private HashMap<Trigger, HashMap<String, TriggerAction>> triggers = new HashMap<Trigger, HashMap<String, TriggerAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>();
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
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.initialize(p, this, null, -1);
		}
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.initialize(p, this, null, -1);
		}
		for (int i = 0; i < data.getHotbar().length; i++) {
			HotbarCompatible hotbar = data.getHotbar()[i];
			if (hotbar == null) continue;
			hotbar.initialize(p, this, Trigger.getFromHotbarSlot(i), i);
		}
		for (int i = 0; i < data.getOtherBinds().length; i++) {
			Usable other = data.getOtherBinds()[i];
			if (other == null) continue;
			other.initialize(p, this, KeyBind.getBindFromData(i).getTrigger(), -1);
		}
		for (ArtifactInstance art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize(p, this, null, -1);
		}
		
		if (data.getOffhand() != null) {
			data.getOffhand().initialize(p, this, null, -1);
		}
		
		// Setup inventory
		PlayerInventory inv = p.getInventory();
		inv.clear();
		ItemStack[] contents = inv.getContents();
		
		for (int i = 0; i < 9; i++) {
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
	public boolean runActions(Trigger trigger, Object[] inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					if (!ei.canTrigger()) {
						ei.sendCooldownMessage(sessdata.getPlayer());
						continue;
					}
				}
				if (!inst.trigger(inputs)) iter.remove();
				if (inst.isCancelled()) cancel = true;
			}
			return cancel;
		}
		return false;
	}

	public boolean runSlotBasedTriggers(Trigger trigger, int hotbar, Object[] inputs) {
		if (!slotBasedTriggers.containsKey(hotbar)) return false;
		HashMap<Trigger, HashMap<String, TriggerAction>> triggers = slotBasedTriggers.get(hotbar);

		if (triggers.containsKey(trigger)) {
			Iterator<TriggerAction> iter = triggers.get(trigger).values().iterator();
			while (iter.hasNext()) {
				TriggerAction inst = iter.next();

				if (inst instanceof EquipmentInstance) {
					EquipmentInstance ei = (EquipmentInstance) inst;
					if (!ei.canTrigger()) {
						ei.sendCooldownMessage(sessdata.getPlayer());
						continue;
					}
				}
				if (!inst.trigger(inputs)) iter.remove();
			}
			return true;
		}
		return false;
	}
	
	public void addHotbarTrigger(String id, int hotbar, Trigger trigger, TriggerAction action) {
		HashMap<Trigger, HashMap<String, TriggerAction>> triggers = slotBasedTriggers.getOrDefault(hotbar, 
				new HashMap<Trigger, HashMap<String, TriggerAction>>());
		
		HashMap<String, TriggerAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new HashMap<String, TriggerAction>();
		addTrigger(id, actions, action);
		triggers.put(trigger, actions);
		slotBasedTriggers.put(hotbar, triggers);
	}

	public void addTrigger(String id, Trigger trigger, TriggerAction action) {
		HashMap<String, TriggerAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new HashMap<String, TriggerAction>();
		addTrigger(id, actions, action);
		triggers.put(trigger, actions);
	}
	
	private void addTrigger(String id, HashMap<String, TriggerAction> actions, TriggerAction action) {
		actions.put(id, action);

		if (action instanceof EquipmentInstance) {
			EquipmentInstance inst = (EquipmentInstance) action;
			equips.put(id, inst);
		}
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
		public boolean run() {
			addMana(manaRegen);
			addStamina(p.isSprinting() ? -4 : staminaRegen);
			return false;
		}
	}
}
