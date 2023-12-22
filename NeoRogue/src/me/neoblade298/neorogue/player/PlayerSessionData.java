package me.neoblade298.neorogue.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
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
import me.neoblade298.neorogue.equipment.*;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerSessionData {
	private PlayerData data;
	private Session s;
	private PlayerClass pc;
	private double maxHealth, maxMana, maxStamina, health, manaRegen, staminaRegen;
	private HotbarCompatible[] hotbar = new HotbarCompatible[9];
	private Armor[] armors = new Armor[3];
	private Offhand offhand;
	private Accessory[] accessories = new Accessory[6];
	private TreeSet<ArtifactInstance> artifacts = new TreeSet<ArtifactInstance>();
	private ArrayList<Equipment> storage = new ArrayList<Equipment>(9);
	private Usable[] otherBinds = new Usable[8];
	private int abilitiesEquipped = 0, maxAbilities = 2, maxStorage = 9, coins = 0;
	private String instanceData;
	private boolean isDead;
	
	private static final ParticleContainer heal = new ParticleContainer(Particle.VILLAGER_HAPPY).count(50).spread(0.5, 1).speed(0.1).ignoreSettings(true);
	
	public PlayerSessionData(UUID uuid, Session s, ResultSet rs) throws SQLException {
		data = PlayerManager.getPlayerData(uuid);
		this.s = s;
		
		this.pc = PlayerClass.valueOf(rs.getString("playerClass"));
		this.maxHealth = rs.getDouble("maxHealth");
		this.maxMana = rs.getDouble("maxMana");
		this.maxStamina = rs.getDouble("maxStamina");
		this.health = rs.getDouble("health");
		this.manaRegen = rs.getDouble("manaRegen");
		this.staminaRegen = rs.getDouble("staminaRegen");
		this.hotbar = Equipment.deserializeHotbar(rs.getString("hotbar"));
		this.armors = Equipment.deserializeArmor(rs.getString("armors"));
		this.offhand = (Offhand) Equipment.deserialize(rs.getString("offhand"));
		this.accessories = Equipment.deserializeAccessories(rs.getString("accessories"));
		this.storage = Equipment.deserializeAsArrayList(rs.getString("storage"));
		this.otherBinds = Equipment.deserializeUsables(rs.getString("otherBinds"));
		this.artifacts = ArtifactInstance.deserializeSet(rs.getString("artifacts"));
		this.maxAbilities = rs.getInt("maxAbilities");
		this.maxStorage = rs.getInt("maxStorage");
		this.coins = rs.getInt("coins");
		this.instanceData = rs.getString("instanceData");
	}
	
	public PlayerSessionData(UUID uuid, PlayerClass pc, Session s) {
		data = PlayerManager.getPlayerData(uuid);
		this.s = s;
		health = 100;
		maxHealth = 100;
		maxMana = 100;
		maxStamina = 100;
		manaRegen = 2;
		staminaRegen = 2;
		health = maxHealth;
		this.pc = pc;
		
		// Need to give player a weapon at the start
		switch (this.pc) {
		case WARRIOR: hotbar[0] = (HotbarCompatible) Equipment.get("woodenSword", false);
		hotbar[1] = (HotbarCompatible) Equipment.get("empoweredEdge", false);
		hotbar[2] = (HotbarCompatible) Equipment.get("stoneHammer", false);
		break;
		case THIEF: hotbar[0] = (HotbarCompatible) Equipment.get("woodenSword", false);
		hotbar[1] = (HotbarCompatible) Equipment.get("empoweredEdge", false);
		break;
		case ARCHER: hotbar[0] = (HotbarCompatible) Equipment.get("woodenSword", false);
		hotbar[1] = (HotbarCompatible) Equipment.get("empoweredEdge", false);
		break;
		case MAGE: hotbar[0] = (HotbarCompatible) Equipment.get("woodenSword", false);
		hotbar[1] = (HotbarCompatible) Equipment.get("empoweredEdge", false);
		break;
		}
		
		setupInventory();
		
		data.getPlayer().setHealthScaled(true);
		data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
		data.initialize(s, this);
	}
	
	public void setupInventory() {
		Player p = data.getPlayer();
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setItemInOffHand(CoreInventory.createButton(Material.ENCHANTED_BOOK, Component.text("Storage Book", NamedTextColor.YELLOW),
				Component.text("Swap hands or click anywhere in your inventory to open your storage."), 200, NamedTextColor.GRAY));
		
		for (int i = 0; i < storage.size(); i++) {
			Equipment eq = storage.get(i);
			if (eq == null) continue;
			inv.setItem(i, eq.getItem());
		}
 	}
	
	public void cleanup() {
		if (isDead) {
			Player p = getPlayer();
			p.setInvisible(false);
			p.setInvulnerable(false);
		}
	}
	
	public Player getPlayer() {
		return data.getPlayer();
	}
	
	public Armor[] getArmor() {
		return armors;
	}
	
	public HotbarCompatible[] getHotbar() {
		return hotbar;
	}
	
	public Accessory[] getAccessories() {
		return accessories;
	}
	
	public ArrayList<Equipment> getStorage() {
		return storage;
	}
	
	public Offhand getOffhand() {
		return offhand;
	}
	
	public void setOffhand(Offhand offhand) {
		this.offhand = offhand;
	}
	
	public Usable[] getOtherBinds() {
		return otherBinds;
	}
	
	public Usable getOtherBind(KeyBind bind) {
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

	public Armor[] getArmors() {
		return armors;
	}

	public TreeSet<ArtifactInstance> getArtifacts() {
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
		ArtifactInstance inst = new ArtifactInstance(artifact);
		artifacts.add(inst);
		inst.getArtifact().onAcquire(this);
	}
	
	public void giveEquipment(Equipment eq) {
		Player p = getPlayer();
		Util.msg(p, SharedUtil.color("You received ").append(eq.getDisplay()));
		Util.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, false);
		
		if (eq instanceof Artifact) {
			giveArtifact((Artifact) eq);
		}
		else {
			HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(eq.getItem());
			if (!overflow.isEmpty()) {
				for (ItemStack item : overflow.values()) {
					Util.msg(p, SharedUtil.color("<red>Your inventory is full! ").append(eq.getDisplay()).append(Component.text(" was dropped on the ground.")));
					p.getWorld().dropItem(p.getLocation(), item);
				}
			}
		}
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
			Util.msg(p, "You have too many items in storage! Drop or sell some!");
			return false;
		}
		
		storage.clear();
		
		for (ItemStack item : toSave) {
			NBTItem nbti = new NBTItem(item);
			String id = nbti.getString("equipId");
			boolean isUpgraded = nbti.getBoolean("isUpgraded");
			Equipment eq = Equipment.get(id, isUpgraded);
			if (eq == null) {
				String display = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
						((TextComponent) item.getItemMeta().displayName()).content() : item.getType().name();
				Bukkit.getLogger().warning("[NeoRogue] " + p.getName() + " could not save " + display + " to their storage");
				continue;
			}
			else {
				storage.add(eq);
			}
		}
		return true;
	}
	
	public boolean hasCoins(int amount) {
		return coins >= amount;
	}
	
	public void addCoins(int amount) {
		coins += amount;
		char symbol = amount > 0 ? '+' : '-';
		Util.msg(getPlayer(), "<yellow>" + symbol + amount + " coins </yellow>(<gold>" + coins + "</gold>)");
	}
	
	public int getCoins() {
		return coins;
	}
	
	public PlayerClass getPlayerClass() {
		return pc;
	}
	
	public void addMaxHealth(int amount) {
		this.maxHealth += amount;
		this.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
	}
	
	public void addMaxStamina(int amount) {
		this.maxStamina += amount;
	}
	
	public void addMaxMana(int amount) {
		this.maxMana += amount;
	}
	public Session getSession() {
		return s;
	}
	
	public double getHealth() {
		return health;
	}
	
	public void updateHealth() {
		health = Math.round(getPlayer().getHealth());
	}
	
	public void syncHealth() {
		getPlayer().setHealth(this.health);
	}
	
	public void setHealth(double health) {
		this.health = health;
		getPlayer().setHealth(Math.min(health, maxHealth));
	}
	
	public void healPercent(double percent) {
		Player p = getPlayer();
		setHealth(this.health + (percent * maxHealth));
		heal.spawn(p);
		Util.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, false);
	}
	
	// Used when the player dies and on cleanup (to revive them)
	public void setDeath(boolean isDead) {
		Player p = getPlayer();
		this.isDead = isDead;
		if (isDead) {
			this.health = 1;
			p.setInvulnerable(true);
			p.setInvisible(true);
			p.getInventory().clear();
		}
		else {
			p.setInvulnerable(false);
			p.setInvisible(false);
		}
	}
	
	public void setInstanceData(String str) {
		this.instanceData = str;
	}
	
	public String getInstanceData() {
		return instanceData;
	}
	
	public boolean isDead() {
		return isDead;
	}
	
	public void save(Statement stmt) {
		UUID host = s.getHost();
		String uuid = data.getPlayer().getUniqueId().toString();
		int saveSlot = s.getSaveSlot();
		try {
			SQLInsertBuilder sql = new SQLInsertBuilder(SQLAction.REPLACE, "neorogue_playersessiondata")
					.addString(host.toString()).addValue(saveSlot).addString(uuid)
					.addString(((TextComponent) data.getPlayer().displayName()).content())
					.addString(pc.name()).addValue(maxHealth).addValue(maxMana)
					.addValue(maxStamina).addValue(health).addValue(manaRegen).addValue(staminaRegen)
					.addString(Equipment.serialize(hotbar)).addString(Equipment.serialize(armors))
					.addString(offhand != null ? offhand.serialize() : "").addString(Equipment.serialize(accessories))
					.addString(Equipment.serialize(storage)).addString(Equipment.serialize(otherBinds))
					.addString(ArtifactInstance.serialize(artifacts)).addValue(maxAbilities).addValue(maxStorage)
					.addValue(coins).addString(instanceData);
			stmt.execute(sql.build());
		}
		catch (SQLException ex) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to save player session data for " + uuid + " hosted by " + host + " to slot " + saveSlot);
			ex.printStackTrace();
		}
	}
}
