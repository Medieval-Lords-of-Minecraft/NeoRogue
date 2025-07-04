package me.neoblade298.neorogue.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.listeners.InventoryListener;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.mechanics.PotionProjectileInstance;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import me.neoblade298.neorogue.player.SessionSnapshot;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.player.inventory.PlayerSessionSpectateInventory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SessionManager implements Listener {
	private static HashMap<UUID, Session> sessions = new HashMap<UUID, Session>();
	private static HashMap<Plot, Session> sessionPlots = new HashMap<Plot, Session>();

	public static Session createSession(Player p, String name, int saveSlot) {
		Plot plot = findPlot();
		Session s = new Session(p, plot, name, saveSlot);
		sessions.put(p.getUniqueId(), s);
		sessionPlots.put(plot, s);

		Util.msg(p, Component.text("Successfully created a lobby!", NamedTextColor.GRAY));
		return s;
	}

	public static Session loadSession(Player p, int saveSlot) {
		PlayerData pd = PlayerManager.getPlayerData(p.getUniqueId());
		SessionSnapshot ss = pd.getSnapshot(saveSlot);
		for (Entry<UUID, String> ent : ss.getPartyIds().entrySet()) {
			if (Bukkit.getPlayer(ent.getKey()) == null) {
				Util.displayError(p, "Cannot load this save as " + ent.getValue() + " is not online!");
				return null;
			}
		}

		Plot plot = findPlot();
		Session s = new Session(p, plot, saveSlot);
		sessions.put(p.getUniqueId(), s);
		sessionPlots.put(plot, s);
		return s;
	}

	private static Plot findPlot() {
		for (int x = 0; x < 6; x++) {
			for (int z = 0; z < 100; z++) {
				Plot plot = new Plot(x, z);
				if (!sessionPlots.containsKey(plot)) {
					return plot;
				}
			}
		}

		return null;
	}

	public static void addToSession(UUID uuid, Session s) {
		sessions.put(uuid, s);
	}

	public static void removeFromSession(UUID uuid) {
		sessions.remove(uuid);
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			p.teleport(NeoRogue.spawn);
	}

	public static void removeSession(Session s) {
		if (s.getInstance() instanceof LobbyInstance) {
			LobbyInstance lob = (LobbyInstance) s.getInstance();
			for (UUID uuid : lob.getPlayers().keySet()) {
				removeFromSession(uuid);
			}
		} else {
			for (UUID uuid : s.getParty().keySet()) {
				removeFromSession(uuid);
			}
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			removeFromSession(uuid);
		}
		sessionPlots.remove(s.getPlot());
	}

	public static Session getSession(Plot p) {
		return sessionPlots.get(p);
	}

	public static Session getSession(UUID uuid) {
		return sessions.get(uuid);
	}

	public static Session getSession(Player p) {
		return sessions.get(p.getUniqueId());
	}

	public static Collection<Session> getSessions() {
		return sessionPlots.values();
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		Player p = (Player) e.getWhoClicked();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);
		e.setCancelled(true);
		if (InventoryListener.hasOpenCoreInventory(p))
			return;
		if (s.getInstance() instanceof EditInventoryInstance
				&& e.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
			new PlayerSessionInventory(s.getData(uuid)).handleInventoryDrag(e); // Register core player inventory when
																				// inv is opened
		} else if (s.getInstance() instanceof FightInstance) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);
		if (e.getClickedInventory() == null)
			return;
		if (e.getClickedInventory().getType() == InventoryType.CRAFTING) {
			e.setCancelled(true);
			return;
		}
		if (InventoryListener.hasOpenCoreInventory(p))
			return;
		if (s.getInstance() instanceof EditInventoryInstance
				&& e.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
			new PlayerSessionInventory(s.getData(uuid)).handleInventoryClick(e);
		} else if (s.getInstance() instanceof FightInstance) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBuild(BlockPlaceEvent e) {
		if (sessions.containsKey(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (sessions.containsKey(uuid)) {
			Session s = sessions.get(uuid);
			e.setCancelled(true);
			if (s.isSpectator(uuid))
				return;

			if (s.getInstance() instanceof FightInstance) {
				FightInstance.handleOffhandSwap(e);
			} else if (s.getInstance() instanceof EditInventoryInstance
					&& !(s.getInstance() instanceof NodeSelectInstance)) {
				EditInventoryInstance.handleSwapHand(e);
				e.setCancelled(false);
			}
		}
	}

	@EventHandler
	public void onLaunchProjectile(ProjectileLaunchEvent e) {
		if (!(e.getEntity().getShooter() instanceof Player))
			return;
		Player p = (Player) e.getEntity().getShooter();

		if (!sessions.containsKey(p.getUniqueId()))
			return;
		e.setCancelled(true);
		if (sessions.get(p.getUniqueId()).isSpectator(p.getUniqueId()))
			return;
		if (e.getEntity() instanceof Trident) {
			FightInstance.trigger(p, Trigger.THROW_TRIDENT, e);
		} else if (e.getEntity() instanceof Arrow) {
			ItemStack item = ((Arrow) e.getEntity()).getItemStack();
			if (item.getType() != Material.ARROW)
				p.getInventory().addItem(item);
			FightInstance.trigger(p, Trigger.VANILLA_PROJECTILE, e);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		e.setCancelled(true);
		if (sessions.get(p.getUniqueId()).isSpectator(uuid))
			return;
		FightInstance.handleDropItem(e);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if (!sessions.containsKey(e.getPlayer().getUniqueId()))
			return;
		Player p = e.getPlayer();
		Session s = sessions.get(p.getUniqueId());
		if (s.isSpectator(p.getUniqueId())) {
			p.spigot().respawn();
			p.teleport(s.getInstance().getSpawn());
			return;
		}
		FightInstance.handleDeath(e);
	}

	@EventHandler
	public void onPotionSplash(PotionSplashEvent e) {
		if (e.getEntity().hasMetadata("uuid")) {
			UUID uuid = (UUID) e.getEntity().getMetadata("uuid").get(0).value();
			PotionProjectileInstance inst = PotionProjectileInstance.get(uuid);
			if (inst == null)
				return;
			inst.callback(e.getPotion().getLocation(), e.getAffectedEntities());
			PotionProjectileInstance.remove(uuid);
		}
	}

	// Stops players from being able to vanilla damage mobs
	@EventHandler
	public void onPlayerAttack(PrePlayerAttackEntityEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		FightInstance.handlePlayerAttack(e);
	}

	// Stops mobs from being able to melee players, but allows custom damage
	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		Player p = null;
		boolean playerDamager = false;

		// Our plugin uses magic damage type
		if (e.getDamageSource().getDamageType() == DamageType.MAGIC)
			return;
		if (sessions.containsKey(e.getDamager().getUniqueId())) {
			p = (Player) e.getDamager();
			playerDamager = true;
		} else if (sessions.containsKey(e.getEntity().getUniqueId())) {
			p = (Player) e.getEntity();
		} else {
			return;
		}

		// Either damager or target are a player at this point
		UUID uuid = p.getUniqueId();
		Session s = sessions.get(uuid);
		if (s.isSpectator(e.getEntity().getUniqueId()) || s.isSpectator(e.getDamager().getUniqueId())) {
			e.setCancelled(true);
			return;
		}
		if (!(s.getInstance() instanceof FightInstance))
			return;
		if (!playerDamager) {
			// Don't cancel damage, but set it to 0 so the ~onAttack mythicmob trigger still
			// goes
			e.setDamage(0);
			return;
		}

	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e instanceof EntityDamageByEntityEvent)
			return;
		UUID uuid = e.getEntity().getUniqueId();
		if (e.getCause() == DamageCause.POISON || e.getCause() == DamageCause.WITHER
				|| e.getCause() == DamageCause.STARVATION || e.getCause() == DamageCause.DROWNING) {
			e.setCancelled(true);
			return;
		}
		if (e.getEntity().getType() != EntityType.PLAYER)
			return;
		if (!sessions.containsKey(uuid))
			return;
		if (!(sessions.get(uuid).getInstance() instanceof FightInstance) && e.getCause() == DamageCause.FALL) {
			e.setCancelled(true);
		}
		FightInstance.handleEnvironmentDamage(e);
	}

	@EventHandler
	public void onHotbarSwap(PlayerItemHeldEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);
		if (s.isSpectator(uuid))
			return;

		if (s.getInstance() instanceof FightInstance) {
			FightInstance.handleHotbarSwap(e);
		} else if (s.getInstance() instanceof EditInventoryInstance) {
			EditInventoryInstance.handleHotbarSwap(e);
		}
	}

	@EventHandler
	public void onLoadCrossbow(EntityLoadCrossbowEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		UUID uuid = e.getEntity().getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		e.setConsumeItem(false);
	}

	@EventHandler(ignoreCancelled = false)
	public void onInteractEntity(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		if (e.getHand() != EquipmentSlot.OFF_HAND)
			return;
		Session s = sessions.get(uuid);
		if (s.getInstance() instanceof EditInventoryInstance && e.getRightClicked() instanceof Player) {
			Player viewed = (Player) e.getRightClicked();
			if (s.getParty().containsKey(viewed.getUniqueId())) {
				new PlayerSessionSpectateInventory(s.getParty().get(viewed.getUniqueId()), p);
			}
			return;
		}

		if (s.isSpectator(uuid)) {
			e.setCancelled(true);
			if (e.getRightClicked() instanceof Player) {
				Player viewed = (Player) e.getRightClicked();
				if (s.getParty().containsKey(viewed.getUniqueId())) {
					new PlayerSessionSpectateInventory(s.getParty().get(viewed.getUniqueId()), p);
				}
			}
			return;
		}

		FightInstance.handleRightClickEntity(e);
	}

	@EventHandler
	public void onPotion(EntityPotionEffectEvent e) {
		FightInstance.handlePotionEffect(e);
	}

	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onToggleCrouch(PlayerToggleSneakEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);
		if (!(s.getInstance() instanceof FightInstance))
			return;
		((FightInstance) s.getInstance()).handleToggleCrouchEvent(e);
	}

	@EventHandler
	public void onToggleSprint(PlayerToggleSprintEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);
		if (!(s.getInstance() instanceof FightInstance))
			return;
		((FightInstance) s.getInstance()).handleToggleSprintEvent(e);
	}

	@EventHandler(ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);

		PlayerInventory inv = p.getInventory();
		ItemStack hand = e.getItem();
		if (hand != null) {
			// Make sure you can't equip armor with right click
			EquipmentSlot slot = canEquip(hand);
			if (slot != null && inv.getItem(slot).getType().isAir()) {
				e.setCancelled(true);
			}

			// Prevent using eye of ender
			if (hand.getType() == Material.ENDER_EYE) {
				e.setCancelled(true);
			}
		}

		// Disable clicking on crafting tables, anvils, furnaces, etc
		Block b = e.getClickedBlock();
		if (b != null) {
			Material mat = b.getType();
			if (mat == Material.DECORATED_POT || mat == Material.CRAFTING_TABLE || mat == Material.ANVIL
					|| mat == Material.ENCHANTING_TABLE || mat == Material.BREWING_STAND || mat == Material.FURNACE
					|| mat == Material.SMITHING_TABLE || mat == Material.GRINDSTONE || mat == Material.CARTOGRAPHY_TABLE
					|| mat == Material.LOOM || mat == Material.JIGSAW) {
				e.setCancelled(true);
			}
		}

		if (s.isSpectator(uuid)) {
			s.getInstance().handleSpectatorInteractEvent(e);
			return;
		}
		s.getInstance().handleInteractEvent(e);
	}

	@EventHandler(ignoreCancelled = false)
	public void onInteractArmorStand(PlayerInteractAtEntityEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!sessions.containsKey(uuid))
			return;
		Session s = sessions.get(uuid);
		if (s.isSpectator(uuid)) {
			e.setCancelled(true);
			return;
		}
		if (!(s.getInstance() instanceof FightInstance))
			return;

		e.setCancelled(true);
		FightInstance.handleClickArmorStand(p, e.getRightClicked());
	}

	private static EquipmentSlot canEquip(ItemStack item) {
		String name = item.getType().name();
		// For sake of efficiency, this is leaving out elytra, jackolanterns, turtle
		// shell, and mob heads
		if (name.endsWith("HELMET"))
			return EquipmentSlot.HEAD;
		else if (name.endsWith("CHESTPLATE"))
			return EquipmentSlot.CHEST;
		else if (name.endsWith("LEGGINGS"))
			return EquipmentSlot.LEGS;
		else if (name.endsWith("BOOTS"))
			return EquipmentSlot.FEET;
		return null;
	}

	@EventHandler
	public void onMythicDespawn(MythicMobDespawnEvent e) {
		FightInstance.handleMythicDespawn(e);
	}

	@EventHandler
	public void onMythicDeath(MythicMobDeathEvent e) {
		FightInstance.handleMythicDeath(e);
	}

	@EventHandler
	public void onMythicSpawn(MythicMobSpawnEvent e) {
		Entity ent = e.getEntity();
		UUID uuid = ent.getUniqueId();
		if (ent instanceof LivingEntity) {
			MythicMob mythicMob = e.getMobType();
			Mob mob = Mob.get(mythicMob.getInternalName());
			if (mob == null)
				return;
			FightData fd = new FightData((LivingEntity) ent, NeoRogue.mythicApi.getMythicMobInstance(ent), mob,
					(MapSpawnerInstance) null);
			FightInstance.putFightData(uuid, fd);
			if (e.getSpawnReason() == SpawnReason.SUMMON || e.getSpawnReason() == SpawnReason.COMMAND) {
				if (fd.getInstance() == null)
					return;
				FightInstance.scaleMob(fd.getInstance().getSession(), mob, mythicMob, e.getMob());
				fd.getInstance().addSpawnCounter(mob.getSpawnValue());
			}
		}
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		if (!sessions.containsKey(e.getEntity().getUniqueId()))
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onHungerRegen(EntityRegainHealthEvent e) {
		if (!sessions.containsKey(e.getEntity().getUniqueId()))
			return;
		if (e.getRegainReason() == RegainReason.SATIATED) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleLeave(e.getPlayer());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		handleLeave(e.getPlayer());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		p.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)
				.removeModifier(NamespacedKey.fromString("jump", NeoRogue.inst()));
		if (sessions.containsKey(p.getUniqueId())) {
			Session s = sessions.get(p.getUniqueId());
			s.getData(p.getUniqueId()).syncHealth();
			s.getInstance().handlePlayerRejoin(p);
		} else {
			p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
			p.teleport(NeoRogue.spawn);
		}
	}

	private void handleLeave(Player p) {
		UUID uuid = p.getUniqueId();
		if (sessions.containsKey(uuid)) {
			Session s = sessions.get(uuid);

			if (s.isSpectator(uuid)) {
				s.removeSpectator(p);
				return;
			}

			if (s.getInstance() instanceof LobbyInstance) {
				LobbyInstance li = (LobbyInstance) s.getInstance();
				li.leavePlayer(p);
			} else {
				// Must be <= 1 since the last player isn't offline until after event
				if (s.getOnlinePlayers().size() <= 1) {
					s.broadcast("Everyone logged off, so the game has ended!");
					endSession(s);
					return;
				}
				s.getInstance().handlePlayerLeave(p);
			}
		}
	}

	public static void resetPlayer(Player p) {
		if (p == null)
			return;
		p.getInventory().clear();
		p.setMaximumNoDamageTicks(20);
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		p.setHealth(20);
		p.setInvulnerable(false);
		p.setInvisible(false);
		p.setAllowFlight(false);
	}

	public static void endSession(Session s) {
		s.cleanup();
		removeSession(s);

		new BukkitRunnable() {
			public void run() {
				try (Connection con = NeoCore.getConnection("NeoRogue-SessionManager");
						Statement insert = con.createStatement();
						Statement delete = con.createStatement()) {
					s.save(insert, delete);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoRogue.inst());
	}
}
