package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.LoseInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.fight.Mob.MobType;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageMultipleEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;
import net.kyori.adventure.text.Component;

public abstract class FightInstance extends Instance {
	private static HashMap<UUID, PlayerFightData> userData = new HashMap<UUID, PlayerFightData>();
	private static HashMap<UUID, Barrier> userBarriers = new HashMap<UUID, Barrier>();
	private static HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	private static HashMap<UUID, BukkitTask> blockTasks = new HashMap<UUID, BukkitTask>();
	private static HashSet<UUID> indicators = new HashSet<UUID>();
	private static final int KILLS_TO_SCALE = 5; // number of mobs to kill before increasing total mobs by 1
	
	protected HashSet<UUID> toTick = new HashSet<UUID>();
	protected LinkedList<Corpse> corpses = new LinkedList<Corpse>();
	protected HashMap<Player, Corpse> revivers = new HashMap<Player, Corpse>();
	protected HashSet<UUID> party = new HashSet<UUID>();
	protected Map map;
	protected ArrayList<MapSpawnerInstance> spawners = new ArrayList<MapSpawnerInstance>(),
			unlimitedSpawners = new ArrayList<MapSpawnerInstance>(),
			initialSpawns = new ArrayList<MapSpawnerInstance>();
	private HashMap<String, Location> mythicLocations = new HashMap<String, Location>();
	protected HashMap<UUID, Barrier> enemyBarriers = new HashMap<UUID, Barrier>();
	protected LinkedList<BukkitTask> tasks = new LinkedList<BukkitTask>();
	protected LinkedList<BukkitRunnable> cleanupTasks = new LinkedList<BukkitRunnable>();
	protected LinkedList<FightRunnable> initialTasks = new LinkedList<FightRunnable>();
	protected double spawnCounter; // When above 1, a mob spawns
	protected double totalKillValue; // Keeps track of total mob spawns, to handle scaling of spawning
	private long startTime;
	private ArrayList<String> spectatorLines;
	protected ArrayList<BossBar> bars = new ArrayList<BossBar>();
	
	private static final Circle reviveCircle = new Circle(5);
	private static final ParticleContainer reviveCirclePart = new ParticleContainer(Particle.END_ROD).count(1);
	private static final ParticleContainer revivePart = new ParticleContainer(Particle.FIREWORK)
			.forceVisible(Audience.ALL).count(50).spread(2, 2).speed(0.1);
	private static final SoundContainer[] reviveSounds = new SoundContainer[] {
			new SoundContainer(Sound.BLOCK_NOTE_BLOCK_BELL, 1F),
			new SoundContainer(Sound.BLOCK_NOTE_BLOCK_BELL, 1.059463F),
			new SoundContainer(Sound.BLOCK_NOTE_BLOCK_BELL, 1.122462F),
			new SoundContainer(Sound.BLOCK_NOTE_BLOCK_BELL, 1.189207F),
			new SoundContainer(Sound.BLOCK_NOTE_BLOCK_BELL, 1.259921F) };
	
	public FightInstance(Session s, Set<UUID> players) {
		super(s, new PlayerFlags(PlayerFlag.SCALE_HEALTH, PlayerFlag.ZERO_DAMAGE_TICKS, PlayerFlag.ALLOW_FLIGHT_FALL));
		party.addAll(players);
	}
	
	public void addInitialTask(FightRunnable runnable) {
		initialTasks.add(runnable);
	}
	
	public static HashMap<UUID, Barrier> getUserBarriers() {
		return userBarriers;
	}
	
	public void instantiate() {
		map.instantiate(this, s.getXOff(), s.getZOff());
	}
	
	public Map getMap() {
		return map;
	}
	
	public HashSet<UUID> getParty() {
		return party;
	}

	public void updateSpectatorLines() {
		spectatorLines = new ArrayList<String>(9);
		for (Player p : s.getOnlinePlayers()) {
			spectatorLines.add(createHealthBar(userData.get(p.getUniqueId())));
		}
	}

	public ArrayList<String> getSpectatorLines() {
		return spectatorLines;
	}

	public abstract void addSpectator(Player p);
	
	public abstract void removeSpectator(Player p);

