package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.google.common.collect.TreeMultiset;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;

public class PlayerFightData extends FightData {
	
	private static final Comparator<Status> stackComparator = new Comparator<Status>() {
		@Override
		public int compare(Status s1, Status s2) {
			// First priority: stacks
			int comp = Integer.compare(s1.getStacks(), s2.getStacks());
			if (comp != 0) return comp;
			
			// Next priority: id
			return s1.getId().compareTo(s2.getId());
		}
	};
	
	private PlayerSessionData sessdata;
	private HashMap<Trigger, TreeMultiset<PriorityAction>> triggers = new HashMap<Trigger, TreeMultiset<PriorityAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>(); // Useful for modifying cooldowns
	private HashMap<Integer, HashMap<Trigger, TreeMultiset<PriorityAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, TreeMultiset<PriorityAction>>>();
	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	private ArrayList<String> boardLines;
	private Player p;
	private long nextAttack, nextOffAttack;

	private double stamina, mana;
	private double maxStamina, maxMana, maxHealth;
	private double staminaRegen, manaRegen;
	private boolean isDead;
	
	private FightStatistics stats = new FightStatistics(this);

	public PlayerFightData(FightInstance inst, PlayerSessionData data) {
		super(data.getPlayer(), inst);
		p = data.getPlayer();
		
		this.inst = inst;
		this.sessdata = data;
		
		// Setup mana and hunger bar
		this.maxStamina = sessdata.getMaxStamina();
		this.maxMana = sessdata.getMaxMana();
		this.maxHealth = sessdata.getMaxHealth();
		this.mana = sessdata.getStartingMana();
		this.stamina = sessdata.getStartingStamina();
		this.staminaRegen = sessdata.getStaminaRegen();
		this.manaRegen = sessdata.getManaRegen();

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
		i = -1;
		for (Equipment other : data.getEquipment(EquipSlot.KEYBIND)) {
			i++;
			if (other == null) continue;
			other.initialize(p, this, KeyBind.getBindFromData(i).getTrigger(), EquipSlot.KEYBIND, i);
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
			contents[i] = data.getEquipment(EquipSlot.HOTBAR)[i].getItem();
		}
		inv.setContents(contents);
		
		if (offhand != null) inv.setItemInOffHand(offhand.getItem());
		addTickAction(new PlayerUpdateTickAction());

		updateStamina();
		updateMana();
		updateBoardLines();
	}
	
	@Override
	public TickResult runTickActions() {
		if (isDead) return TickResult.KEEP;
		return super.runTickActions();
	}
	
	public PlayerSessionData getSessionData() {
		return sessdata;
	}
	
	public ArrayList<String> getBoardLines() {
		return boardLines;
	}
	
	public void updateBoardLines() {
		int lineSize = 9;
		boardLines = new ArrayList<String>(lineSize);
		
		TreeSet<Status> statuses = new TreeSet<Status>(stackComparator);
		for (Status s : this.statuses.values()) {
			statuses.add(s);
		}
		
		ArrayList<Player> online = sessdata.getSession().getOnlinePlayers();
		int players = online.size();
		Iterator<Status> iter = statuses.descendingIterator();
		while (iter.hasNext() && boardLines.size() < lineSize - players - 1) {
			Status s = iter.next();
			boardLines.add(s.getBoardDisplay());
		}
		if (!boardLines.isEmpty()) boardLines.add("§8§m-----");
		
		for (Player p : online) {
			if (p == this.p) continue;
			boardLines.add(createHealthBar(p));
		}
	}
	
	private static String createHealthBar(Player p) {
		PlayerFightData pfd = FightInstance.getUserData(p.getUniqueId());
		if (pfd != null && pfd.isDead) {
			return "&c&m" + p.getName();
		}
		double percenthp = p.getHealth() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		percenthp *= 100;
		int php = (int) percenthp;
		String color = "§a";
		if (php < 50 && php >= 25) {
			color = "§e";
		}
		else if (php < 25) {
			color = "§c";
		}

		String bar = "" + color;
		// Add 5 so 25% is still 3/10 on the health bar
		int phpmod = (php + 5) / 10;
		for (int i = 0; i < phpmod; i++) {
			bar += "|";
		}
		bar += "§7";
		for (int i = 0; i < (10 - phpmod); i++) {
			bar += "|";
		}
		
		return color + p.getName() + " " + bar;
	}

	// Used when the player dies or revived
	public void setDeath(boolean isDead) {
		Player p = getPlayer();
		this.isDead = isDead;
		if (isDead) {
			p.setInvulnerable(true);
			p.setInvisible(true);
			clearStatus(StatusType.POISON);
			clearStatus(StatusType.BLEED);
		}
		else {
			p.setInvulnerable(false);
			p.setInvisible(false);
			p.setHealth(Math.round(this.maxHealth * 0.25));
		}
	}

	public boolean isDead() {
		return isDead;
	}
	
	public void cleanup(PlayerSessionData data) {
		super.cleanup();

		if (isDead) {
			Player p = getPlayer();
			p.setInvisible(false);
			p.setInvulnerable(false);
		}
		
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
		return runActions(data, triggers, trigger, inputs);
	}

