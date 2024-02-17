package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neocore.shared.util.SQLInsertBuilder.SQLAction;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerSessionData {
	private PlayerData data;
	private UUID uuid;
	private Session s;
	private EquipmentClass ec;
	private double maxHealth, maxMana, maxStamina, health, startingMana, startingStamina, manaRegen, staminaRegen;
	private Equipment[] hotbar = new Equipment[9];
	private Equipment[] armors = new Equipment[3];
	private Equipment[] offhand = new Equipment[1];
	private Equipment[] accessories = new Equipment[6];
	private Equipment[] storage = new Equipment[STORAGE_SIZE];
	private Equipment[] otherBinds = new Equipment[8];
	private TreeMap<String, ArtifactInstance> artifacts = new TreeMap<String, ArtifactInstance>();
	private int abilitiesEquipped, maxAbilities = 4, maxStorage = 9, coins = 50;
	private HashMap<EquipSlot, HashSet<Integer>> upgradable = new HashMap<EquipSlot, HashSet<Integer>>(),
			upgraded = new HashMap<EquipSlot, HashSet<Integer>>();
	private String instanceData;
	private DropTableSet<Artifact> personalArtifacts;
	private ArrayList<String> boardLines;

	private static final ParticleContainer heal = new ParticleContainer(Particle.VILLAGER_HAPPY).count(50)
			.spread(0.5, 1).speed(0.1).ignoreSettings(true);
	private static final int STORAGE_SIZE = 9;

	public PlayerSessionData(UUID uuid, Session s, ResultSet rs) throws SQLException {
		this.uuid = uuid;
		data = PlayerManager.getPlayerData(uuid);
		this.s = s;

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
		this.instanceData = rs.getString("instanceData");
		setupArtifacts();
		updateBoardLines();
	}

	public PlayerSessionData(UUID uuid, EquipmentClass ec, Session s) {
		this.uuid = uuid;
		data = PlayerManager.getPlayerData(uuid);
		this.s = s;
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
			hotbar[0] = Equipment.get("woodenSword", false);
			hotbar[1] = Equipment.get("empoweredEdge", false);
			abilitiesEquipped = 1;
			break;
		case THIEF:
			hotbar[0] = Equipment.get("woodenSword", false);
			hotbar[1] = Equipment.get("empoweredEdge", false);
			abilitiesEquipped = 1;
			break;
		case ARCHER:
			hotbar[0] = Equipment.get("woodenSword", false);
			hotbar[1] = Equipment.get("empoweredEdge", false);
			abilitiesEquipped = 1;
			break;
		case MAGE:
			hotbar[0] = Equipment.get("woodenSword", false);
			hotbar[1] = Equipment.get("empoweredEdge", false);
			abilitiesEquipped = 1;
			break;
		default:
			break;
		}

		for (int i = 2; i < accessories.length; i++) {
			accessories[i] = Equipment.get("curseOfInexperience", false);
		}

		for (EquipSlot es : EquipSlot.values()) {
			upgradable.put(es, new HashSet<Integer>());
			upgraded.put(es, new HashSet<Integer>());
		}
		upgradable.get(EquipSlot.HOTBAR).add(0);
		upgradable.get(EquipSlot.HOTBAR).add(1);

		setupInventory();
		setupArtifacts();
		updateBoardLines();

		data.getPlayer().setHealthScaled(true);
		data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
		data.initialize(s, this);
	}

	public UUID getUniqueId() {
		return uuid;
	}

	private void setupArtifacts() {
		personalArtifacts = Equipment.copyArtifactsDropSet(ec, EquipmentClass.CLASSLESS);
		for (ArtifactInstance ai : artifacts.values()) {
			if (ai.getArtifact().canStack()) continue;
			personalArtifacts.remove(ai.getArtifact());
		}
	}

	public void setupEditInventory() {
		updateCoinsBar();
		getPlayer().setSaturation(20);
	}

	public HashMap<EquipSlot, HashSet<Integer>> getEquipment(boolean upgraded) {
		return upgraded ? this.upgraded : this.upgradable;
	}

	public void setupInventory() {
		Player p = data.getPlayer();
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setItem(4,
				(CoreInventory.createButton(Material.ENDER_CHEST,
						Component.text("Left/right click to open inventory", NamedTextColor.YELLOW),
						Component.text("You can also swap hands or click anywhere in your inventory."), 200,
						NamedTextColor.GRAY)));

		for (int i = 0; i < storage.length; i++) {
			Equipment eq = storage[i];
			if (eq == null) continue;
			inv.setItem(i, eq.getItem());
		}
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
		upgradable.get(es).remove(slot);
		upgraded.get(es).add(slot);
	}

	public void setEquipment(EquipSlot es, int slot, Equipment eq) {
		Equipment[] slots = getArrayFromEquipSlot(es);
		if (slots[slot] != null) removeEquipment(es, slot);
		if (eq.isUpgraded())
			upgraded.get(es).add(slot);
		else
			upgradable.get(es).add(slot);
		slots[slot] = eq;
		if (eq.getType() == EquipmentType.ABILITY) abilitiesEquipped++;
	}

	public void removeEquipment(EquipSlot es, int slot) {
		Equipment[] slots = getArrayFromEquipSlot(es);
		Equipment eq = slots[slot];
		if (eq.isUpgraded())
			upgraded.get(es).remove(slot);
		else
			upgradable.get(es).remove(slot);
		slots[slot] = null;
		if (eq.getType() == EquipmentType.ABILITY) abilitiesEquipped--;
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
		default:
			Bukkit.getLogger().warning("[NeoRogue] Tried to modify equipment for invalid equip slot " + es);
			break;
		}
		return slots;
	}

	public Equipment[] getStorage() {
		return storage;
	}

	public void setOffhand(Equipment offhand) {
		this.offhand[0] = offhand;
	}

	public Equipment[] getOtherBinds() {
		return otherBinds;
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

	public double getManaRegen() {
		return manaRegen;
	}

	public double getStaminaRegen() {
		return staminaRegen;
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
		}
		inst.getArtifact().onAcquire(this);
	}

	public void giveEquipment(Equipment eq, Component toSelf, Component toOthers) {
		Player p = getPlayer();
		Util.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, false);
		s.broadcastOthers(toOthers.append(eq.getHoverable()).append(Component.text(".")), p);
		toSelf = toSelf.append(eq.getHoverable());

		if (eq instanceof Artifact) {
			Util.msg(p, toSelf.append(Component.text(".")));
			giveArtifact((Artifact) eq);
		}
		else {
			// First try to auto-equip
			boolean success = false;
			if (eq.getType() != EquipmentType.ABILITY || canEquipAbility()) {
				EquipSlot es = null;
				for (EquipSlot eqs : eq.getType().getSlots()) {
					success = tryEquip(eqs, eq);
					if (success) {
						es = eqs;
						break;
					}
				}
				if (success) {
					Util.msg(p, toSelf.append(SharedUtil.color(", it was auto-equipped to " + es.getDisplay() + ".")));
					return;
				}
			}

			HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(eq.getItem());
			Util.msg(p, toSelf.append(Component.text(".")));
			if (!overflow.isEmpty()) {
				for (ItemStack item : overflow.values()) {
					Util.msg(p, SharedUtil.color("<red>Your inventory is full! ").append(eq.getDisplay())
							.append(Component.text(" was dropped on the ground.")));
					p.getWorld().dropItem(p.getLocation(), item);
				}
			}
		}
	}

	public void giveEquipment(Equipment eq) {
		giveEquipment(eq, SharedUtil.color("You received "),
				SharedUtil.color("<yellow>" + data.getDisplay() + "</yellow> received "));
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

	public PlayerSlot getRandomEquipment() {
		return getRandomEquipment(NeoRogue.gen.nextBoolean());
	}

	public PlayerSlot getRandomEquipment(boolean upgraded) {
		HashMap<EquipSlot, HashSet<Integer>> pool = upgraded ? this.upgraded : this.upgradable;
		// First randomly roll equipment slot and try to find a non-empty one
		EquipSlot es = null;
		for (int i = 0; i < 20; i++) {
			EquipSlot temp = EquipSlot.values()[NeoRogue.gen.nextInt(EquipSlot.values().length)];
			if (pool.get(temp).size() > 0) {
				es = temp;
				break;
			}
		}

		// If randomly rolling failed, just manually look through
		if (es == null) {
			for (EquipSlot temp : EquipSlot.values()) {
				if (pool.get(temp).size() > 0) {
					es = temp;
					break;
				}
			}

			// Player has nothing equipped
			if (es == null) {
				return null;
			}
		}

		HashSet<Integer> slots = pool.get(es);
		Iterator<Integer> iter = slots.iterator();
		int slot = iter.next();
		for (int i = 0; i < NeoRogue.gen.nextInt(slots.size()); i++) {
			slot = iter.next();
		}
		return new PlayerSlot(es, slot);
	}

	public boolean saveStorage() {
		Player p = data.getPlayer();
		int max = maxStorage;
		ArrayList<ItemStack> toSave = new ArrayList<ItemStack>(max);
		p.getInventory().setItemInOffHand(null);
		for (ItemStack item : p.getInventory().getContents()) {
			if (item == null) continue;

			toSave.add(item);
		}

		if (toSave.size() > max) {
			Util.displayError(p, "You have too many items in storage! Drop or sell some!");
			return false;
		}

		storage = new Equipment[maxStorage];
		upgradable.get(EquipSlot.STORAGE).clear();
		upgraded.get(EquipSlot.STORAGE).clear();
		int i = 0;
		for (ItemStack item : toSave) {
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			boolean isUpgraded = nbti.getBoolean("isUpgraded");
			Equipment eq = Equipment.get(id, isUpgraded);
			// Hard coded so that the enderchest doesn't give an error
			if (eq == null) {
				if (item.getType() != Material.ENDER_CHEST) {
					String display = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
							? ((TextComponent) item.getItemMeta().displayName()).content()
							: item.getType().name();
					Bukkit.getLogger()
							.warning("[NeoRogue] " + p.getName() + " could not save " + display + " to their storage");
				}
				continue;
			}
			if (eq.isCursed()) {
				Util.displayError(p, "All cursed items must be equipped before continuing!");
				return false;
			}

			if (eq.isUpgraded()) {
				upgraded.get(EquipSlot.STORAGE).add(i);
			}
			else {
				upgradable.get(EquipSlot.STORAGE).add(i);
			}
			storage[i++] = eq;
		}
		return true;
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

	public void increaseAbilityLimit(int amount) {
		this.abilitiesEquipped += amount;
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
		heal.spawn(p);
		Util.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, false);
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
		boardLines.add("§cHP§7: §f" + health);
		boardLines.add("§9Max MP§7: §f" + maxMana);
		boardLines.add("§aMax SP§7: §f" + maxStamina);
		boardLines.add("§eCoins§7: §f" + coins);
		if (s.getParty().size() <= 1) return;
		boardLines.add("§8§m-----");
		for (PlayerSessionData psd : s.getParty().values()) {
			if (psd == this) continue;
			boardLines.add("§e" + psd.getData().getDisplay() + "§7: §f" + Math.round(psd.getHealth()));
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
					.addValue(coins).addString(instanceData);
			stmt.execute(sql.build());
		} catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save player session data for " + uuid + " hosted by "
					+ host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
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
}