	private static String createHealthBar(PlayerFightData pfd) {
		if (pfd != null && pfd.isDead()) {
			return "&c&m" + pfd.getSessionData().getData().getDisplay();
		}
		if (pfd.getPlayer() == null) {
			return "&7&m" + pfd.getSessionData().getData().getDisplay();
		}
		Player p = pfd.getPlayer();
		double percenthp = p.getHealth() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		percenthp *= 100;
		int php = (int) percenthp;
		String color = "§a";
		if (php < 50 && php >= 25) {
			color = "§e";
		} else if (php < 25) {
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
	
	public static void handleDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Location prev = p.getLocation();
		
		// Remove the player's abilities from the fight
		UUID pu = p.getUniqueId();
		if (!userData.containsKey(pu))
			return;
		PlayerFightData data = userData.get(pu);
		FightInstance fi = data.getInstance();
		Session s = fi.getSession();
		ItemStack[] inv = p.getInventory().getContents();
		int[] cooldowns = new int[9]; // Get material cooldowns so they persist in death
		for (int i = 0; i < 9; i++) {
			if (inv[i] == null) continue;
			cooldowns[i] = p.getCooldown(inv[i].getType());
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				s.broadcast("<red>" + p.getName() + " died!");
				data.setDeath(true);
				data.getStats().addDeath();
				
				// If that's the last player alive, send them to lose instance
				boolean lose = true;
				for (UUID uuid : data.getInstance().getParty()) {
					PlayerFightData fdata = userData.get(uuid);
					if (fdata != null && fdata.isActive()) {
						lose = false;
						break;
					}
				}
				
				// End game as a loss
				if (lose) {
					p.spigot().respawn();
					Session sess = data.getInstance().getSession();
					new BukkitRunnable() {
						@Override
						public void run() {
							sess.setInstance(new LoseInstance(sess));
						}
					}.runTask(NeoRogue.inst());
				} else {
					if (p != null) {
						fi.corpses.add(new Corpse(data));
						// Have a wait time so that mythicmobs can handle deaths
						new BukkitRunnable() {
							@Override
							public void run() {
								p.spigot().respawn();
								p.teleport(prev);
								p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
								p.getInventory().setContents(inv);
								// Set cooldowns so they persist through death
								for (int i = 0; i < 9; i++) {
									if (inv[i] == null) continue;
									p.setCooldown(inv[i].getType(), cooldowns[i]);
								}
							}
						}.runTask(NeoRogue.inst());
					}
				}
			}
		}.runTaskLater(NeoRogue.inst(), 5L);
	}

	public void handlePlayerLeaveParty(Player p) {
		for (BossBar bar : bars) {
			bar.removePlayer(p);
		}
		userData.get(p.getUniqueId()).cleanup();
		checkInstanceDead(p);
	}

	private void checkInstanceDead(Player leaver) {
		boolean lose = true;
		for (UUID uuid : this.getParty()) {
			if (leaver.getUniqueId().equals(uuid))
				continue;
			PlayerFightData fdata = userData.get(uuid);
			if (fdata != null && fdata.isActive()) {
				lose = false;
				break;
			}
		}

		if (lose) {
			s.setInstance(new LoseInstance(s));
		}
	}

	public void createIndicator(Component txt, Location src) {
		TextDisplay td = (TextDisplay) src.getWorld().spawnEntity(src, EntityType.TEXT_DISPLAY);
		td.text(txt);
		Transformation trans = td.getTransformation();
		trans.getScale().set(2);
		td.setBillboard(Billboard.CENTER);
		td.setTransformation(trans);
		td.setTeleportDuration(40);
		new BukkitRunnable() {
			@Override
			public void run() {
				td.teleport(td.getLocation().add(0, 2, 0));
			}
		}.runTaskLater(NeoRogue.inst(), 2);

		new BukkitRunnable() {
			@Override
			public void run() {
				td.remove();
				indicators.remove(td.getUniqueId());
			}
		}.runTaskLater(NeoRogue.inst(), 40L);

	}

	@Override
	public void handlePlayerLeave(Player p) {
		super.handlePlayerLeave(p);
		p.removePotionEffect(PotionEffectType.ABSORPTION);
		checkInstanceDead(p);
	}

	@Override
	public void handlePlayerRejoin(Player p) {
		super.handlePlayerRejoin(p);
		PlayerFightData pdata = getUserData(p.getUniqueId());
		if (pdata == null) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to get player fight data on rejoin for " + p.getName());
			return;
		}