	// Must be separate due to the same trigger doing a different thing based on slot (like weapons)
	public boolean runSlotBasedActions(PlayerFightData data, Trigger trigger, int slot, Object inputs) {
		if (!slotBasedTriggers.containsKey(slot)) return false;
		HashMap<Trigger, TreeMultiset<PriorityAction>> triggers = slotBasedTriggers.get(slot);
		return runActions(data, triggers, trigger, inputs);
	}
	
	private boolean runActions(PlayerFightData data, HashMap<Trigger, TreeMultiset<PriorityAction>> triggers, Trigger trigger, Object inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			Iterator<PriorityAction> iter = triggers.get(trigger).iterator();
			PlayerInventory inv = p.getInventory();
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
					EquipmentInstance.updateSlot(p, inv, ei);
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
			p.updateInventory();
			return cancel;
		}
		return false;
	}
	
	public boolean hasTriggerAction(Trigger trigger) {
		return triggers.containsKey(trigger);
	}
	
	public void addSlotBasedTrigger(String id, int slot, Trigger trigger, TriggerAction action) {
		addSlotBasedTrigger(id, slot, trigger, new PriorityAction(id, action));
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
		addTrigger(id, trigger, new PriorityAction(id, action));
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
		return !isDead && getPlayer() != null; // Not dead and online
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
		updateActionBar();
		updateBoardLines();
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
	
	public void addStaminaRegen(double amount) {
		this.staminaRegen += amount;
	}
	
	public void addManaRegen(double amount) {
		this.manaRegen += amount;
	}
	
	private void updateMana() {
		this.mana = Math.min(this.mana, this.maxMana);
		updateActionBar();
		updateBoardLines();
	}
	
	private void updateActionBar() {
		p.sendActionBar(
			Component.text("HP: " + (int)getPlayer().getHealth() + " / " + (int)maxHealth, NamedTextColor.RED)
			.append(Component.text("  |  ", NamedTextColor.GRAY))
			.append(Component.text("MP: " + (int)mana + " / " + (int)maxMana, NamedTextColor.BLUE))
			.append(Component.text("  |  ", NamedTextColor.GRAY))
			.append(Component.text("SP: " + (int)stamina + " / " + (int)maxStamina, NamedTextColor.GREEN))
		);
	}
	
	private class PlayerUpdateTickAction extends TickAction {
		HashMap<Integer, EquipmentInstance> insts = new HashMap<Integer, EquipmentInstance>();
		LinkedList<Integer> toRemove = new LinkedList<Integer>();
		
		public PlayerUpdateTickAction() {
			// Get the usable instances to tie cooldowns to
			for (int i = 0 ; i < 8; i++) {
				Trigger t = Trigger.getFromHotbarSlot(i);
				boolean found = false;
				
				// First look for hotbar castable skills
				if (triggers.containsKey(t)) {
					TreeMultiset<PriorityAction> actions = triggers.get(t);
					for (PriorityAction action : actions) {
						// Only the first valid usable instance is used as the cooldown
						if (action instanceof EquipmentInstance) {
							insts.put(i, (EquipmentInstance) action);
							found = true;
							break;
						}
					}
				}
				
				// Next look for any slot-based triggers (so far only thrown tridents) that can have cooldowns
				if (found || !slotBasedTriggers.containsKey(i)) continue;
				for (TreeMultiset<PriorityAction> slotActions : slotBasedTriggers.get(i).values()) {
					for (PriorityAction slotAction : slotActions) {
						if (slotAction instanceof EquipmentInstance) {
							insts.put(i, (EquipmentInstance) slotAction);
							found = true;
							break;
						}
					}
					if (found) break;
				}
			}
		}
		@Override
		public TickResult run() {
			addMana(manaRegen);
			addStamina(p.isSprinting() ? staminaRegen - 4 : staminaRegen);
			updateBoardLines();
			
			FightInstance.trigger(p, Trigger.PLAYER_TICK, null);
			
			// Update hotbar cooldowns
			PlayerInventory inv = p.getInventory();
			for (Entry<Integer, EquipmentInstance> ent : insts.entrySet()) {
				// Check if cooldown is over
				ItemStack item = inv.getItem(ent.getKey());
				if (item == null) {
					toRemove.add(ent.getKey());
					continue;
				}
				if (item.getType().name().endsWith("CANDLE") &&
						ent.getValue().getCooldownSeconds() == 0) {
					inv.setItem(ent.getKey(), ent.getValue().getEquipment().getItem());
				}
			}
			p.updateInventory();
			
			for (int i : toRemove) {
				insts.remove(i);
			}
			toRemove.clear();
			return TickResult.KEEP;
		}
	}
	
	public boolean canBasicAttack() {
		return nextAttack <= System.currentTimeMillis();
	}
	
	public void setBasicAttackCooldown(EquipSlot slot, EquipmentProperties props) {
		long attackCooldown = (long) (1000 / props.get(PropertyType.ATTACK_SPEED)) - 50; // Subtract 50 for tick differentials
		
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
