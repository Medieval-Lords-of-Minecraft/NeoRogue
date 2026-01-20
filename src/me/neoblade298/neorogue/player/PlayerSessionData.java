package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.abilities.EmpoweredEdge;
import me.neoblade298.neorogue.equipment.abilities.ManaBlitz;
import me.neoblade298.neorogue.equipment.abilities.PiercingShot;
import me.neoblade298.neorogue.equipment.abilities.ShadowWalk;
import me.neoblade298.neorogue.equipment.weapons.BasicBow;
import me.neoblade298.neorogue.equipment.weapons.WoodenArrow;
import me.neoblade298.neorogue.equipment.weapons.WoodenDagger;
import me.neoblade298.neorogue.equipment.weapons.WoodenSword;
import me.neoblade298.neorogue.equipment.weapons.WoodenWand;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionAction;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerSessionData extends MapViewer implements Comparable<PlayerSessionData> {
	private PlayerData data;
	private EquipmentClass ec;
	private double maxHealth, maxMana, maxStamina, health, startingMana, startingStamina, manaRegen, staminaRegen;
	private Equipment[] hotbar = new Equipment[9];
	private Equipment[] armors = new Equipment[3];
	private Equipment[] offhand = new Equipment[1];
	private Equipment[] accessories = new Equipment[6];
	private Equipment[] storage = new Equipment[MAX_STORAGE_SIZE];
	private Equipment[] otherBinds = new Equipment[8];
	private TreeMap<String, ArtifactInstance> artifacts = new TreeMap<String, ArtifactInstance>();
	private HashMap<SessionTrigger, ArrayList<SessionAction>> triggers = new HashMap<SessionTrigger, ArrayList<SessionAction>>();
	private int abilitiesEquipped, armorEquipped, accessoriesEquipped, maxAbilities = 4, maxStorage = 3, coins = 100, armorSlots = 1, accessorySlots = 2;
	private String instanceData;
	private DropTableSet<Artifact> personalArtifacts;
	private ArrayList<String> boardLines;

	private static final ParticleContainer heal = new ParticleContainer(Particle.HAPPY_VILLAGER).count(50)
			.spread(0.5, 1).speed(0.1).forceVisible(Audience.ALL);
	public static final int MAX_STORAGE_SIZE = 27, ARMOR_SIZE = 3, ACCESSORY_SIZE = 6;
	private static final DecimalFormat df = new DecimalFormat("#.##");

	public PlayerSessionData(UUID uuid, Session s, ResultSet rs) throws SQLException {
		super(s, uuid);
		data = PlayerManager.getPlayerData(uuid);

		this.ec = EquipmentClass.valueOf(rs.getString("playerClass"));
		this.maxHealth = rs.getDouble("maxHealth");
		this.maxMana = rs.getDouble("maxMana");
		this.maxStamina = rs.getDouble("maxStamina");
		this.health = rs.getDouble("health");
		this.startingMana = rs.getDouble("startingMana");
		this.startingStamina = rs.getDouble("startingStamina");
		this.manaRegen = rs.getDouble("manaRegen");
		this.staminaRegen = rs.getDouble("staminaRegen");
		this.hotbar = Equipment.deserializeAsArray(rs.getString("hotbar"));
		this.armors = Equipment.deserializeAsArray(rs.getString("armors"));
		this.offhand = Equipment.deserializeAsArray(rs.getString("offhand"));
		this.accessories = Equipment.deserializeAsArray(rs.getString("accessories"));
		this.storage = Equipment.deserializeAsArray(rs.getString("storage"));
		this.otherBinds = Equipment.deserializeAsArray(rs.getString("otherBinds"));
		this.artifacts = ArtifactInstance.deserializeMap(rs.getString("artifacts"));
		this.maxAbilities = rs.getInt("maxAbilities");
		this.maxStorage = rs.getInt("maxStorage");
		this.coins = rs.getInt("coins");
		this.armorSlots = rs.getInt("armorSlots");
		this.accessorySlots = rs.getInt("accessorySlots");
		this.instanceData = rs.getString("instanceData");

		updateEquipmentLimits();

		setupArtifacts();
		updateBoardLines();
	}

	public PlayerSessionData(UUID uuid, EquipmentClass ec, Session s) {
		super(s, uuid);
		data = PlayerManager.getPlayerData(uuid);
		health = 100;
		maxHealth = 100;
		maxMana = 50;
		maxStamina = 50;
		manaRegen = 2;
		staminaRegen = 2;
		health = maxHealth;
		this.ec = ec;
		
		// Starting equipment
		// If you ever use abilities equipped, need to initialize it to 1 here
		switch (this.ec) {
		case WARRIOR:
			hotbar[0] = WoodenSword.get();
			hotbar[1] = EmpoweredEdge.get();
			abilitiesEquipped = 1;
			maxStamina = 50;
			maxMana = 25;
			staminaRegen = 2;
			manaRegen = 1;
			break;
		case THIEF:
			hotbar[0] = WoodenDagger.get();
			hotbar[1] = ShadowWalk.get();
			abilitiesEquipped = 1;
			maxStamina = 45;
			maxMana = 30;
			staminaRegen = 1.8;
			manaRegen = 1.2;
			break;
		case ARCHER:
			hotbar[0] = BasicBow.get();
			hotbar[1] = PiercingShot.get();
			hotbar[8] = WoodenArrow.get();
			abilitiesEquipped = 1;
			maxStamina = 40;
			maxMana = 35;
			staminaRegen = 1.6;
			manaRegen = 1.4;
			break;
		case MAGE:
			hotbar[0] = WoodenWand.get();
			hotbar[1] = ManaBlitz.get();
			abilitiesEquipped = 1;
			maxStamina = 25;
			maxMana = 50;
			staminaRegen = 1;
			manaRegen = 2;
			break;
		default:
			break;
		}

		setupArtifacts();
		data.getPlayer().setHealthScaled(true);
		data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
		data.initialize(s, this);
	}

	public void updateEquipmentLimits() {
		abilitiesEquipped = aggregateEquipment((eq) -> {
			return eq.getEquipment().getType() == EquipmentType.ABILITY && eq.getEquipSlot() != EquipSlot.STORAGE;
		}).size();
		armorEquipped = aggregateEquipment((eq) -> {
			return eq.getEquipment().getType() == EquipmentType.ARMOR && eq.getEquipSlot() != EquipSlot.STORAGE;
		}).size();
		accessoriesEquipped = aggregateEquipment((eq) -> {
			return eq.getEquipment().getType() == EquipmentType.ACCESSORY && eq.getEquipSlot() != EquipSlot.STORAGE;
		}).size();
	}

	public void cleanup() {
		data.getPlayer().setHealthScaled(false);
		data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
	}

	public UUID getUniqueId() {
		return uuid;
	}

	private void setupArtifacts() {
		personalArtifacts = Equipment.copyArtifactsDropSet(ec, EquipmentClass.CLASSLESS);
		for (ArtifactInstance ai : artifacts.values()) {
			ai.getArtifact().onInitializeSession(this);

			// Artifacts that can stack do not get removed from the droptable if you have one
			if (ai.getArtifact().canStack()) continue;
			personalArtifacts.remove(ai.getArtifact());
		}
	}

	public void setupInventory() {
		Player p = data.getPlayer();
		p.getInventory().clear();
		PlayerSessionInventory.setupInventory(p.getInventory(), this);
		updateCoinsBar();
		getPlayer().setSaturation(20);
	}

	public Player getPlayer() {
		return data.getPlayer();
	}

	public Equipment[] getEquipment(EquipSlot es) {
		return getArrayFromEquipSlot(es);
	}

	public void upgradeEquipment(EquipSlot es, int slot) {
		Equipment[] slots = getArrayFromEquipSlot(es);
		slots[slot] = slots[slot].getUpgraded();
		PlayerSessionInventory.setupInventory(data.getPlayer().getInventory(), this);
	}

	public void setEquipment(EquipSlot es, int slot, Equipment eq) {
		Equipment[] slots = getArrayFromEquipSlot(es);
		if (slots[slot] != null) removeEquipment(es, slot);
		slots[slot] = eq;

		switch (eq.getType()) {
		case ARMOR:
			armorEquipped++;
			break;
		case ACCESSORY:
			accessoriesEquipped++;
			break;
		case ABILITY:
			abilitiesEquipped++;
			break;
		default:
		}
	}

	public Equipment removeEquipment(EquipSlot es, int slot) {
		Equipment[] slots = getArrayFromEquipSlot(es);
		Equipment eq = slots[slot];
		slots[slot] = null;

		switch (eq.getType()) {
		case ARMOR:
			armorEquipped--;
			break;
		case ACCESSORY:
			accessoriesEquipped--;
			break;
		case ABILITY:
			abilitiesEquipped--;
			break;
		default:
		}
		return eq;
	}

	// Used for curses to auto unequip an item, only does so if all slots are used
	// Returns true if an item was unequipped
	public boolean unequip(EquipmentType type) {
		Equipment eq = null;
		EquipmentMetadata result;
		switch (type) {
			case ARMOR:
				if (armorEquipped < armorSlots) return false;
				result = aggregateEquipment((meta) -> {
					return meta.getEquipment().getType() == EquipmentType.ARMOR && !meta.getEquipment().isCursed()
							&& meta.getEquipSlot() != EquipSlot.STORAGE;
				}).getLast();
				eq = removeEquipment(result.getEquipSlot(), result.getSlot());
				sendToStorage(eq);
				break;
			case ACCESSORY:
				if (accessoriesEquipped < accessorySlots) return false;
				result = aggregateEquipment((meta) -> {
					return meta.getEquipment().getType() == EquipmentType.ACCESSORY && !meta.getEquipment().isCursed()
							&& meta.getEquipSlot() != EquipSlot.STORAGE;
				}).getLast();
				eq = removeEquipment(result.getEquipSlot(), result.getSlot());
				sendToStorage(eq);
				break;
			case ABILITY:
				if (abilitiesEquipped < maxAbilities) return false;
				result = aggregateEquipment((meta) -> { return meta.getEquipment().getType() == EquipmentType.ABILITY && !meta.getEquipment().isCursed() && meta.getEquipSlot() != EquipSlot.STORAGE; }).getLast();
				eq = removeEquipment(result.getEquipSlot(), result.getSlot());
				sendToStorage(eq);
				break;
			default:
				Bukkit.getLogger().warning("[NeoRogue] Unaccounted case for unequip with equipment type " + type);
				break;
		}

		if (eq != null) {
			PlayerSessionInventory.setupInventory(getPlayer().getInventory(), this);
			Util.msg(getPlayer(), Component.text("You unequipped ", NamedTextColor.GRAY).append(eq.getHoverable()).append(Component.text(", it was sent to storage.")));
			return true;
		}
		return false;
	}

	private Equipment[] getArrayFromEquipSlot(EquipSlot es) {
		Equipment[] slots = null;
		switch (es) {
		case ARMOR:
			slots = armors;
			break;
		case ACCESSORY:
			slots = accessories;
			break;
		case HOTBAR:
			slots = hotbar;
			break;
		case KEYBIND:
			slots = otherBinds;
			break;
		case OFFHAND:
			slots = offhand;
			break;
		case STORAGE:
			slots = storage;
			break;
		default:
			Bukkit.getLogger().warning("[NeoRogue] Tried to modify equipment for invalid equip slot " + es);
			break;
		}
		return slots;
	}

	public Equipment[] getStorage() {
		return storage;
	}
	
	public void setStorage(Equipment[] storage) {
		this.storage = storage;
	}

	public void setOffhand(Equipment offhand) {
		this.offhand[0] = offhand;
	}

	public Equipment[] getOtherBinds() {
		return otherBinds;
	}

	public void addArmorSlots(int amount) {
		this.armorSlots += amount;
	}
	
	public void addAccessorySlots(int amount) {
		this.accessorySlots += amount;
	}

	public Equipment getOtherBind(KeyBind bind) {
		return otherBinds[bind.getDataSlot()];
	}

	public boolean canEquipAbility() {
		return abilitiesEquipped < maxAbilities;
	}

	public int getMaxAbilities() {
		return maxAbilities;
	}

	public void addAbilityEquipped(int num) {
		abilitiesEquipped += num;
	}

	public int getAbilitiesEquipped() {
		return abilitiesEquipped;
	}

	public PlayerData getData() {
		return data;
	}

	public double getMaxHealth() {
		return maxHealth;
	}

	public double getMaxMana() {
		return maxMana;
	}

	public double getMaxStamina() {
		return maxStamina;
	}

	public TreeMap<String, ArtifactInstance> getArtifacts() {
		return artifacts;
	}

	public int getMaxStorage() {
		return maxStorage;
	}

	public void addMaxStorage(int amount) {
		this.maxStorage += amount;
		if (maxStorage > MAX_STORAGE_SIZE) maxStorage = MAX_STORAGE_SIZE;
	}

	public double getManaRegen() {
		return manaRegen;
	}
	
	public void addManaRegen(double amount) {
		this.manaRegen += amount;
	}

	public double getStaminaRegen() {
		return staminaRegen;
	}
	
	public void addStaminaRegen(double amount) {
		this.staminaRegen += amount;
	}

	public void addTrigger(String id, SessionTrigger trigger, SessionAction action) {
		ArrayList<SessionAction> actions = triggers.containsKey(trigger) ? triggers.get(trigger)
				: new ArrayList<SessionAction>();
		actions.add(action);
		triggers.putIfAbsent(trigger, actions);
	}
	
	public void trigger(SessionTrigger trigger, Object inputs) {
		if (!triggers.containsKey(trigger)) return;
		
		for (SessionAction action : triggers.get(trigger)) {
			action.trigger(this, inputs);
		}
	}

	public void removeArtifact(Artifact artifact) {
		removeArtifact(artifact, 1);
	}

	public void removeArtifact(Artifact artifact, int amount) {
		if (artifacts.containsKey(artifact.getId())) {
			ArtifactInstance inst = artifacts.get(artifact.getId());
			inst.add(-amount);
			if (inst.getAmount() <= 1) artifacts.remove(artifact.getId());
		}
	}

	public void giveArtifact(Artifact artifact, int amount) {
		ArtifactInstance inst;
		if (artifacts.containsKey(artifact.getId())) {
			inst = artifacts.get(artifact.getId());
			inst.add(amount);
		}
		else {
			inst = new ArtifactInstance(artifact);
			artifacts.put(artifact.getId(), inst);
			inst.getArtifact().onInitializeSession(this);
			if (!artifact.canStack()) personalArtifacts.remove(artifact);
		}
		inst.getArtifact().onAcquire(this, amount);

		// If you want customizable broadcast message, you'll need to refactor a bit
		Player p = data.getPlayer();
		Component toSelf = SharedUtil.color("You received ");
		Component toOthers = SharedUtil.color("<yellow>" + data.getDisplay() + "</yellow> received ");
		Component body = Component.text(amount + " ", NamedTextColor.YELLOW).append(artifact.getHoverable()).append(Component.text(".", NamedTextColor.GRAY));
		s.broadcastOthers(toOthers.append(body), p);
		Util.msg(p, toSelf.append(body));
	}

	private void giveArtifact(Artifact artifact) {
		ArtifactInstance inst;
		if (artifacts.containsKey(artifact.getId())) {
			inst = artifacts.get(artifact.getId());
			inst.add(1);
		}
		else {
			inst = new ArtifactInstance(artifact);
			artifacts.put(artifact.getId(), inst);
			if (!artifact.canStack()) personalArtifacts.remove(artifact);
			inst.getArtifact().onInitializeSession(this);
		}

		inst.getArtifact().onAcquire(this, 1);
	}
	
	public void giveEquipmentSilent(Equipment eq) {
		giveEquipment(eq, null, null);
	}

	// If components null, no broadcast
	public void giveEquipment(Equipment eq, Component toSelf, Component toOthers) {
		Player p = getPlayer();
		if (toSelf != null) {
			s.broadcastOthers(toOthers.append(eq.getHoverable()).append(Component.text(".")), p);
			toSelf = toSelf.append(eq.getHoverable());
		}

		if (eq instanceof Artifact) {
			if (toSelf != null) {
				Util.msg(p, toSelf.append(Component.text(".")));
			}
			giveArtifact((Artifact) eq);
		}
		else {
			// First try to auto-equip
			boolean success = false;
			boolean blockEquip = (eq.getType() == EquipmentType.ACCESSORY && accessoriesEquipped >= accessorySlots)
					|| (eq.getType() == EquipmentType.ARMOR && armorEquipped >= armorSlots)
					|| (eq.getType() == EquipmentType.ABILITY && abilitiesEquipped >= maxAbilities);
			if (!blockEquip) {
				EquipSlot es = null;
				for (EquipSlot eqs : eq.getType().getSlots()) {
					success = tryEquip(eqs, eq);
					if (success) {
						es = eqs;
						break;
					}
				}
				if (success) {
					if (toSelf != null) Util.msg(p, toSelf.append(SharedUtil.color(", it was auto-equipped to " + es.getDisplay() + ".")));
					PlayerSessionInventory.setupInventory(p.getInventory(), this);
					return;
				}
			}
			
			// If unable to, send it to storage
			if (sendToStorage(eq)) {
				if (toSelf != null) Util.msg(p, toSelf.append(SharedUtil.color(", it was sent to storage.")));
			}
			else {
				// Should basically never happen
				Util.displayError(p, "Your storage is full!");
			}
		}
	}
	
	public boolean sendToStorage(Equipment eq) {
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null) {
				storage[i] = eq;
				return true;
			}
		}
		return false;
	}

	public void giveEquipment(Equipment eq) {
		giveEquipment(eq, SharedUtil.color("You received "),
				SharedUtil.color("<yellow>" + data.getDisplay() + "</yellow> received "));
	}

	public void giveEquipment(ArrayList<? extends Equipment> eqs) {
		for (Equipment eq : eqs) {
			giveEquipment(eq, SharedUtil.color("You received "),
					SharedUtil.color("<yellow>" + data.getDisplay() + "</yellow> received "));
		}
	}

	private boolean tryEquip(EquipSlot es, Equipment eq) {
		Equipment[] arr = getArrayFromEquipSlot(es);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				setEquipment(es, i, eq);
				return true;
			}
		}
		return false;
	}
	
	private Equipment[][] getAllEquips() {
		return new Equipment[][] { hotbar, armors, offhand, accessories, storage, otherBinds };
	}
	
	public ArrayList<EquipmentMetadata> aggregateEquipment(Predicate<EquipmentMetadata> filter) {
		ArrayList<EquipmentMetadata> list = new ArrayList<EquipmentMetadata>();
		EquipSlot[] es = new EquipSlot[] { EquipSlot.HOTBAR, EquipSlot.ARMOR, EquipSlot.OFFHAND, EquipSlot.ACCESSORY, EquipSlot.STORAGE, EquipSlot.KEYBIND };
		
		int esIdx = -1;
		for (Equipment[] arr : getAllEquips()) {
			esIdx++;
			int slot = -1;
			for (Equipment eq : arr) {
				slot++;
				if (eq == null) continue;
				EquipmentMetadata meta = new EquipmentMetadata(eq, slot, es[esIdx]);
				if (filter.test(meta)) list.add(meta);
			}
		}
		return list;
	}
	
	public class EquipmentMetadata {
		private Equipment eq;
		private int slot;
		private EquipSlot es;
		public EquipmentMetadata(Equipment eq, int slot, EquipSlot es) {
			this.eq = eq;
			this.slot = slot;
			this.es = es;
		}
		public Equipment getEquipment() {
			return eq;
		}
		public int getSlot() {
			return slot;
		}
		public EquipSlot getEquipSlot() {
			return es;
		}
	}

	public boolean hasUnequippedCurses() {
		Player p = data.getPlayer();

		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null) continue;
			Equipment eq = storage[i];
			if (eq.isCursed()) {
				Util.displayError(p, "All cursed items must be equipped before continuing!");
				return true;
			}
		}
		return false;
	}
	
	public boolean exceedsStorageLimit() {
		Player p = data.getPlayer();
		int size = 0;
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null) continue;
			size++;
		}
		if (size > maxStorage) {
			Util.displayError(p, "Your storage exceeds the maximum storage limit! You must remove some items before you can continue!");
			return true;
		}
		return false;
	}

	public boolean hasCoins(int amount) {
		return coins >= amount;
	}

	public void addCoins(int amount) {
		coins += amount;
		String symbol = amount > 0 ? "+" : "";
		Util.msg(getPlayer(), "<yellow>" + symbol + amount + " coins </yellow>(<gold>" + coins + "</gold>)");
		updateCoinsBar();
		updateBoardLines();
	}

	public int getCoins() {
		return coins;
	}

	public EquipmentClass getPlayerClass() {
		return ec;
	}

	public void addMaxAbilities(int amount) {
		this.maxAbilities += amount;
	}

	public void addMaxHealth(int amount) {
		this.maxHealth += amount;
		this.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
	}

	public void addMaxStamina(int amount) {
		this.maxStamina += amount;
		updateBoardLines();
	}

	public void addMaxMana(int amount) {
		this.maxMana += amount;
		updateBoardLines();
	}

	public void addStartingStamina(int amount) {
		this.startingStamina += amount;
	}

	public void addStartingMana(int amount) {
		this.startingMana += amount;
	}

	public Session getSession() {
		return s;
	}

	public double getStartingMana() {
		return startingMana;
	}

	public double getStartingStamina() {
		return startingStamina;
	}

	public double getHealth() {
		return health;
	}
	
	public int getArmorSlots() {
		return armorSlots;
	}

	public int getArmorEquipped() {
		return armorEquipped;
	}

	public int getAccessorySlots() {
		return accessorySlots;
	}
	
	public int getAccessoriesEquipped() {
		return accessoriesEquipped;
	}

	public void updateHealth() {
		health = Math.round(Math.min(this.maxHealth, getPlayer().getHealth()));
	}

	public void revertMaxHealth() {
		getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHealth);
	}

	public void updateCoinsBar() {
		Player p = getPlayer();
		p.setLevel(coins);
		p.setExp(0);
	}

	public void syncHealth() {
		getPlayer().setHealth(this.health);
	}

	public void setHealth(double health) {
		if (this.health > health) getPlayer().damage(0.1);
		this.health = Math.min(health, maxHealth);
		getPlayer().setHealth(this.health);
		updateBoardLines();
	}

	public void healPercent(double percent) {
		Player p = getPlayer();
		setHealth(this.health + (percent * maxHealth));
		heal.play(p, p);
		Sounds.levelup.play(p, p);
		updateBoardLines();
	}

	public void damagePercent(double percent) {
		setHealth(this.health - (percent * maxHealth));
	}

	public void setInstanceData(String str) {
		this.instanceData = str;
	}

	public String getInstanceData() {
		return instanceData;
	}

	public DropTableSet<Artifact> getArtifactDroptable() {
		return personalArtifacts;
	}

	public void updateBoardLines() {
		boardLines = new ArrayList<String>();
		boardLines.add("§cHP§7: §f" + (int) health + "§7 / §f" + (int) maxHealth);
		boardLines.add("§9MP§7: §f" + maxMana + " §7| §f" + df.format(manaRegen) + "/s");
		boardLines.add("§aSP§7: §f" + maxStamina + " §7| §f" + df.format(staminaRegen) + "/s");
		boardLines.add("§eCoins§7: §f" + coins);
		s.updateSpectatorLines();
		if (s.getParty().size() <= 1) return;
		boardLines.add("§8§m-----");
		for (PlayerSessionData psd : s.getParty().values()) {
			if (psd == this) continue;
			boardLines.add("§e" + psd.getData().getDisplay() + "§7: §f" + Math.round(psd.getHealth()) + "§c♥");
		}
	}

	public ArrayList<String> getBoardLines() {
		return boardLines;
	}

	public void save(Statement stmt) {
		UUID host = s.getHost();
		String uuid = data.getPlayer().getUniqueId().toString();
		int saveSlot = s.getSaveSlot();
		try {
			SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playersessiondata")
					.addString(host.toString()).addValue(saveSlot).addString(uuid)
					.addString(((TextComponent) data.getPlayer().displayName()).content()).addString(ec.name())
					.addValue(maxHealth).addValue(maxMana).addValue(maxStamina).addValue(health).addValue(startingMana)
					.addValue(startingStamina).addValue(manaRegen).addValue(staminaRegen)
					.addString(Equipment.serialize(hotbar)).addString(Equipment.serialize(armors))
					.addString(Equipment.serialize(offhand)).addString(Equipment.serialize(accessories))
					.addString(Equipment.serialize(storage)).addString(Equipment.serialize(otherBinds))
					.addString(ArtifactInstance.serialize(artifacts)).addValue(maxAbilities).addValue(maxStorage)
					.addValue(armorSlots).addValue(accessorySlots).addValue(coins).addString(instanceData);
			stmt.execute(sql.build());
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save player session data for " + uuid + " hosted by "
					+ host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
	}


	// Notably, this isn't used for saving to SQL, only for the admin command
	public String serialize() {
		return ec.name() + "," + health + "," + maxHealth + "," + maxMana + "," + maxStamina + "," + manaRegen + "," + staminaRegen + "," +
			Equipment.serialize(hotbar) + "," + Equipment.serialize(armors) + "," + Equipment.serialize(offhand) + "," +
			Equipment.serialize(accessories) + "," + Equipment.serialize(storage) + "," + Equipment.serialize(otherBinds) + "," +
			ArtifactInstance.serialize(artifacts) + "," + maxAbilities + "," + maxStorage + "," + armorSlots + "," + accessorySlots + "," + coins;
	}

	public void deserialize(String str) throws Exception {
		String[] arr = str.split(",");
		int i = 0;
		ec = EquipmentClass.valueOf(arr[i++]);
		health = Double.parseDouble(arr[i++]);
		maxHealth = Double.parseDouble(arr[i++]);
		maxMana = Double.parseDouble(arr[i++]);
		maxStamina = Double.parseDouble(arr[i++]);
		manaRegen = Double.parseDouble(arr[i++]);
		staminaRegen = Double.parseDouble(arr[i++]);
		hotbar = Equipment.deserializeAsArray(arr[i++]);
		armors = Equipment.deserializeAsArray(arr[i++]);
		offhand = Equipment.deserializeAsArray(arr[i++]);
		accessories = Equipment.deserializeAsArray(arr[i++]);
		storage = Equipment.deserializeAsArray(arr[i++]);
		otherBinds = Equipment.deserializeAsArray(arr[i++]);
		artifacts = ArtifactInstance.deserializeMap(arr[i++]);
		maxAbilities = Integer.parseInt(arr[i++]);
		maxStorage = Integer.parseInt(arr[i++]);
		armorSlots = Integer.parseInt(arr[i++]);
		accessorySlots = Integer.parseInt(arr[i++]);
		coins = Integer.parseInt(arr[i++]);

		getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
		getPlayer().setHealth(health);

		// Need to initialize artifacts after deserialization
		setupArtifacts();
		updateEquipmentLimits();
	}

	public class PlayerSlot {
		private int slot;
		private EquipSlot es;

		public PlayerSlot(EquipSlot es, int slot) {
			this.slot = slot;
			this.es = es;
		}

		public int getSlot() {
			return slot;
		}

		public EquipSlot getEquipSlot() {
			return es;
		}
	}

	@Override
	public int compareTo(PlayerSessionData o) {
		return this.data.getDisplay().compareTo(o.getData().getDisplay());
	}
}