		if (pdata.isDead()) {
			p.setInvulnerable(true);
			p.setInvisible(true);
		}
	}
	
	public static void handlePlayerAttack(PrePlayerAttackEntityEvent e) {
		Player p = e.getPlayer();
		e.setCancelled(true);
		PlayerFightData data = userData.get(p.getUniqueId());
		trigger(p, Trigger.LEFT_CLICK, null);
		if (!(e.getAttacked() instanceof LivingEntity))
			return;
		if (e.getAttacked() instanceof Player)
			return;
		if (!e.getAttacked().isValid())
			return;
		
		// Revive logic
		if (e.getAttacked().getType() == EntityType.ARMOR_STAND) {
			FightInstance.handleClickArmorStand(p, e.getAttacked());
			return;
		}
		
		if (!data.canBasicAttack())
			return;
		trigger(p, Trigger.LEFT_CLICK_HIT, new LeftClickHitEvent((LivingEntity) e.getAttacked()));
	}

	public static void handleEnvironmentDamage(EntityDamageEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		Player p = (Player) e.getEntity();
		if (e.getCause() == DamageCause.FALL) {
			e.setCancelled(true);
			trigger(p, Trigger.FALL_DAMAGE, e);
			return;
		}
	}
	
	public static void handleWin() {
		for (PlayerFightData data : userData.values()) {
			trigger(data.getPlayer(), Trigger.WIN_FIGHT, new Object[0]);
		}
	}
	
	public static void handleHotbarSwap(PlayerItemHeldEvent e) {
		// Only cancel swap if something is bound to the trigger
		Player p = e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return;
		e.setCancelled(data.hasTriggerAction(Trigger.getFromHotbarSlot(e.getNewSlot())));
		trigger(e.getPlayer(), Trigger.getFromHotbarSlot(e.getNewSlot()), null);
	}
	
	public static void handleOffhandSwap(PlayerSwapHandItemsEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		// Offhand is always cancelled
		trigger(p, p.isSneaking() ? Trigger.SHIFT_SWAP : Trigger.SWAP, null);
	}
	
	public static void handleDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		// Drop item is always cancelled
		trigger(p, p.isSneaking() ? Trigger.SHIFT_DROP : Trigger.DROP, null);
	}
	
	@Override
	public void handleInteractEvent(PlayerInteractEvent e) {
		Action a = e.getAction();
		if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
			handleLeftClick(e);
		} else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			handleRightClickGeneral(e);
		}
	}

	public void handleFlightToggle(PlayerToggleFlightEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		trigger(p, Trigger.TOGGLE_FLIGHT, e);
		p.setAllowFlight(false);
		FightInstance fight = this;
		new BukkitRunnable() {
			public void run() {
				if (p != null && s.getInstance() == fight) {
				p.setAllowFlight(true);
				}
			}
		}.runTaskLater(NeoRogue.inst(), 10);
	}

	public void handleToggleCrouchEvent(PlayerToggleSneakEvent e) {
		trigger(e.getPlayer(), Trigger.TOGGLE_CROUCH, e);
	}

	public void handleToggleSprintEvent(PlayerToggleSprintEvent e) {
		trigger(e.getPlayer(), Trigger.TOGGLE_SPRINT, e);
	}

	public static void handleClickArmorStand(Player p, Entity armorStand) {
		PlayerFightData data = userData.get(p.getUniqueId());
		FightInstance fi = data.getInstance();
		if (data == null || data.isDead())
			return;
		for (Corpse c : fi.corpses) {
			if (c.hitCorpse(armorStand)) {
				if (fi.revivers.containsKey(p)) {
					Util.displayError(p, "You're already reviving someone!");
					return;
				}

				if (c.reviver != null) {
					Util.displayError(p, "Someone is already reviving this player!");
					return;
				}
				fi.startRevive(data, c);
			}
		}
	}
	
	public static void handlePotionEffect(EntityPotionEffectEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		Player p = (Player) e.getEntity();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return;
		trigger(p, Trigger.RECEIVE_POTION, e);
	}
	
	public static void handleRightClickEntity(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return;
		if (!data.canBasicAttack(EquipSlot.OFFHAND))
			return;
		if (!(e.getRightClicked() instanceof LivingEntity))
			return;
		if (!((LivingEntity) e.getRightClicked()).isValid())
			return;
		trigger(p, Trigger.RIGHT_CLICK_HIT, new RightClickHitEvent((LivingEntity) e.getRightClicked()));
	}

	public void cancelRevives(Player p) {
		if (!revivers.containsKey(p))
			return;
		cancelRevive(revivers.get(p), p);
	}

	private void startRevive(PlayerFightData data, Corpse corpse) {
		UUID deadId = corpse.data.getUniqueId();
		Player p = data.getPlayer();
		Player dead = Bukkit.getPlayer(deadId);
		if (dead == null) {
			Util.displayError(p, "Offline dead players cannot be revived!");
			return;
		}

		revivers.put(p, corpse);
		corpse.reviver = data;
		Util.msg(
				p, "You started reviving <yellow>" + dead.getName() + "</yellow>. Stay near their body for 5 seconds!"
		);
		Util.msg(dead, "You are being revived by <yellow>" + p.getName());
		s.broadcastOthers(
				SharedUtil.color(
						"<yellow>" + p.getName() + "</yellow> is reviving <yellow>" + dead.getName() + "</yellow>!"
				), p
		);
		dead.teleport(corpse.corpseDisplay);

		BossBar reviveBar = Bukkit
				.createBossBar("§e" + p.getName() + " §fReviving §e" + dead.getName(), BarColor.BLUE, BarStyle.SOLID);
		bars.add(reviveBar);
		for (Player party : s.getOnlinePlayers()) {
			reviveBar.addPlayer(party);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player spec = Bukkit.getPlayer(uuid);
			if (p != null)
				reviveBar.addPlayer(spec);
		}
		corpse.bar = reviveBar;
		cleanupTasks.add(new BukkitRunnable() {
			@Override
			public void run() {
				reviveBar.removeAll();
				bars.remove(reviveBar);
			}
		});

		for (int i = 0; i < 5; i++) {
			final int count = i;
			final double progress = 0.2 * (i + 1);
			corpse.tasks.add(new BukkitRunnable() {
				@Override
				public void run() {
					if (!canRevive(corpse, p, dead)) {
						cancelRevive(corpse, p);
						return;
					}

					reviveBar.setProgress(progress);
					reviveSounds[count].play(p, p, Audience.ORIGIN);
					reviveSounds[count].play(dead, dead, Audience.ORIGIN);
					reviveCircle.play(reviveCirclePart, corpse.corpseDisplay.getLocation(), LocalAxes.xz(), null);

					// Revive not complete
					if (count == 4) {
						completeRevive(p, corpse);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20 * i));
		}
	}

	private boolean canRevive(Corpse corpse, Player p, Player dead) {
		if (!corpse.isNear(p)) {
			Util.msg(p, "<red>Revival failed! You got too far away!");
			return false;
		}

		if (dead == null || !dead.isOnline()) {
			Util.msg(p, "<red>Revival failed! Dead player logged off!");
			return false;
		}

		return true;
	}

	private void cancelRevive(Corpse corpse, Player p) {
		Player dead = corpse.data.getPlayer();
		if (dead != null) {
			Util.msg(dead, "<red>Revival failed!");
			Sounds.error.play(dead, dead, Audience.ORIGIN);
		}
		Util.msg(p, "<red>Revival failed!");
		Sounds.error.play(p, p, Audience.ORIGIN);
		revivers.remove(p);
		corpse.reviver = null;
		corpse.bar.removeAll();
		for (BukkitTask task : corpse.tasks) {
			task.cancel();
		}
	}

	private void completeRevive(Player p, Corpse corpse) {
		Player dead = corpse.data.getPlayer();
		Sounds.levelup.play(dead, dead);
		revivePart.play(p, p);
		revivers.remove(p);
		corpses.remove(corpse);
		dead.teleport(corpse.corpseDisplay);
		corpse.remove();
		s.broadcast("<yellow>" + p.getName() + " </yellow>has revived <yellow>" + dead.getName());
		PlayerFightData deadData = userData.get(dead.getUniqueId());
		PlayerFightData reviverData = userData.get(p.getUniqueId());
		deadData.setDeath(false);
		deadData.applyStatus(StatusType.INVINCIBLE, reviverData, 1, 20);
		reviverData.getStats().addRevive();
		new BukkitRunnable() {
			@Override
			public void run() {
				corpse.bar.removeAll();
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
	}
	
	public static void handleRightClickGeneral(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		PlayerInventory pinv = p.getInventory();
		boolean hasShield = pinv.getItemInOffHand() != null && pinv.getItemInOffHand().getType() == Material.SHIELD;

		// Trigger case 1. Right click mainhand with something in it
		// Trigger case 2. Right click offhand when mainhand has nothing in it
		if ((p.getInventory().getItemInMainHand() != null && e.getHand() == EquipmentSlot.HAND)
				|| p.getInventory().getItemInMainHand() == null && e.getHand() == EquipmentSlot.OFF_HAND) {
			trigger(p, Trigger.RIGHT_CLICK, null);

			double y = p.getEyeLocation().getDirection().normalize().getY();
			if (p.isSneaking()) {
				trigger(p, Trigger.SHIFT_RCLICK, null);
			}
			
			if (y > 1) {
				trigger(p, Trigger.UP_RCLICK, null);
			} else if (y < -1) {
				trigger(p, Trigger.DOWN_RCLICK, null);
			}
		}
		
		if (e.getHand() == EquipmentSlot.OFF_HAND) {
			if (hasShield) {
				trigger(p, Trigger.RAISE_SHIELD, null);
				if (blockTasks.containsKey(uuid)) {
					blockTasks.get(uuid).cancel();
				}
				
				blockTasks.put(uuid, new BukkitRunnable() {
					@Override
					public void run() {
						if (p == null || !p.isBlocking()) {
							this.cancel();
							trigger(p, Trigger.LOWER_SHIELD, null);
							blockTasks.remove(uuid);
						} else {
							trigger(p, Trigger.SHIELD_TICK, null);
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 10L, 10L));
			}
		}
	}
	
	public static void handleLeftClick(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		Player p = e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return;
		trigger(e.getPlayer(), Trigger.LEFT_CLICK_NO_HIT, null);
		trigger(e.getPlayer(), Trigger.LEFT_CLICK, null);
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			trigger(e.getPlayer(), Trigger.LEFT_CLICK_BLOCK, e);
		}
	}
	
	public static void handleMythicDespawn(MythicMobDespawnEvent e) {
		FightData data = removeFightData(e.getEntity().getUniqueId());
		if (data == null)
			return;
		data.cleanup();
		if (data.getInstance() == null)
			return;
		data.getInstance().handleRespawn(data, e.getMobType().getInternalName(), true, false);
	}
	
	public static void handleMythicDeath(MythicMobDeathEvent e) {
		Entity killer = e.getEntity().getLastDamageCause().getDamageSource().getCausingEntity();

		// Trigger on kill event before removing fight data
		boolean playerKill = killer instanceof Player;
		if (playerKill && e.getEntity() instanceof LivingEntity
				&& SessionManager.getSession(killer.getUniqueId()) != null) {
			Session s = SessionManager.getSession((Player) killer);
			FightInstance.trigger((Player) killer, Trigger.KILL, new KillEvent((LivingEntity) e.getEntity()));
			for (Player p : s.getOnlinePlayers()) {
				FightInstance.trigger(p, Trigger.KILL_GLOBAL, e.getEntity());
			}
		}
		
		FightData data = removeFightData(e.getEntity().getUniqueId());
		if (data == null)
			return;
		data.cleanup();

		if (data.getInstance() == null)
			return;
		
		String id = e.getMobType().getInternalName();
		data.getInstance().handleRespawn(data, id, false, playerKill);
		data.getInstance().handleMobKill(id, playerKill);
	}
	
	public abstract void handleMobKill(String id, boolean playerKill);
	
	public void handleRespawn(FightData data, String id, boolean isDespawn, boolean playerKill) {
		Mob mob = Mob.get(id);
		if (mob == null)
			return;
		
		if (data.getSpawner() != null) {
			data.getSpawner().subtractActiveMobs();
		}
		
		if (!isDespawn && playerKill) {
			totalKillValue += mob.getKillValue();
			if (totalKillValue > KILLS_TO_SCALE) {
				spawnCounter++;
				totalKillValue -= KILLS_TO_SCALE;
			}
		}
		spawnCounter = data.getInstance().activateSpawner(spawnCounter + mob.getKillValue());
	}
	
	public void addSpawnCounter(double amount) {
		this.spawnCounter += amount;
	}
	
	// Method that's called by all listeners and is directly connected to events
	// Returns true if the event should be cancelled
	public static boolean trigger(Player p, Trigger trigger, Object obj) {
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return false;
		if (data.isDead())
			return false;
		if (data.hasStatus(StatusType.STOPPED))
			return true;
		
		boolean cancel = data.runSlotBasedActions(data, trigger, p.getInventory().getHeldItemSlot(), obj);
		return data.runActions(data, trigger, obj) || cancel; // Either slot-based or non-slotbased can cancel the event
	}

	public static FightData getFightData(Entity ent) {
		return getFightData(ent.getUniqueId());
	}
	
	public static FightData getFightData(UUID uuid) {
		if (!fightData.containsKey(uuid)) {
			LivingEntity ent = (LivingEntity) Bukkit.getEntity(uuid);
			ActiveMob am = NeoRogue.mythicApi.getMythicMobInstance(ent);
			if (am == null) {
				Bukkit.getLogger().info(
						"Failed to create new FightData for " + ent.getName() + " " + ent.getType() + " "
								+ ent.getUniqueId()
				);
				return null;
			}
			Mob mob = Mob.get(am.getType().getInternalName());
			FightData fd = new FightData(ent, am, mob, (MapSpawnerInstance) null);
			fightData.put(uuid, fd);
		}
		return fightData.get(uuid);
	}
	
	public static HashMap<UUID, PlayerFightData> getUserData() {
		return userData;
	}
	
	public static PlayerFightData getUserData(UUID uuid) {
		return userData.get(uuid);
	}
	
	public static void giveHeal(LivingEntity caster, double amount, LivingEntity... targets) {
		for (LivingEntity target : targets) {
			if (!(target instanceof Attributable))
				continue;
			PlayerFightData cfd = FightInstance.getUserData(caster.getUniqueId());
			PlayerFightData tfd = FightInstance.getUserData(target.getUniqueId());

			double toSet = Math.min(
					caster.getHealth() + amount,
					((Attributable) caster).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
			);
			double actual = toSet - caster.getHealth();

			if (caster == target) {
				if (cfd != null) {
					cfd.getStats().addSelfHealing(actual);
				}
			} else {
				if (cfd != null) {
					cfd.getStats().addHealingGiven(actual);
				}
				if (tfd != null) {
					tfd.getStats().addHealingReceived(actual);
				}
			}

			caster.setHealth(toSet);
		}
	}
	
	public static void applyStatus(Entity target, String id, Entity applier, int stacks, int seconds) {
		FightData data = getFightData(target.getUniqueId());
		FightData fdApplier = getFightData(applier.getUniqueId());
		try {
			StatusType st = StatusType.valueOf(id);
			data.applyStatus(st, fdApplier, stacks, seconds);
		} catch (IllegalArgumentException e) {
			data.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, id, data), fdApplier, stacks, seconds);
		}
	}
	
	public static void applyStatus(Entity target, Status s, int stacks, int ticks, FightData applier) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(s, applier, stacks, ticks);
	}
	
	public static void applyStatus(Entity target, StatusType type, Entity applier, int stacks, int ticks) {
		FightData data = getFightData(target.getUniqueId());
		FightData fdApplier = getFightData(applier.getUniqueId());
		data.applyStatus(type, fdApplier, stacks, ticks);
	}
	
	public static void applyStatus(Entity target, StatusType type, FightData applier, int stacks, int ticks) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(type, applier, stacks, ticks);
	}
	
	public static void applyStatus(
			Entity target, GenericStatusType type, String id, Entity applier, int stacks, int ticks
	) {
		FightData data = getFightData(target.getUniqueId());
		FightData fdApplier = getFightData(applier.getUniqueId());
		data.applyStatus(Status.createByGenericType(type, id, data), fdApplier, stacks, ticks);
	}
	
	public static void dealDamage(FightData owner, DamageType type, double amount, LivingEntity target, DamageStatTracker tracker) {
		dealDamage(new DamageMeta(owner, amount, type, tracker), target);
	}
	
	public static void knockback(Entity src, Entity trg, double force) {
		knockback(src.getLocation(), trg, force);
	}
	
	// Limits +y velocity
	public static void knockback(Location src, Entity trg, double force) {
		Vector v = trg.getLocation().subtract(src).toVector().setY(0).normalize().multiply(force).setY(0.5);
		knockback(trg, v);
	}
	
	public static void knockback(Entity trg, Vector v) {
		v = trg.getVelocity().add(v);
		v = v.setY(Math.min(v.getY(), 0.5));
		FightData fd = FightInstance.getFightData(trg);
		if (fd != null) {
			v = v.multiply(fd.getKnockbackMultiplier());
		}
		trg.setVelocity(v);
	}
	
	public static void dealDamage(DamageMeta meta, Collection<LivingEntity> targets) {
		if (meta.getOwner() instanceof PlayerFightData) {
			trigger(
					(Player) meta.getOwner().getEntity(), Trigger.DEAL_DAMAGE_MULTIPLE,
					new PreDealDamageMultipleEvent(meta, targets)
			);
		}
		for (LivingEntity target : targets) {
			dealDamage(meta.clone(), target);
		}
	}
	
	public static double dealDamage(DamageMeta meta, LivingEntity target) {
		return meta.clone().dealDamage(target);
	}
	
	public static double dealDamage(DamageMeta meta, LivingEntity target, SkillMetadata data) {
		return meta.clone().dealDamage(target, data);
	}
	
	@Override
	public void setup() {
		instantiate();
		s.broadcast("Commencing fight...");
		setupInstance(s);
		FightInstance fi = this;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				// Setup triggers
				ArrayList<PlayerFightData> fdata = new ArrayList<PlayerFightData>();
				for (Player p : s.getOnlinePlayers()) {
					fdata.add(setup(p, s.getData(p.getUniqueId())));
				}
				
				// Choose random teleport location
				spawn = map.getRandomSpawn().toLocation();
				spawn.add(
						s.getXOff() + MapPieceInstance.X_FIGHT_OFFSET, MapPieceInstance.Y_OFFSET,
						MapPieceInstance.Z_FIGHT_OFFSET + s.getZOff()
				);
				spawn.setX(-spawn.getX());
				
				for (Player p : s.getOnlinePlayers()) {
					p.teleport(spawn);
				}
				for (UUID uuid : s.getSpectators().keySet()) {
					Player p = Bukkit.getPlayer(uuid);
					p.teleport(spawn);
				}
				
				for (FightRunnable runnable : initialTasks) {
					runnable.run(fi, fdata);
				}
				s.setBusy(false);
			}
		}.runTaskLater(NeoRogue.inst(), 20L);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				double rngBonus = NeoRogue.gen.nextDouble(-1, 1);
				double toActivate = rngBonus + (map.getEffectiveSize() / 2);
				
				activateSpawner(toActivate);

				startTime = System.currentTimeMillis();
				for (MapSpawnerInstance inst : fi.initialSpawns) {
					inst.spawnMob();
				}
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
		
		tasks.add(new BukkitRunnable() {
			boolean alternate = false;
			
			@Override
			public void run() {
				alternate = !alternate;
				
				// Every 20 ticks
				if (alternate && !toTick.isEmpty()) {
					updateSpectatorLines();

					Iterator<UUID> iter = toTick.iterator();
					while (iter.hasNext()) {
						FightData data = fightData.get(iter.next());
						if (data == null) {
							iter.remove();
							continue;
						}
						if (data.getEntity() == null) {
							iter.remove();
							FightInstance.removeFightData(data.getUniqueId());
							continue;
						}
						if (data.runTickActions() == TickResult.REMOVE)
							iter.remove();
					}
				}
				
				// Every 10 ticks
				for (Session s : SessionManager.getSessions()) {
					if (!(s.getInstance() instanceof FightInstance))
						continue;
					for (Barrier b : ((FightInstance) s.getInstance()).getEnemyBarriers().values()) {
						b.tick();
					}
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L));
	}
	
	protected abstract void setupInstance(Session s);
	
	public static void putFightData(UUID uuid, FightData data) {
		fightData.put(uuid, data);
	}
	
	private PlayerFightData setup(Player p, PlayerSessionData data) {
		UUID uuid = p.getUniqueId();
		PlayerFightData fd = new PlayerFightData(this, data);
		fightData.put(uuid, fd);
		userData.put(uuid, fd);
		return fd;
	}

	public Session getSession() {
		return s;
	}
	
	@Override
	public void cleanup() {
		long time = System.currentTimeMillis() - startTime;
		final long hr = TimeUnit.MILLISECONDS.toHours(time);
		final long min = TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS
				.toSeconds(time - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(
				time - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec)
		);
		String timer = String.format("%d:%02d.%03d", min, sec, ms);
		
		s.broadcast(FightStatistics.getStatsHeader(timer));
		for (UUID uuid : s.getParty().keySet()) {
			PlayerFightData pdata = userData.remove(uuid);
			PlayerSessionData data = pdata.getSessionData();
			Player p = pdata.getPlayer();
			if (pdata != null) {
				pdata.cleanup();
				if (p != null) {
					if (pdata.isDead()) {
						pdata.setDeath(false);
					}
					data.updateHealth();
					data.syncHealth();
					p.setFoodLevel(20);
					data.revertMaxHealth();
					data.updateCoinsBar();
					p.clearActivePotionEffects();
					s.broadcast(pdata.getStats().getStatLine());
				}
			}
			FightData fdata = fightData.remove(uuid);
			if (fdata != null)
				fdata.cleanup();
		}

		for (Corpse c : corpses) {
			c.remove();
		}

		for (UUID ind : indicators) {
			Entity ent = Bukkit.getEntity(ind);
			if (ent != null)
				ent.remove();
		}

		for (BukkitRunnable cleanupTask : cleanupTasks) {
			cleanupTask.runTask(NeoRogue.inst());
		}
		
		for (BukkitTask task : tasks) {
			task.cancel();
		}
	}
	
	public static FightData removeFightData(UUID uuid) {
		FightData fd = fightData.remove(uuid);
		if (fd == null) return null;
		if (fd.getInstance() == null) return fd;
		fd.getInstance().removeFromTickList(uuid);
		return fd;
	}
	
	public void addToTickList(UUID uuid) {
		toTick.add(uuid);
	}

	public void removeFromTickList(UUID uuid) {
		toTick.remove(uuid);
	}
	
	// For any barrier that isn't the user's personal barrier (shield)
	public void addUserBarrier(PlayerFightData owner, Barrier b, int duration) {
		addBarrier(owner, b, duration, true);
	}
	
	public void addEnemyBarrier(PlayerFightData owner, Barrier b, int duration) {
		addBarrier(owner, b, duration, false);
	}
	
	public void addBarrier(FightData owner, Barrier b, int duration, boolean isUser) {
		HashMap<UUID, Barrier> barriers = isUser ? userBarriers : enemyBarriers;
		UUID uuid = b.getUniqueId();
		barriers.put(uuid, b);
		owner.addGuaranteedTask(uuid, new Runnable() {
			@Override
			public void run() {
				barriers.remove(uuid);
			}
		}, duration);
	}
	
	public Location getMythicLocation(String key) {
		return mythicLocations.get(key);
	}
	
	public void removeEnemyBarrier(UUID uuid) {
		enemyBarriers.remove(uuid);
	}
	
	public HashMap<UUID, Barrier> getEnemyBarriers() {
		return enemyBarriers;
	}
	
	public void addSpawner(MapSpawnerInstance spawner) {
		spawners.add(spawner);
		if (spawner.getMaxMobs() == -1) {
			unlimitedSpawners.add(spawner);
		}
	}
	
	public void addInitialSpawn(MapSpawnerInstance spawner) {
		initialSpawns.add(spawner);
	}
	
	public void addMythicLocation(String key, Location loc) {
		mythicLocations.put(key, loc);
	}
	
	// Returns attempted - actual spawns
	protected double activateSpawner(double value) {
		double current = 0;
		if (spawners.isEmpty())
			return value;
		if (value <= 0)
			return value;
		while (current < value) {
			MapSpawnerInstance spawner = spawners.get(NeoRogue.gen.nextInt(spawners.size()));
			if (!spawner.canSpawn()) {
				spawner = unlimitedSpawners.get(NeoRogue.gen.nextInt(unlimitedSpawners.size()));
			}
			spawner.spawnMob();
			current += spawner.getMob().getSpawnValue();
		}
		return value - current;
	}
	
	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] FightInstance attempted to save, this should never happen");
		return null;
	}
	
	public abstract String serializeInstanceData();
	
	public static FightInstance deserializeInstanceData(Session s, HashMap<UUID, PlayerSessionData> party, String str) {
		if (str.startsWith("STANDARD")) {
			return new StandardFightInstance(s, party.keySet(), Map.deserialize(str.substring("STANDARD:".length())));
		} else if (str.startsWith("MINIBOSS")) {
			return new MinibossFightInstance(s, party.keySet(), Map.deserialize(str.substring("MINIBOSS:".length())));
		} else {
			return new BossFightInstance(s, party.keySet(), Map.deserialize(str.substring("BOSS:".length())));
		}
	}
	
	public interface FightRunnable {
		public void run(FightInstance inst, ArrayList<PlayerFightData> fdata);
	}
	
	public static ActiveMob spawnScaledMob(Session s, Location loc, MythicMob mythicMob) {
		return scaleMob(
				s, Mob.get(mythicMob.getInternalName()), mythicMob,
				mythicMob.spawn(BukkitAdapter.adapt(loc), s.getLevel())
		);
	}

	public static ActiveMob scaleMob(Session s, Mob mob, MythicMob mythicMob, ActiveMob am) {
		double lvl = s.getLevel();
		am.setLevel(lvl);
		if (mythicMob.getHealth() == null)
			return am; // Some summoned mobs don't have health
			
		double mhealth = mythicMob.getHealth().get();
		// Bosses scale with number of players too
		if (mob != null && mob.getType() != MobType.NORMAL) {
			mhealth *= 0.75 + (s.getParty().size() * 0.25); // 25% health increase per player, starting from 2 players
		}
		mhealth *= 1 + (lvl / 5);
		am.getEntity().setMaxHealth(Math.round(mhealth));
		am.getEntity().setHealth(Math.round(mhealth));
		return am;
	}

	public void addBar(BossBar bar) {
		bars.add(bar);
	}

	public ArrayList<BossBar> getBars() {
		return bars;
	}

	private static class Corpse {
		protected PlayerFightData data;
		protected PlayerFightData reviver;
		protected BossBar bar;
		protected Entity corpseDisplay;
		protected LinkedList<Entity> hitbox = new LinkedList<Entity>();
		protected LinkedList<BukkitTask> tasks = new LinkedList<BukkitTask>();
		
		public Corpse(PlayerFightData data) {
			this.data = data;
			Player p = data.getPlayer();
			p.getInventory().clear();
			World w = p.getWorld();
			Location loc = p.getLocation();
			Vector v = new Vector(-0.3, 0, 0); // The direction when yaw is 0
			v.rotateAroundY(Math.toRadians(p.getYaw()));
			corpseDisplay = w.spawnEntity(loc, EntityType.ARMOR_STAND);
			PlayerDisguise dis = new PlayerDisguise(p);
			dis.getWatcher().setSleeping(true);
			dis.setName(p.getName() + " (Click to Revive)");
			dis.setEntity(corpseDisplay);
			dis.getWatcher().setGlowing(true);
			dis.startDisguise();

			loc.subtract(v);
			for (int i = 0; i < 7; i++) {
				Entity e = w.spawnEntity(loc.add(v), EntityType.ARMOR_STAND);
				hitbox.add(e);
				ArmorStand as = (ArmorStand) e;
				as.setSmall(true);
				as.setArms(true);
				as.setInvisible(true);
			}
		}

		public void remove() {
			corpseDisplay.remove();
			for (Entity ent : hitbox) {
				ent.remove();
			}

			if (bar != null)
				bar.removeAll();
		}

		public boolean hitCorpse(Entity e) {
			if (e.getLocation().distanceSquared(corpseDisplay.getLocation()) > 9)
				return false;
			for (Entity hit : hitbox) {
				if (e == hit)
					return true;
			}
			return false;
		}

		public boolean isNear(Entity e) {
			return e.getLocation().distanceSquared(corpseDisplay.getLocation()) < 25;
		}
	}
}
