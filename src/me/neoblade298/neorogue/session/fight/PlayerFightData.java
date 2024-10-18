package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.StaminaChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
	private HashMap<Trigger, ArrayList<PriorityAction>> triggers = new HashMap<Trigger, ArrayList<PriorityAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>(); // Useful for modifying cooldowns
	private HashMap<Integer, HashMap<Trigger, ArrayList<PriorityAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, ArrayList<PriorityAction>>>();
	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	private HashMap<UUID, Trap> traps = new HashMap<UUID, Trap>();
	private ArrayList<String> boardLines;
	private Player p;
	private long nextAttack, nextOffAttack;

	private double stamina, mana;
	private double maxStamina, maxMana, maxHealth;
	private double staminaRegen, manaRegen;
	private double sprintCost = 4;
	private boolean isDead, ignoreCooldowns;
	private AmmunitionInstance ammo = null;
	
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
		PlayerInventory inv = p.getInventory();
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
			if (hotbar == null) {
				inv.setItem(i, null);
			}
			else {
				hotbar.initialize(p, this, Trigger.getFromHotbarSlot(i), EquipSlot.HOTBAR, i);
			}
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
		
		// Sort triggers by priority
		for (ArrayList<PriorityAction> list : triggers.values()) {
			Collections.sort(list);
		}
		for (HashMap<Trigger, ArrayList<PriorityAction>> map : slotBasedTriggers.values()) {
			for (ArrayList<PriorityAction> list : map.values()) {
				Collections.sort(list);
			}
		}
		addTickAction(new PlayerUpdateTickAction());

		updateStamina();
		updateMana();
		updateBoardLines();
	}

	public boolean isChanneling() {
		return hasStatus(StatusType.CHANNELING);
	}

	public void channel(int ticks) {
		applyStatus(StatusType.CHANNELING, this, 1, ticks);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 3));
	}

	public void charge(int ticks) {
		applyStatus(StatusType.CHANNELING, this, 1, ticks);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1));
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

	public void ignoreCooldowns(boolean ignore) {
		this.ignoreCooldowns = ignore;
	}

	public boolean isIgnoreCooldowns() {
		return ignoreCooldowns;
	}
	
	@Override
	public void updateDisplayName() {}
	
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
			if (s.isHidden()) continue;
			boardLines.add(s.getDisplay());
		}
		if (!boardLines.isEmpty()) boardLines.add("§8§m-----");
		
		for (Player p : online) {
			if (p == this.p) continue;
			boardLines.add(createHealthBar(p));
		}
	}
	
	private static String createHealthBar(Player p) {
		PlayerFightData pfd = FightInstance.getUserData(p.getUniqueId());
		if (pfd == null) {
			return "";
		}
		if (pfd != null && pfd.isDead) {
			return "&c&m" + p.getName();
		}
		double percenthp = p.getHealth() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double percentShield = pfd.getShields().getAmount() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		percenthp *= 100;
		percentShield *= 100;
		int php = (int) percenthp;
		int psh = (int) percentShield;
		String color = "§a";
		if (php < 50 && php >= 25) {
			color = "§6";
		}
		else if (php < 25) {
			color = "§c";
		}

		// Add 5 so 25% is still 3/10 on the health bar
		int phpmod = (php + 5) / 10;
		int pshmod = (psh + 5) / 10;
		String bar = "" + color + (pshmod > 0 ? "§n" : "");
		for (int i = 0; i < 10; i++) {
			// If you have more shields than health, make the bars gray but keep the underline
			if (i == phpmod) {
				bar += "§7";
				if (pshmod > i) {
					bar += "§n";
				}
			}
			
			// If you have less shields than health, remove the underline
			else if (pshmod > 0 && i == pshmod) {
				bar += color;
			}
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
			removeStatus(StatusType.POISON);
			removeStatus(StatusType.BLEED);
		}
		else {
			p.setInvulnerable(false);
			p.setInvisible(false);
			p.setHealth(Math.round(this.maxHealth * 0.05));
			// This one's more reliable, sometimes statuses may be applied when player is dead
			removeStatus(StatusType.POISON);
			removeStatus(StatusType.BLEED);
		}
	}
	
	@Override
	public void applyStatus(Status s, FightData applier, int stacks, int seconds, DamageMeta meta) {
		if (isDead) return;
		super.applyStatus(s, applier, stacks, seconds, meta);
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
		HashMap<Trigger, ArrayList<PriorityAction>> triggers = slotBasedTriggers.get(slot);
		return runActions(data, triggers, trigger, inputs);
	}
	
	private boolean runActions(PlayerFightData data, HashMap<Trigger, ArrayList<PriorityAction>> triggers, Trigger trigger, Object inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			Iterator<PriorityAction> iter = triggers.get(trigger).iterator();
			while (iter.hasNext()) {
				PriorityAction inst = iter.next();
				TriggerResult tr;

				if (inst instanceof EquipmentInstance) {
					if (data.isChanneling()) return false;
					EquipmentInstance ei = (EquipmentInstance) inst;
					CastUsableEvent ev = new CastUsableEvent(ei);
					runActions(data, Trigger.PRE_CAST_USABLE, ev);
					
					// Buff mana costs, cannot go below 0, uses temp mana/stamina cost if it exists first (Escape Plan)
					Buff b = ev.getBuff(PropertyType.MANA_COST);
					if (!b.isEmpty() && ei.getTempManaCost() == -1) ei.setTempManaCost(Math.max(0, b.applyNegative(ei.getManaCost())));
					// Buff stamina costs, cannot go below 0
					b = ev.getBuff(PropertyType.STAMINA_COST);
					if (!b.isEmpty() && ei.getTempStaminaCost() == -1) ei.setTempStaminaCost(Math.max(0, b.applyNegative(ei.getStaminaCost())));
					// Buff cooldowns, doesn't matter if it goes below 0
					b = ev.getBuff(PropertyType.COOLDOWN);
					if (!b.isEmpty()) ei.setTempCooldown(b.applyNegative(ei.getBaseCooldown()));
					
					if (!ei.canTrigger(p, data)) {
						ei.resetTempCosts();
						continue;
					}
					runActions(data, Trigger.CAST_USABLE, ev);
					tr = ei.trigger(data, inputs);
					ei.resetTempCosts();
					ei.updateIcon();
				}
				else {
					if (!inst.canTrigger(p, data)) {
						continue;
					}
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

	public void setAmmoInstance(AmmunitionInstance ammo) {
		this.ammo = ammo;
	}

	public AmmunitionInstance getAmmoInstance() {
		return ammo;
	}
	
	public boolean hasTriggerAction(Trigger trigger) {
		return triggers.containsKey(trigger);
	}
	
	public void addSlotBasedTrigger(String id, int slot, Trigger trigger, TriggerAction action) {
		addSlotBasedTrigger(id, slot, trigger, new PriorityAction(id, action));
	}
	
	public void addSlotBasedTrigger(String id, int slot, Trigger trigger, TriggerAction action, TriggerCondition cond) {
		addSlotBasedTrigger(id, slot, trigger, new PriorityAction(id, action, cond));
	}
	
	public void addSlotBasedTrigger(String id, int slot, Trigger trigger, PriorityAction action) {
		HashMap<Trigger, ArrayList<PriorityAction>> triggers = slotBasedTriggers.getOrDefault(slot, 
				new HashMap<Trigger, ArrayList<PriorityAction>>());
		slotBasedTriggers.put(slot, triggers);
		ArrayList<PriorityAction> actions = triggers.getOrDefault(trigger, new ArrayList<PriorityAction>());
		triggers.put(trigger, actions);
		addTrigger(id, actions, action);
	}

	public void addTrigger(String id, Trigger trigger, TriggerAction action) {
		addTrigger(id, trigger, new PriorityAction(id, action));
	}

	public void addTrigger(String id, Trigger trigger, TriggerAction action, TriggerCondition cond) {
		addTrigger(id, trigger, new PriorityAction(id, action, cond));
	}

	public void addTrigger(String id, Trigger trigger, PriorityAction action) {
		ArrayList<PriorityAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new ArrayList<PriorityAction>();
		triggers.put(trigger, actions);
		addTrigger(id, actions, action);
	}
	
	private void addTrigger(String id, ArrayList<PriorityAction> actions, PriorityAction action) {
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
		StaminaChangeEvent ev = new StaminaChangeEvent(amount);
		FightInstance.trigger(p, Trigger.STAMINA_CHANGE, ev);
		this.stamina += ev.getChange();
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
	
	@Override
	public void addHealth(double amount) {
		double curr = p.getHealth();
		double after = Math.min(this.maxHealth, curr + amount);
		stats.addSelfHealing(after - curr);
		p.setHealth(after);
	}
	
	public void addMana(double amount) {
		StaminaChangeEvent ev = new StaminaChangeEvent(amount);
		FightInstance.trigger(p, Trigger.MANA_CHANGE, ev);
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
	public double getMaxHealth() {
		return maxHealth;
	}

	public void setStaminaRegen(double amount) {
		this.staminaRegen = amount;
	}
	
	public void addStaminaRegen(double amount) {
		this.staminaRegen += amount;
	}
	
	public void setManaRegen(double amount) {
		this.manaRegen = amount;
	}
	
	public void addManaRegen(double amount) {
		this.manaRegen += amount;
	}
	
	public void addSprintCost(double amount) {
		this.sprintCost += amount;
	}
	
	private void updateMana() {
		this.mana = Math.min(this.mana, this.maxMana);
		updateActionBar();
		updateBoardLines();
	}
	
	public void updateActionBar() {
		Component bar = Component.text("HP: " + (int)getPlayer().getHealth(), NamedTextColor.RED);
		if (shields.getAmount() > 0) {
			bar = bar.append(Component.text("+" + (int)getShields().getAmount(), NamedTextColor.YELLOW));
		}
		bar = bar.append(Component.text(" / " + (int)maxHealth, NamedTextColor.RED))
		.append(Component.text("  |  ", NamedTextColor.GRAY))
		.append(Component.text("MP: " + (int)mana + " / " + (int)maxMana, NamedTextColor.BLUE))
		.append(Component.text("  |  ", NamedTextColor.GRAY))
		.append(Component.text("SP: " + (int)stamina + " / " + (int)maxStamina, NamedTextColor.GREEN));
		p.sendActionBar(bar);
	}

	public Trap addTrap(Location loc) {
		Trap trap = new Trap(loc);
		traps.put(trap.getUniqueId(), trap);
		return trap;
	}

	public void removeTrap(Trap trap) {
		traps.remove(trap.getUniqueId());
	}

	public HashMap<UUID, Trap> getTraps() {
		return traps;
	}
	
	private class PlayerUpdateTickAction extends TickAction {
		@Override
		public TickResult run() {
			addMana(manaRegen);
			addStamina(p.isSprinting() ? staminaRegen - sprintCost : staminaRegen);
			updateBoardLines();
			FightInstance.trigger(p, Trigger.PLAYER_TICK, null);
			return TickResult.KEEP;
		}
	}
	
	public boolean canBasicAttack() {
		return nextAttack <= System.currentTimeMillis();
	}
	
	public void setBasicAttackCooldown(EquipSlot slot, double attackSpeed) {
		long attackCooldown = (long) (1000 / attackSpeed) - 50; // Subtract 50 for tick differentials
		
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
