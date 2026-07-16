package me.neoblade298.neorogue.session.fight;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.AmmunitionInstance;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.LimitedAmmunition;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.TaskChain;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerCondition;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.CreateRiftEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LayTrapEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.StaminaChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlayerFightData extends FightData {

	private static final DecimalFormat df = new DecimalFormat("##.#");
	public static String EXTRA_SHOT_TAG = "EXTRA_SHOT";
	private static final Comparator<Status> stackComparator = new Comparator<Status>() {
		@Override
		public int compare(Status s1, Status s2) {
			// First priority: stacks
			int comp = Integer.compare(s1.getStacks(), s2.getStacks());
			if (comp != 0)
				return comp;

			// Next priority: id
			return s1.getId().compareTo(s2.getId());
		}
	};

	private PlayerSessionData sessdata;
	private HashMap<Trigger, ArrayList<PriorityAction>> triggers = new HashMap<Trigger, ArrayList<PriorityAction>>();
	private HashMap<String, EquipmentInstance> equips = new HashMap<String, EquipmentInstance>(); // Useful for
																									// modifying
																									// cooldowns
	private HashMap<Integer, HashMap<Trigger, ArrayList<PriorityAction>>> slotBasedTriggers = new HashMap<Integer, HashMap<Trigger, ArrayList<PriorityAction>>>();
	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	private HashMap<UUID, Marker> markers = new HashMap<UUID, Marker>();
	private HashMap<UUID, Trap> traps = new HashMap<UUID, Trap>();
	private HashMap<UUID, Rift> rifts = new HashMap<UUID, Rift>();
	private ArrayList<String> boardLines;
	private Player p;
	private long nextAttack, nextOffAttack;
	private ArrayList<ProjectileGroup> extraShots = new ArrayList<ProjectileGroup>();

	private double stamina, mana;
	private double maxStamina, maxMana, maxHealth;
	private double staminaRegen, manaRegen;
	private double sprintCost = 4;
	private boolean isDead, ignoreCooldowns, hasSprinted, droppedThisTick;
	private AmmunitionInstance ammo = null;

	private FightStatistics stats = new FightStatistics(this);

	public PlayerFightData(FightInstance inst, PlayerSessionData data) {
		super(data.getPlayer(), inst);
		p = data.getPlayer();

		this.inst = inst;
		this.sessdata = data;

		// Register early so equipment initialize() can look up this FightData
		FightInstance.putFightData(p.getUniqueId(), this);

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
		for (SessionEquipment acc : data.getSessionEquipment(EquipSlot.ACCESSORY)) {
			if (acc == null)
				continue;
			acc.getEquipment().initialize(this, null, EquipSlot.ACCESSORY, i++, acc);
		}
		i = 0;
		for (SessionEquipment armor : data.getSessionEquipment(EquipSlot.ARMOR)) {
			if (armor == null)
				continue;
			armor.getEquipment().initialize(this, null, EquipSlot.ARMOR, i++, armor);
		}
		i = -1;
		for (SessionEquipment hotbar : data.getSessionEquipment(EquipSlot.HOTBAR)) {
			i++;
			if (hotbar == null) {
				inv.setItem(i, null);
			} else {
				hotbar.getEquipment().initialize(this, Trigger.getFromHotbarSlot(i), EquipSlot.HOTBAR, i, hotbar);
				Equipment eq = hotbar.getEquipment();

				// This hotfix is basically just to allow tipped arrows to stay in place in the hotbar
				// since vanilla minecraft uses them and they get put in the first available slot
				boolean needsHotfix = (eq instanceof Ammunition) && !(eq instanceof LimitedAmmunition) &&
						eq.getItem().getType() != Material.ARROW;
				if (needsHotfix) {
					ItemStack icon = inv.getItem(i);
					if (icon == null) icon = hotbar.getItem();
					icon.setAmount(2);
					inv.setItem(i, icon);
				}
			}
		}
		i = -1;
		for (SessionEquipment other : data.getSessionEquipment(EquipSlot.KEYBIND)) {
			i++;
			if (other == null)
				continue;
			other.getEquipment().initialize(this, KeyBind.getBindFromData(i).getTrigger(), EquipSlot.KEYBIND, i, other);
		}
		i = 0;
		for (ArtifactInstance art : new ArrayList<>(data.getArtifacts().values())) {
			if (art == null)
				continue;
			art.initialize(this, null, null, i++);
		}

		SessionEquipment offhand = data.getSessionEquipment(EquipSlot.OFFHAND)[0];
		if (offhand != null) {
			inv.setItem(EquipmentSlot.OFF_HAND, offhand.getItem());
			offhand.getEquipment().initialize(this, null, EquipSlot.OFFHAND, 40, offhand);
		} else {
			inv.setItem(EquipmentSlot.OFF_HAND, null);
		}

		// Leave Fight icon, placed in the same slot as PlayerSessionInventory's Save & Quit button.
		inv.setItem(PlayerSessionInventory.LEAVE, CoreInventory.createButton(Material.COMPASS,
				Component.text("Leave Fight", NamedTextColor.RED),
				"Leave the fight and end the run. Your health is saved.", 250, NamedTextColor.GRAY));

		// Initialize static triggers
		addTrigger("SPRINT", Trigger.TOGGLE_SPRINT, (pdata, in) -> {
			// Toggling sprint on or off both imply the player sprinted this tick
			hasSprinted = true;
			return TriggerResult.keep();
		});

		addTrigger("EXTRA_ARROWS", Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBasicAttack()) return TriggerResult.keep();
			if (extraShots.isEmpty()) return TriggerResult.keep();
			int period = Math.min(1, 10 / extraShots.size());
			PlayerFightData fd = this;

			ArrayList<ProjectileGroup> finalExtraShots = extraShots;
			extraShots = new ArrayList<ProjectileGroup>();
			addTask(new BukkitRunnable() {
				public void run() {
					if (finalExtraShots.isEmpty()) {
						this.cancel();
						return;
					}
					ProjectileGroup pg = finalExtraShots.removeFirst();
					List<ProjectileInstance> projs = pg.start(fd);
					for (ProjectileInstance proj : projs) {
						proj.getMeta().addTag(EXTRA_SHOT_TAG);
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 10L, period));
			return TriggerResult.keep();
		});

		// If the player has a TOGGLE_FLIGHT trigger, allow them to fly
		if (triggers.containsKey(Trigger.TOGGLE_FLIGHT)) {
			p.setAllowFlight(true);
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

		addTrigger("durability", Trigger.WIN_FIGHT, (pdata, in) -> {
			sessdata.tickDurability(getPlayer());
			return TriggerResult.keep();
		});

		updateStamina();
		updateMana();
		updateBoardLines();
	}

	public boolean isChanneling() {
		return hasStatus(StatusType.CHANNELING);
	}

	public TaskChain channel(int ticks) {
		// Make sounds every second while charging if over 1s
		if (ticks >= 20) {
			addWaitSound(Sounds.piano, ticks);
		}

		applyStatus(StatusType.CHANNELING, this, 1, ticks);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 7));
		disableJump(ticks);
		return new TaskChain(this, ticks);
	}

	public TaskChain charge(int ticks) {
		return charge(ticks, 1);
	}

	public TaskChain chargeSecs(double seconds) {
		return charge((int) (seconds * 20));
	}

	public TaskChain wandDelay(int ticks) {
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 1));
		return new TaskChain(this, ticks);
	}

	public TaskChain wandDelaySecs(double seconds) {
		return wandDelay((int) (seconds * 20));
	}

	public void disableJump(int ticks) {
		NamespacedKey key = NamespacedKey.fromString("jump", NeoRogue.inst());
		if (entity.getAttribute(Attribute.JUMP_STRENGTH).getModifier(key) != null) return;
		AttributeModifier mod = new AttributeModifier(key, -0.42,
				Operation.ADD_NUMBER);
		entity.getAttribute(Attribute.JUMP_STRENGTH).addModifier(mod);
		addGuaranteedTask(UUID.randomUUID(), new Runnable() {
			public void run() {
				entity.getAttribute(Attribute.JUMP_STRENGTH)
						.removeModifier(key);
			}
		}, ticks);
	}

	public void disableJump() {
		NamespacedKey key = NamespacedKey.fromString("jump", NeoRogue.inst());
		if (entity.getAttribute(Attribute.JUMP_STRENGTH).getModifier(key) != null) return;
		AttributeModifier mod = new AttributeModifier(key, -0.42,
				Operation.ADD_NUMBER);
		entity.getAttribute(Attribute.JUMP_STRENGTH).addModifier(mod);
	}

	public void enableJump() {
		entity.getAttribute(Attribute.JUMP_STRENGTH)
				.removeModifier(NamespacedKey.fromString("jump", NeoRogue.inst()));
	}

	public TaskChain charge(int ticks, int slow) {
		// Make sounds every second while charging if over 1s
		if (ticks >= 20) {
			addWaitSound(Sounds.piano, ticks);
		}

		applyStatus(StatusType.CHANNELING, this, 1, ticks);
		disableJump(ticks);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, slow));
		return new TaskChain(this, ticks);
	}

	private void addWaitSound(SoundContainer sc, int ticks) {
		addTask(new BukkitRunnable() {
			int tick = 0;

			public void run() {
				sc.play(p, p);
				tick += 20;
				if (tick >= ticks) {
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0, 20));
	}

	public HashMap<String, EquipmentInstance> getActiveEquipment() {
		return equips;
	}

	public HashMap<Trigger, ArrayList<PriorityAction>> getTriggers() {
		return triggers;
	}

	public HashMap<Integer, HashMap<Trigger, ArrayList<PriorityAction>>> getSlotBasedTriggers() {
		return slotBasedTriggers;
	}

	@Override
	public TickResult runTickActions() {
		if (isDead)
			return TickResult.KEEP;
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

	public void dash() {
		dash(p.getEyeLocation().getDirection());
	}

	@SuppressWarnings("deprecation")
	public void dash(Vector v) {
		runActions(this, Trigger.DASH, null);
		v = v.normalize();
		if (p.isOnGround()) {
			p.teleport(p.getLocation().add(0, 0.2, 0));
		}
		p.setVelocity(v.setY(0).normalize().multiply(1.5).setY(-1));
		applyStatus(StatusType.INVINCIBLE, this, 1, 10);
	}

	@Override
	public void updateDisplayName() {
		if (isDead || p == null || !p.isValid()) {
			removeHologram();
			return;
		}

		double health = p.getHealth();
		double shieldAmt = shields.getAmount();
		double healthPct = maxHealth > 0 ? health / maxHealth : 0;
		NamedTextColor healthColor;
		if (healthPct < 0.33) {
			healthColor = NamedTextColor.RED;
		} else if (healthPct < 0.67) {
			healthColor = NamedTextColor.YELLOW;
		} else {
			healthColor = NamedTextColor.GREEN;
		}

		// Example: 55 (+20) / 100 ♥, where (+20) is shields
		Component text = Component.text((int) Math.ceil(health), healthColor);
		if (shieldAmt > 0) {
			text = text.append(Component.text(" (+" + (int) Math.ceil(shieldAmt) + ")", NamedTextColor.YELLOW));
		}
		text = text.append(Component.text(" / ", NamedTextColor.WHITE))
				.append(Component.text((int) maxHealth, healthColor))
				.append(Component.text(" ♥", NamedTextColor.RED));
		renderHologram(text);
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
			if (s.isHidden())
				continue;
			if (s.getStacks() <= 0)
				continue;
			boardLines.add(s.getDisplay());
		}
		if (!boardLines.isEmpty())
			boardLines.add("§8§m-----");

		for (Player p : online) {
			if (p == this.p)
				continue;
			boardLines.add(createHealthBar(p));
		}
	}

	// Used for archer, like Magic Quiver
	public void addExtraShot(ProjectileGroup pg) {
		extraShots.add(pg);
	}

	private static String createHealthBar(Player p) {
		PlayerFightData pfd = FightInstance.getUserData(p.getUniqueId());
		if (pfd == null) {
			return "";
		}
		if (pfd != null && pfd.isDead) {
			return "&c&m" + p.getName();
		}
		double percenthp = p.getHealth() / p.getAttribute(Attribute.MAX_HEALTH).getValue();
		double percentShield = pfd.getShields().getAmount() / p.getAttribute(Attribute.MAX_HEALTH).getValue();
		percenthp *= 100;
		percentShield *= 100;
		int php = (int) percenthp;
		int psh = (int) percentShield;
		String color = "§a";
		if (php < 50 && php >= 25) {
			color = "§6";
		} else if (php < 25) {
			color = "§c";
		}

		// Add 5 so 25% is still 3/10 on the health bar
		int phpmod = (php + 5) / 10;
		int pshmod = (psh + 5) / 10;
		String bar = "" + color + (pshmod > 0 ? "§n" : "");
		for (int i = 0; i < 10; i++) {
			// If you have more shields than health, make the bars gray but keep the
			// underline
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
		this.isDead = isDead;
		if (isDead) {
			p.setInvulnerable(true);
			p.setInvisible(true);
			removeStatus(StatusType.POISON);
		} else {
			p.setInvulnerable(false);
			p.setInvisible(false);
			p.setHealth(Math.round(this.maxHealth * 0.05));
			// This one's more reliable, sometimes statuses may be applied when player is
			// dead
			removeStatus(StatusType.POISON);
		}
		updateDeathVisibility();
		updateDisplayName();
	}

	// Dead players are fully hidden from alive players. Visibility is restored when
	// they're revived or when the fight ends (both go through setDeath(false)).
	private void updateDeathVisibility() {
		FightInstance inst = getInstance();
		if (inst == null)
			return;
		for (Player other : inst.getSession().getOnlinePlayers()) {
			if (other.equals(p))
				continue;
			PlayerFightData od = FightInstance.getUserData(other.getUniqueId());
			boolean otherDead = od != null && od.isDead();
			if (isDead) {
				// Hide this newly-dead player from alive players, but let dead players
				// still see each other
				if (otherDead) {
					other.showPlayer(NeoRogue.inst(), p);
					p.showPlayer(NeoRogue.inst(), other);
				} else {
					other.hidePlayer(NeoRogue.inst(), p);
				}
			} else {
				// This player is alive again: reveal them to everyone, and hide any
				// players still dead from them
				other.showPlayer(NeoRogue.inst(), p);
				if (otherDead) {
					p.hidePlayer(NeoRogue.inst(), other);
				} else {
					p.showPlayer(NeoRogue.inst(), other);
				}
			}
		}
	}

	@Override
	public void applyStatus(Status s, FightData applier, int stacks, int seconds, DamageMeta meta,
			boolean isSecondary, Equipment source) {
		if (isDead)
			return;
		super.applyStatus(s, applier, stacks, seconds, meta, isSecondary, source);
	}

	public boolean isDead() {
		return isDead;
	}

	public boolean isDroppedThisTick() {
		return droppedThisTick;
	}

	public void setDroppedThisTick(boolean droppedThisTick) {
		this.droppedThisTick = droppedThisTick;
	}

	public void cleanup(PlayerSessionData data) {
		super.cleanup();

		if (isDead) {
			p.setInvisible(false);
			p.setInvulnerable(false);
		}

		// Perform end of fight actions (currently only used for resetting damage ticks)
		for (SessionEquipment acc : data.getSessionEquipment(EquipSlot.ACCESSORY)) {
			if (acc == null)
				continue;
			acc.getEquipment().cleanup(this);
		}
		for (SessionEquipment armor : data.getSessionEquipment(EquipSlot.ARMOR)) {
			if (armor == null)
				continue;
			armor.getEquipment().cleanup(this);
		}
		for (SessionEquipment hotbar : data.getSessionEquipment(EquipSlot.HOTBAR)) {
			if (hotbar == null)
				continue;
			hotbar.getEquipment().cleanup(this);
		}
		for (SessionEquipment other : data.getSessionEquipment(EquipSlot.KEYBIND)) {
			if (other == null)
				continue;
			other.getEquipment().cleanup(this);
		}
		for (ArtifactInstance art : data.getArtifacts().values()) {
			if (art == null)
				continue;
			art.cleanup(this);
		}

		if (data.getSessionEquipment(EquipSlot.OFFHAND)[0] != null) {
			data.getSessionEquipment(EquipSlot.OFFHAND)[0].getEquipment().cleanup(this);
		}

		for (Listener l : listeners) {
			HandlerList.unregisterAll(l);
		}
	}

	// Returns whether to cancel the event, which may or may not be ignored if it's
	// an event that can be cancelled
	public boolean runActions(PlayerFightData data, Trigger trigger, Object inputs) {
		return runActions(data, triggers, trigger, inputs);
	}

	// Must be separate due to the same trigger doing a different thing based on
	// slot (like weapons)
	public boolean runSlotBasedActions(PlayerFightData data, Trigger trigger, int slot, Object inputs) {
		if (!slotBasedTriggers.containsKey(slot))
			return false;
		HashMap<Trigger, ArrayList<PriorityAction>> triggers = slotBasedTriggers.get(slot);
		return runActions(data, triggers, trigger, inputs);
	}

	private boolean runActions(PlayerFightData data, HashMap<Trigger, ArrayList<PriorityAction>> triggers,
			Trigger trigger, Object inputs) {
		if (triggers.containsKey(trigger)) {
			boolean cancel = false;
			ArrayList<PriorityAction> actions = triggers.get(trigger);
			ArrayList<PriorityAction> snapshot = new ArrayList<>(actions);
			for (PriorityAction inst : snapshot) {
				TriggerResult tr;

				if (inst instanceof EquipmentInstance) {
					if (data.isChanneling() || data.hasStatus(StatusType.STOPPED)
							|| data.hasStatus(StatusType.SILENCED))
						return false;
					EquipmentInstance ei = (EquipmentInstance) inst;
					PreCastUsableEvent ev = new PreCastUsableEvent(ei, inputs);
					runActions(data, Trigger.PRE_CAST_USABLE, ev);

					// Check other conditions first (cooldown, custom conditions)
					if (!ei.canTrigger(p, data, inputs)) {
						continue;
					}

					boolean useResources = ei.shouldUseResource(p, data, inputs);
					// Mana
					BuffList b = ev.getBuff(PropertyType.MANA_COST);
					double manaCost = useResources ? Math.max(0, b.applyNegative(ei.getManaCost())) : 0;
					boolean needsMana = manaCost > data.getMana() && manaCost > 0;

					// Stamina
					b = ev.getBuff(PropertyType.STAMINA_COST);
					double staminaCost = useResources ? Math.max(0, b.applyNegative(ei.getStaminaCost())) : 0;
					boolean needsStamina = staminaCost > data.getStamina() && staminaCost > 0;
					Component msg = Component.text("You need ").color(NamedTextColor.RED);
					if (needsMana && needsStamina) {
						msg = msg.append(Component.text(df.format(manaCost - data.getMana())).color(NamedTextColor.BLUE))
								.append(Component.text(" mana and "))
								.append(Component.text(df.format(staminaCost - data.getStamina())).color(NamedTextColor.GREEN))
								.append(Component.text(" stamina!"));
					}
					else if (needsMana) {
						msg = msg.append(Component.text(df.format(manaCost - data.getMana())).color(NamedTextColor.BLUE))
								.append(Component.text(" mana!"));
					}
				    else if (needsStamina) {
						msg = msg.append(Component.text(df.format(staminaCost - data.getStamina())).color(NamedTextColor.GREEN))
								.append(Component.text(" stamina!"));
					}

					if (needsMana || needsStamina) {
						Sounds.error.play(p, p);
						Util.msgRaw(p, msg);
						continue;
					}

					// Cooldown
					b = ev.getBuff(PropertyType.COOLDOWN);
					double cooldown = Math.max(0, b.applyNegative(ei.getBaseCooldown()));
					
					// Passed checks, run stat trackers
					if (useResources) {
						calculateStatTrackers(ei.getStaminaCost(), ei.getStaminaCost() - staminaCost, b);
						calculateStatTrackers(ei.getManaCost(), ei.getManaCost() - manaCost, b);
					}
					calculateStatTrackers(ei.getBaseCooldown(), ei.getBaseCooldown() - cooldown, b);

					CastType type = ei.getEquipment().getProperties().getCastType();
					// If the cast type is not standard, it's up to the equipment to run the action
					// This is so CAST_USABLE doesn't get multi-triggered by toggled or recast abilities
					CastUsableEvent cuv = new CastUsableEvent(ei, type, manaCost, staminaCost, cooldown, inputs, ev.getTags());
					if (type == CastType.STANDARD) {
						runActions(data, Trigger.CAST_USABLE, cuv);
					}
					else {
						ei.setLastCastEvent(cuv);
					}
					data.addMana(-manaCost);
					data.addStamina(-staminaCost);
					if (!data.isIgnoreCooldowns()) ei.setCooldown(cooldown);
					tr = ei.trigger(data, inputs);
					ei.updateIcon();
				} else {
					if (!inst.canTrigger(p, data, inputs)) {
						continue;
					}
					tr = inst.trigger(data, inputs);
				}

				if (tr.removeTrigger()) {
					int hotbar = Trigger.toHotbarSlot(trigger);
					if (hotbar != -1) {
						p.getInventory().setItem(hotbar, null);
					}
					actions.remove(inst);
				}
				if (tr.cancelEvent()) {
					cancel = true;
					if (inputs instanceof ReceiveDamageEvent) {
						((ReceiveDamageEvent) inputs).setNullifiedSourceId(inst.getId());
					}
					break;
				}
				if (tr.breakLoop()) {
					break;
				}
			}
			p.updateInventory();
			return cancel;
		}
		return false;
	}

	private void calculateStatTrackers(double base, double diff, BuffList buffs) {
		Comparator<Buff> comp = new Comparator<Buff>() {
			@Override
			public int compare(Buff b1, Buff b2) {
				double change1 = b1.getEffectiveChange(base);
				double change2 = b2.getEffectiveChange(base);
				return Double.compare(change2, change1);
			}
		};
		ArrayList<Buff> sorted = new ArrayList<Buff>(buffs.getBuffs());
		Collections.sort(sorted, comp);

		for (Buff b : sorted) {
			if (b.getStatTracker() == null)
				continue;
			if (!(b.getApplier() instanceof PlayerFightData))
				continue;
				
			double change = b.getEffectiveChange(base);
			// Run through buffs and apply stats until the diff is 0
			if (diff > change) {
				((PlayerFightData) b.getApplier()).getStats().addBuffStat(b.getStatTracker(), change);
				diff -= change;
			}
			else {
				((PlayerFightData) b.getApplier()).getStats().addBuffStat(b.getStatTracker(), diff);
				break;
			}
		}
	}

	public void setAmmoInstance(AmmunitionInstance ammo) {
		this.ammo = ammo;
	}

	public AmmunitionInstance getAmmoInstance() {
		return ammo;
	}

	public boolean hasAmmoInstance() {
		return ammo != null;
	}

	public boolean hasTriggerAction(Trigger trigger) {
		return triggers.containsKey(trigger) && !triggers.get(trigger).isEmpty();
	}

	// Inventory slots disabled by a mob mechanic; the player can't switch to (or use) them until re-enabled
	private HashSet<Integer> disabledSlots = new HashSet<Integer>();

	public void setSlotDisabled(int slot, boolean disabled) {
		if (disabled) disabledSlots.add(slot);
		else disabledSlots.remove(slot);
	}

	public boolean isSlotDisabled(int slot) {
		return disabledSlots.contains(slot);
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
		return !isDead && p.isOnline(); // Not dead and online
	}

	@Override
	public FightInstance getInstance() {
		return inst;
	}

	public Player getPlayer() {
		return p;
	}

	public void updatePlayer() {
		this.p = Bukkit.getPlayer(uuid);
		this.entity = this.p;
		// The old hologram was mounted on the stale player entity; recreate it fresh
		removeHologram();

		for (EquipmentInstance eqi : equips.values()) {
			eqi.updatePlayer(p);
			eqi.updateIcon();
		}
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

	public void setMaxStamina(double amount) {
		this.maxStamina = amount;
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
		this.stamina = Math.max(0, Math.min(this.stamina, this.maxStamina));
		if (hasStatus(StatusType.WITHERED)) {
			p.setFoodLevel(0);
			// Disable jump with a separate key so it doesn't conflict with charge
			NamespacedKey key = NamespacedKey.fromString("withered", NeoRogue.inst());
			if (entity.getAttribute(Attribute.JUMP_STRENGTH).getModifier(key) == null) {
				AttributeModifier mod = new AttributeModifier(key, -0.42, Operation.ADD_NUMBER);
				entity.getAttribute(Attribute.JUMP_STRENGTH).addModifier(mod);
			}
		} else {
			p.setFoodLevel((int) Math.ceil(this.stamina * 14 / (int) sessdata.getMaxStamina()) + 6);
			// Remove withered jump modifier if present
			NamespacedKey key = NamespacedKey.fromString("withered", NeoRogue.inst());
			if (entity.getAttribute(Attribute.JUMP_STRENGTH).getModifier(key) != null) {
				entity.getAttribute(Attribute.JUMP_STRENGTH).removeModifier(key);
			}
		}
		updateActionBar();
		updateBoardLines();
	}

	@Override
	public void addHealth(double amount) {
		addHealth(amount, null);
	}

	@Override
	public void addHealth(double amount, Equipment source) {
		double curr = p.getHealth();
		double after = Math.min(this.maxHealth, curr + amount);
		double healed = after - curr;
		stats.addSelfHealing(healed);
		stats.addHealingDone(source, healed);
		p.setHealth(after);
		updateDisplayName();
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

	public void setMaxMana(double amount) {
		this.maxMana = amount;
		updateMana();
	}

	public void setMana(double amount) {
		this.mana = amount;
		updateMana();
	}

	public void setManaUncapped(double amount) {
		this.mana = amount;
		updateActionBar();
		updateBoardLines();
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

	public double getManaRegen() {
		return manaRegen;
	}

	public double getStaminaRegen() {
		return staminaRegen;
	}

	public void addSprintCost(double amount) {
		this.sprintCost += amount;
	}

	public void setSprintCost(double amount) {
		this.sprintCost = amount;
	}

	public double getSprintCost() {
		return sprintCost;
	}

	private void updateMana() {
		this.mana = Math.max(0, Math.min(this.mana, this.maxMana));
		updateActionBar();
		updateBoardLines();
	}

	public void updateActionBar() {
		boolean invincible = hasStatus(StatusType.INVINCIBLE);
		NamedTextColor hpColor = invincible ? NamedTextColor.AQUA : NamedTextColor.RED;
		TextComponent hp = Component.text(invincible ? "✦ HP: " : "HP: ", hpColor)
				.append(Component.text((int) getPlayer().getHealth(), hpColor));
		if (invincible) {
			hp = hp.decoration(TextDecoration.BOLD, true);
		}
		if (shields.getAmount() > 0) {
			hp = hp.append(Component.text("+" + (int) getShields().getAmount(), NamedTextColor.YELLOW));
		}
		Component bar = hp.append(Component.text(" / " + (int) maxHealth + (invincible ? " ✦" : ""), hpColor))
				.append(Component.text("  |  ", NamedTextColor.GRAY))
				.append(Component.text("MP: " + (int) mana + " / " + (int) maxMana, NamedTextColor.BLUE))
				.append(Component.text("  |  ", NamedTextColor.GRAY))
				.append(Component.text("SP: " + (int) stamina + " / " + (int) maxStamina, NamedTextColor.GREEN));
		p.sendActionBar(bar);
		updateDisplayName();
	}

	public void addTrap(Trap trap) {
		traps.put(trap.getUniqueId(), trap);
		LayTrapEvent ev = new LayTrapEvent(trap);
		runActions(this, Trigger.LAY_TRAP, ev);
		trap.setDuration((int) ev.getDurationBuffList().apply(trap.getDuration()));
		trap.activate();
	}

	public void addMarker(Marker marker) {
		markers.put(marker.getUniqueId(), marker);
		marker.activate();
	}

	public void removeMarker(Marker marker) {
		if (markers.remove(marker.getUniqueId()) != null) {
			marker.deactivate();
		}
	}

	public void removeTrap(Trap trap) {
		if (traps.remove(trap.getUniqueId()) != null) {
			trap.deactivate();
		}
	}

	public HashMap<UUID, Trap> getTraps() {
		return traps;
	}

	public void addRift(Rift rift) {
		rifts.put(rift.getUniqueId(), rift);
		CreateRiftEvent ev = new CreateRiftEvent(rift);
		runActions(this, Trigger.CREATE_RIFT, ev);
		rift.setDuration((int) ev.getDurationBuffList().apply(rift.getDuration()));
		rift.activate();
	}

	public void removeRift(Rift rift) {
		rift.deactivate();
		rifts.remove(rift.getUniqueId());
	}

	public HashMap<UUID, Rift> getRifts() {
		return rifts;
	}

	private class PlayerUpdateTickAction extends TickAction {
		private int tick = 0;
		@Override
		public TickResult run() {
			addMana(manaRegen);

			double staminaChange = staminaRegen;
			if (!hasStatus(StatusType.WITHERED)) {
				if (p.isSprinting() && tick > 2) // Sprint cost only applies 2 seconds of the fight starting
					staminaChange -= sprintCost;
				else if (hasSprinted && tick > 2) // Player sprinted this tick, but is not currently sprinting
					staminaChange -= sprintCost / 2;
			}
			addStamina(staminaChange);
			hasSprinted = false;

			updateBoardLines();
			FightInstance.trigger(p, Trigger.PLAYER_TICK, null);
			tick++;
			return TickResult.KEEP;
		}
	}

	public boolean canBasicAttack() {
		return nextAttack <= System.currentTimeMillis();
	}

	public void setBasicAttackCooldown(EquipSlot slot, double attackSpeed) {
		if (hasStatus(StatusType.FROSTBITE)) attackSpeed *= 0.5;
		long attackCooldown = (long) (1000 / attackSpeed) - 50; // Subtract 50 for tick differentials

		if (slot == EquipSlot.HOTBAR)
			this.nextAttack = System.currentTimeMillis() + attackCooldown;
		else
			this.nextOffAttack = System.currentTimeMillis() + attackCooldown;
	}

	public void setBasicAttackCooldown(EquipSlot slot, long cooldown) {
		if (slot == EquipSlot.HOTBAR)
			this.nextAttack = System.currentTimeMillis() + cooldown;
		else
			this.nextOffAttack = System.currentTimeMillis() + cooldown;
	}

	public void resetBasicAttackCooldown(EquipSlot slot) {
		if (slot == EquipSlot.HOTBAR)
			this.nextAttack = 0;
		else
			this.nextOffAttack = 0;
	}

	public boolean canBasicAttack(EquipSlot slot) {
		if (slot == EquipSlot.HOTBAR)
			return nextAttack <= System.currentTimeMillis();
		else
			return nextOffAttack <= System.currentTimeMillis();
	}

	public void addMaxHealth(double amount) {
		this.maxHealth += amount;
		getPlayer().getAttribute(Attribute.MAX_HEALTH).setBaseValue(this.maxHealth);
	}

}
