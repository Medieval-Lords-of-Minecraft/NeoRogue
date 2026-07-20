package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
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
import io.lumine.mythic.core.mobs.DespawnMode;
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
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.map.Coordinates;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.map.MapPieceInstance;
import me.neoblade298.neorogue.map.MapSpawnerInstance;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;
import me.neoblade298.neorogue.session.analytics.AnalyticsManager;
import me.neoblade298.neorogue.session.analytics.EquipmentContribution;
import me.neoblade298.neorogue.session.analytics.FightSnapshot;
import me.neoblade298.neorogue.session.fight.TickAction.TickResult;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageMultipleEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;
import me.neoblade298.neorogue.session.instances.Instance;
import me.neoblade298.neorogue.session.instances.LoseInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import me.neoblade298.neorogue.tutorial.TutorialManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public abstract class FightInstance extends Instance {
	private static HashMap<UUID, PlayerFightData> userData = new HashMap<UUID, PlayerFightData>();
	private static HashMap<UUID, Barrier> userBarriers = new HashMap<UUID, Barrier>();
	private static HashMap<UUID, FightData> fightData = new HashMap<UUID, FightData>();
	private static HashMap<UUID, BukkitTask> blockTasks = new HashMap<UUID, BukkitTask>();
	private static HashSet<UUID> indicators = new HashSet<UUID>();
	
	protected HashSet<UUID> toTick = new HashSet<UUID>();
	private HashSet<UUID> pendingToTick = new HashSet<UUID>();
	private boolean isIteratingToTick = false;
	protected LinkedList<Corpse> corpses = new LinkedList<Corpse>();
	protected HashMap<Player, Corpse> revivers = new HashMap<Player, Corpse>();
	protected Map map;
	// A single mob modifier assigned to this fight (notoriety feature). For miniboss/boss fights every
	// non-normal mob spawns with it; for standard fights each mob has a chance to spawn with it. It is
	// serialized with the fight instance so quitting and rejoining can't reroll it.
	protected MobModifier modifier;
	protected ArrayList<MapSpawnerInstance> spawners = new ArrayList<MapSpawnerInstance>(),
			unlimitedSpawners = new ArrayList<MapSpawnerInstance>(),
			initialSpawns = new ArrayList<MapSpawnerInstance>();
	private HashMap<String, Location> mythicLocations = new HashMap<String, Location>();
	protected HashMap<UUID, Barrier> enemyBarriers = new HashMap<UUID, Barrier>();
	protected LinkedList<BukkitTask> tasks = new LinkedList<BukkitTask>();
	protected LinkedList<BukkitRunnable> cleanupTasks = new LinkedList<BukkitRunnable>();
	protected LinkedList<FightRunnable> initialTasks = new LinkedList<FightRunnable>();
	protected double spawnCounter; // When above 1, a mob spawns
	private long startTime;
	private ArrayList<String> spectatorLines;
	protected boolean isActive = true;
	protected ArrayList<BossBar> bars = new ArrayList<BossBar>();
	// Fight outcome for analytics: null = aborted/unknown, true = win, false = loss
	private Boolean fightWon = null;

	protected void showBar(Player p, BossBar bar) {
		p.showBossBar(bar);
	}

	protected void hideBar(Player p, BossBar bar) {
		p.hideBossBar(bar);
	}

	protected void showBarToAll(BossBar bar) {
		for (Player p : s.getOnlinePlayers()) {
			p.showBossBar(bar);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) p.showBossBar(bar);
		}
	}

	protected void hideBarFromAll(BossBar bar) {
		for (Player p : s.getOnlinePlayers()) {
			p.hideBossBar(bar);
		}
		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) p.hideBossBar(bar);
		}
	}
	private boolean isCleaned;
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
		super(s, new PlayerFlags(PlayerFlag.SHORTER_IFRAMES, PlayerFlag.ALLOW_FLIGHT_FALL));
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

	public void updateSpectatorLines() {
		spectatorLines = new ArrayList<String>(9);
		for (Player p : s.getOnlinePlayers()) {
			spectatorLines.add(createHealthBar(userData.get(p.getUniqueId())));
		}
	}

	public ArrayList<String> getSpectatorLines() {
		return spectatorLines;
	}

	@Override
	public void handleSpectatorJoin(Player p) {
		for (BossBar bar : bars) {
			p.showBossBar(bar);
		}
	}

	@Override
	public void handleSpectatorLeave(Player p) {
		for (BossBar bar : bars) {
			p.hideBossBar(bar);
		}
	}

	private static String createHealthBar(PlayerFightData pfd) {
		if (pfd != null && pfd.isDead()) {
			return "&c&m" + pfd.getSessionData().getData().getDisplay();
		}
		if (pfd.getPlayer() == null) {
			return "&7&m" + pfd.getSessionData().getData().getDisplay();
		}
		Player p = pfd.getPlayer();
		double percenthp = p.getHealth() / p.getAttribute(Attribute.MAX_HEALTH).getValue();
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
		s.broadcast("<red>" + p.getName() + " died!");
		data.setDeath(true);
		data.getStats().addDeath();

		// If that's the last player alive, send them to lose instance
		if (fi.isLose()) {
			runLoseLogic(fi);
		}
		else {
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

	public static void runLoseLogic(FightInstance fi) {
		// Prevent double-resolution: if the fight was already won (e.g. the player's
		// return damage killed the last mob on the same tick they died), don't override
		// it with a loss.
		if (!fi.isActive) return;
		fi.isActive = false;
		fi.fightWon = false;
		new BukkitRunnable() {
			public void run() {
				for (PlayerFightData data : userData.values()) {
					Player p = data.getPlayer();
					if (p == null) continue;
					p.spigot().respawn();
				}
				Session sess = fi.getSession();
				new BukkitRunnable() {
					@Override
					public void run() {
						fi.broadcastStatistics();
						sess.setInstance(new LoseInstance(sess));
					}
				}.runTask(NeoRogue.inst());
			}
		}.runTask(NeoRogue.inst());
	}

	@Override
	public void handlePlayerLeaveParty(OfflinePlayer p) {
		if (p instanceof Player pl) {
			for (BossBar bar : bars) {
				pl.hideBossBar(bar);
			}
		}
		userData.remove(p.getUniqueId()).cleanup();
		fightData.remove(p.getUniqueId());
		if (isLose()) {
			runLoseLogic(this);
		}
	}

	private boolean isLose() {
		for (Player pl : s.getOnlinePlayers()) {
			PlayerFightData fdata = userData.get(pl.getUniqueId());
			if (fdata != null && fdata.isActive()) {
				return false;
			}
		}
		return true;
	}

	public boolean isActive() {
		return isActive;
	}

	public void createIndicator(Component txt, Location src) {
		createIndicator(txt, src, false, null);
	}

	public void createIndicator(Component txt, Location src, boolean bigHit) {
		createIndicator(txt, src, bigHit, null);
	}

	public void createIndicator(Component txt, Location src, boolean bigHit, Vector direction) {
		TextDisplay td = (TextDisplay) src.getWorld().spawnEntity(src, EntityType.TEXT_DISPLAY);
		td.text(txt);
		Transformation trans = td.getTransformation();
		trans.getScale().set(bigHit ? 3 : 2);
		td.setBillboard(Billboard.CENTER);
		td.setTransformation(trans);
		td.setTeleportDuration(bigHit ? 1 : 40);
		if (bigHit) {
			// Perpendicular-to-facing direction for the shake axis
			Vector rawDir = direction != null ? direction : src.getDirection();
			Vector facing = rawDir.clone().setY(0);
			if (facing.lengthSquared() == 0) facing.setX(1);
			else facing.normalize();
			final Vector right = new Vector(0, 1, 0).crossProduct(facing).normalize();
			new BukkitRunnable() {
				int ticks = 0;
				double totalY = 0;
				double currentX = 0;
				// Positions relative to spawn: large first kick, then damped oscillation (~75% decay per step)
				final double[] shakeX = { 0.70, -0.53, 0.40, -0.30, 0.22, -0.17, 0.13, -0.09, 0.07, -0.05 };
				@Override
				public void run() {
					if (ticks >= 10) {
						td.teleport(td.getLocation().add(right.getX() * -currentX, 2 - totalY, right.getZ() * -currentX));
						this.cancel();
						return;
					}
					double dx = shakeX[ticks] - currentX;
					currentX = shakeX[ticks];
					double dy = 0.1;
					totalY += dy;
					td.teleport(td.getLocation().add(right.getX() * dx, dy, right.getZ() * dx));
					ticks++;
					// Prime the slow teleport duration one tick before the final float-up
					if (ticks >= 10) td.setTeleportDuration(30);
				}
			}.runTaskTimer(NeoRogue.inst(), 1L, 1L);
		}
		else {
			new BukkitRunnable() {
				@Override
				public void run() {
					td.teleport(td.getLocation().add(0, 2, 0));
				}
			}.runTaskLater(NeoRogue.inst(), 2);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				td.remove();
				indicators.remove(td.getUniqueId());
			}
		}.runTaskLater(NeoRogue.inst(), 40L);

	}

	@Override
	public void handlePlayerLogout(Player p) {
		super.handlePlayerLogout(p);
		if (isLose()) {
			runLoseLogic(this);
		}
	}

	@Override
	public void handlePlayerLogin(Player p) {
		super.handlePlayerLogin(p);
		PlayerFightData pdata = getUserData(p.getUniqueId());
		if (pdata == null) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to get player fight data on rejoin for " + p.getName());
			return;
		}

		pdata.updatePlayer();

		if (pdata.isDead()) {
			p.setInvulnerable(true);
			p.setInvisible(true);
		}

		// The rejoining player has a fresh client visibility list, so reconcile
		// death-based visibility: hide dead players from alive players (and vice versa)
		boolean pDead = pdata.isDead();
		for (Player other : getSession().getOnlinePlayers()) {
			if (other.equals(p))
				continue;
			PlayerFightData od = getUserData(other.getUniqueId());
			boolean otherDead = od != null && od.isDead();
			// Hide dead players from the rejoining player unless the rejoiner is also dead
			if (otherDead && !pDead) {
				p.hideEntity(NeoRogue.inst(), other);
			} else {
				p.showEntity(NeoRogue.inst(), other);
			}
			// Hide the rejoining player from alive players if they're dead
			if (pDead && !otherDead) {
				other.hideEntity(NeoRogue.inst(), p);
			} else {
				other.showEntity(NeoRogue.inst(), p);
			}
		}

		for (BossBar bar : bars) {
			p.showBossBar(bar);
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
	
	public void handleWin(Title title, Instance next) {
		// Prevent double-resolution: if the fight was already resolved (e.g. the last
		// player died on the same tick the final mob was killed), don't fire a win.
		if (!isActive) return;
		for (PlayerFightData data : userData.values()) {
			trigger(data.getPlayer(), Trigger.WIN_FIGHT, new Object[0]);
		}
		isActive = false;
		fightWon = true;

		broadcastStatistics();
		s.launchFireworks();
		cleanupHelper(false);
		s.broadcastTitle(title);
		s.broadcastSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);

		new BukkitRunnable() {
			@Override
			public void run() {
				s.setInstance(next);
			}
		}.runTaskLater(NeoRogue.inst(), 60L);
	}
	
	public static void handleHotbarSwap(PlayerItemHeldEvent e) {
		// Only cancel swap if something is bound to the trigger
		Player p = e.getPlayer();
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return;
		// Disabled slots can't be switched to
		if (data.isSlotDisabled(e.getNewSlot())) {
			e.setCancelled(true);
			return;
		}
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
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data != null) {
			data.setDroppedThisTick(true);
		}
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
		float fallDistance = p.getFallDistance();
		trigger(p, Trigger.TOGGLE_FLIGHT, e);
		p.setAllowFlight(false);
		p.setFallDistance(fallDistance);
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

	// Returns true if the player is reviving someone, false otherwise
	public static boolean handleClickArmorStand(Player p, Entity armorStand) {
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null || data.isDead())
			return false;
		FightInstance fi = data.getInstance();
		for (Corpse c : fi.corpses) {
			if (c.hitCorpse(armorStand)) {
				if (fi.revivers.containsKey(p)) {
					Util.displayError(p, "You're already reviving someone!");
					return false;
				}

				if (c.reviver != null) {
					Util.displayError(p, "Someone is already reviving this player!");
					return false;
				}
				fi.startRevive(data, c);
				return true;
			}
		}
		return false;
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
		Util.msgRaw(
				p, "You started reviving <yellow>" + dead.getName() + "</yellow>. Stay near their body for 5 seconds!"
		);
		Util.msgRaw(dead, "You are being revived by <yellow>" + p.getName());
		s.broadcastOthers(
				SharedUtil.color(
						"<yellow>" + p.getName() + "</yellow> is reviving <yellow>" + dead.getName() + "</yellow>!"
				), p
		);
		dead.teleport(corpse.corpseDisplay);

		BossBar reviveBar = BossBar.bossBar(
				Component.text(p.getName() + " ", NamedTextColor.YELLOW)
					.append(Component.text("Reviving ", NamedTextColor.WHITE))
					.append(Component.text(dead.getName(), NamedTextColor.YELLOW)),
				0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
		bars.add(reviveBar);
		showBarToAll(reviveBar);
		corpse.bar = reviveBar;
		cleanupTasks.add(new BukkitRunnable() {
			@Override
			public void run() {
				hideBarFromAll(reviveBar);
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

					reviveBar.progress((float) progress);
					reviveSounds[count].play(p, p, Audience.ORIGIN);
					reviveSounds[count].play(dead, dead, Audience.ORIGIN);
					reviveCircle.play(reviveCirclePart, corpse.corpseDisplay.getLocation(), LocalAxes.xz(), null);

					// Revive not complete
					if (count == 4) {
						completeRevive(p, corpse, reviveBar);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20 * i));
		}
	}

	private boolean canRevive(Corpse corpse, Player p, Player dead) {
		if (!corpse.isNear(p)) {
			Util.msgRaw(p, "<red>Revival failed! You got too far away!");
			return false;
		}

		if (dead == null || !dead.isOnline()) {
			Util.msgRaw(p, "<red>Revival failed! Dead player logged off!");
			return false;
		}

		return true;
	}

	private void cancelRevive(Corpse corpse, Player p) {
		Player dead = corpse.data.getPlayer();
		if (dead != null) {
			Util.msgRaw(dead, "<red>Revival failed!");
			Sounds.error.play(dead, dead, Audience.ORIGIN);
		}
		Util.msgRaw(p, "<red>Revival failed!");
		Sounds.error.play(p, p, Audience.ORIGIN);
		revivers.remove(p);
		corpse.reviver = null;
		hideBarFromAll(corpse.bar);
		for (BukkitTask task : corpse.tasks) {
			task.cancel();
		}
	}

	private void completeRevive(Player p, Corpse corpse, BossBar reviveBar) {
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
		bars.remove(reviveBar);
		new BukkitRunnable() {
			@Override
			public void run() {
				hideBarFromAll(corpse.bar);
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
		// Bukkit fires LEFT_CLICK_AIR alongside PlayerDropItemEvent when Q is pressed;
		// suppress to prevent slot-based triggers from activating on item drop
		if (data.isDroppedThisTick()) {
			data.setDroppedThisTick(false);
			return;
		}
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
		data.getInstance().handleMobDespawn(data, e.getMobType().getInternalName(), true, false);
	}
	
	public static void handleMythicDeath(MythicMobDeathEvent e) {
		Entity killer = e.getEntity().getLastDamageCause().getDamageSource().getCausingEntity();

		// Trigger on kill event before removing fight data
		boolean playerKill = killer instanceof Player;
		if (playerKill && e.getEntity() instanceof LivingEntity
				&& SessionManager.getSession(killer.getUniqueId()) != null) {
			Session s = SessionManager.getSession((Player) killer);
			for (Player p : s.getOnlinePlayers()) {
				FightInstance.trigger(p, Trigger.KILL_GLOBAL, e.getEntity());
			}
		}
		
		FightData data = removeFightData(e.getEntity().getUniqueId());
		if (data == null)
			return;
		data.runDeathActions();
		data.cleanup();

		if (data.getInstance() == null || !data.getInstance().isActive)
			return;
		
		String id = e.getMobType().getInternalName();
		data.getInstance().handleMobKill(data, id, playerKill);
	}
	
	public abstract void handleMobKill(FightData fd, String id, boolean playerKill);

	public abstract void handleMobDespawn(FightData fd, String id, boolean despawn, boolean playerKill);
	
	public void addSpawnCounter(double amount) {
		this.spawnCounter += amount;
	}
	
	// Method that's called by all listeners and is directly connected to events
	// Runs both the general actions and, if the held slot has slot-based actions
	// bound to this trigger, those too. Returns true if the event should be cancelled
	public static boolean trigger(Player p, Trigger trigger, Object obj) {
		PlayerFightData data = userData.get(p.getUniqueId());
		if (data == null)
			return false;
		if (data.isDead())
			return false;
		if (data.hasStatus(StatusType.STOPPED))
			return true;
		
		boolean cancel = data.runActions(data, trigger, obj);
		int slot = p.getInventory().getHeldItemSlot();
		if (trigger.isSlotBased()) {
			cancel = data.runSlotBasedActions(data, trigger, slot, obj) || cancel;
		}
		return cancel;
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

	// Used for CmdAdminDamage
	public FightData createFightData(LivingEntity ent) {
		ActiveMob am = NeoRogue.mythicApi.getMythicMobInstance(ent);
		FightData fd = new FightData(ent, am, null, (MapSpawnerInstance) null);
		fightData.put(ent.getUniqueId(), fd);
		return fd;
	}
	
	public static HashMap<UUID, PlayerFightData> getUserData() {
		return userData;
	}
	
	public static PlayerFightData getUserData(UUID uuid) {
		return userData.get(uuid);
	}
	
	public static void giveHeal(LivingEntity caster, double amount, LivingEntity... targets) {
		giveHeal(caster, amount, (Equipment) null, targets);
	}

	public static void giveHeal(LivingEntity caster, double amount, Equipment source, LivingEntity... targets) {
		for (LivingEntity target : targets) {
			if (!(target instanceof Attributable))
				continue;
			PlayerFightData cfd = FightInstance.getUserData(caster.getUniqueId());
			PlayerFightData tfd = FightInstance.getUserData(target.getUniqueId());

			double toSet = Math.min(
					caster.getHealth() + amount,
					((Attributable) caster).getAttribute(Attribute.MAX_HEALTH).getValue()
			);
			double actual = toSet - caster.getHealth();

			if (caster == target) {
				if (cfd != null) {
					cfd.getStats().addSelfHealing(actual);
					cfd.getStats().addHealingDone(source, actual);
				}
			} else {
				if (cfd != null) {
					cfd.getStats().addHealingGiven(actual);
					cfd.getStats().addHealingDone(source, actual);
				}
				if (tfd != null) {
					tfd.getStats().addHealingReceived(actual);
				}
			}

			caster.setHealth(toSet);
		}
	}
	
	public static void applyStatus(Entity target, String id, Entity applier, int stacks, int seconds) {
		applyStatus(target, id, applier, stacks, seconds, null);
	}

	public static void applyStatus(Entity target, String id, Entity applier, int stacks, int seconds, Equipment source) {
		FightData data = getFightData(target.getUniqueId());
		FightData fdApplier = getFightData(applier.getUniqueId());
		try {
			StatusType st = StatusType.valueOf(id);
			data.applyStatus(st, fdApplier, stacks, seconds, source);
		} catch (IllegalArgumentException e) {
			data.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, id, data), fdApplier, stacks, seconds, source);
		}
	}
	
	public static void applyStatus(Entity target, Status s, int stacks, int ticks, FightData applier) {
		applyStatus(target, s, stacks, ticks, applier, null);
	}

	public static void applyStatus(Entity target, Status s, int stacks, int ticks, FightData applier, Equipment source) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(s, applier, stacks, ticks, source);
	}
	
	public static void applyStatus(Entity target, StatusType type, Entity applier, int stacks, int ticks) {
		applyStatus(target, type, applier, stacks, ticks, (Equipment) null);
	}

	public static void applyStatus(Entity target, StatusType type, Entity applier, int stacks, int ticks, Equipment source) {
		FightData data = getFightData(target.getUniqueId());
		FightData fdApplier = getFightData(applier.getUniqueId());
		data.applyStatus(type, fdApplier, stacks, ticks, source);
	}
	
	public static void applyStatus(Entity target, StatusType type, FightData applier, int stacks, int ticks) {
		applyStatus(target, type, applier, stacks, ticks, (Equipment) null);
	}

	public static void applyStatus(Entity target, StatusType type, FightData applier, int stacks, int ticks, Equipment source) {
		FightData data = getFightData(target.getUniqueId());
		data.applyStatus(type, applier, stacks, ticks, source);
	}
	
	public static void applyStatus(
			Entity target, GenericStatusType type, String id, Entity applier, int stacks, int ticks
	) {
		applyStatus(target, type, id, applier, stacks, ticks, null);
	}

	public static void applyStatus(
			Entity target, GenericStatusType type, String id, Entity applier, int stacks, int ticks, Equipment source
	) {
		FightData data = getFightData(target.getUniqueId());
		FightData fdApplier = getFightData(applier.getUniqueId());
		data.applyStatus(Status.createByGenericType(type, id, data), fdApplier, stacks, ticks, source);
	}
	
	public static void dealDamage(FightData owner, DamageType type, double amount, LivingEntity target, DamageStatTracker tracker) {
		dealDamage(new DamageMeta(owner, amount, type, tracker), target);
	}
	
	public static void knockback(Entity src, Entity trg, double force) {
		knockback(src.getLocation(), trg, force);
	}
	
	// Limits +y velocity
	public static void knockback(Location src, Entity trg, double force) {
		Vector v = trg.getLocation().subtract(src).toVector().setY(0);
		if (v.lengthSquared() == 0) return; // Prevent NaN from normalizing zero-length vector
		v = v.normalize().multiply(force).setY(0.5);
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
		s.broadcastTitle(Title.title(Component.text("Commencing fight..."), Component.text(" ")));
		setupInstance(s);
		FightInstance fi = this;

		// Choose random map piece to spawn in and order spawners by distance from it
		Coordinates spawnCoords = map.getRandomSpawn();
		ArrayList<MapSpawnerInstance> spawnersByDist = new ArrayList<MapSpawnerInstance>(fi.spawners);
		spawnersByDist.sort(
				(a, b) -> Double
						.compare(spawnCoords.toLocation().distanceSquared(a.getLocation()), spawnCoords.toLocation().distanceSquared(b.getLocation()))
		);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				// Setup triggers
				ArrayList<PlayerFightData> fdata = new ArrayList<PlayerFightData>();
				for (Player p : s.getOnlinePlayers()) {
					PlayerFightData pdata = setup(p, s.getData(p.getUniqueId()));
					TutorialManager.registerFightTutorials(fi, pdata);
					AchievementManager.registerFightAchievements(fi, pdata);
					fdata.add(pdata);
				}
				
				// Choose random teleport location
				spawn = map.getRandomSpawn().toLocation();
				spawn.add(
						s.getXOff() + MapPieceInstance.X_FIGHT_OFFSET, MapPieceInstance.Y_OFFSET,
						MapPieceInstance.Z_FIGHT_OFFSET + s.getZOff()
				);
				spawn.setX(-spawn.getX() + (spawn.getX() % 1 != 0 ? 1 : 0));
				
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
				double toActivate = getInitialSpawnBudget();
				
				if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] Initial spawn: toActivate=" + toActivate
						+ " spawners=" + spawners.size() + " initialSpawns=" + fi.initialSpawns.size());
				
				// Always spawn one of the closest spawners if it exists (it won't for minibosses and bosses)
				if (!spawnersByDist.isEmpty()) {
					if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] Spawning closest spawner");
					spawnersByDist.getFirst().spawnMob();
					toActivate -= spawnersByDist.getFirst().getMob().getSpawnValue();
				}
				if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] Calling activateSpawner(" + toActivate + ")");
				activateSpawner(toActivate);

				startTime = System.currentTimeMillis();
				for (MapSpawnerInstance inst : fi.initialSpawns) {
					if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] Initial spawn entry: " + inst.getMob().getId());
					inst.spawnMob();
				}
			}
		}.runTaskLater(NeoRogue.inst(), 40L);
		
		tasks.add(new BukkitRunnable() {
			boolean alternate = false;
			
			@Override
			public void run() {
				alternate = !alternate;
				
				// Every 20 ticks
				if (alternate && !toTick.isEmpty()) {
					updateSpectatorLines();

					isIteratingToTick = true;
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
					isIteratingToTick = false;
					toTick.addAll(pendingToTick);
					pendingToTick.clear();
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
	
	public static boolean hasFightData(UUID uuid) {
		return fightData.containsKey(uuid);
	}
	
	private PlayerFightData setup(Player p, PlayerSessionData data) {
		UUID uuid = p.getUniqueId();
		PlayerFightData fd = new PlayerFightData(this, data);
		// fightData.put already done in PlayerFightData constructor
		userData.put(uuid, fd);
		return fd;
	}

	public Session getSession() {
		return s;
	}

	private void broadcastStatistics() {
		long time = System.currentTimeMillis() - startTime;
		final long hr = TimeUnit.MILLISECONDS.toHours(time);
		final long min = TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS
				.toSeconds(time - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(
				time - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec)
		);
		String timer = String.format("%d:%02d.%03d", min, sec, ms);
		
		// Don't show a fight score/rating when the player lost or left
		FightScore score = Boolean.FALSE.equals(fightWon) || fightWon == null ? null : getFightScore();
		s.broadcast(FightStatistics.getStatsHeader(timer, score));
	}

	public FightScore getFightScore() {
		return null;
	}
	
	@Override
	public void cleanup(boolean pluginDisable) {
		cleanupHelper(pluginDisable);
	}

	// Needs a helper because this is called from two places
	private void cleanupHelper(boolean pluginDisable) {
		if (isCleaned) return;
		isCleaned = true;
		isActive = false;

		if (fightWon == null && !pluginDisable) {
			broadcastStatistics();
		}

		// Analytics: only record fights that resolved to a clear win/loss (not aborted/plugin disable)
		boolean recordAnalytics = fightWon != null && !pluginDisable && AnalyticsManager.ENABLED;
		ArrayList<FightSnapshot.EquipRow> equipRows = new ArrayList<FightSnapshot.EquipRow>();
		ArrayList<FightSnapshot.StatusRow> statusRows = new ArrayList<FightSnapshot.StatusRow>();
		LinkedHashSet<String> mobIds = new LinkedHashSet<String>();
		ArrayList<FightSnapshot.MobRow> mobRows = new ArrayList<FightSnapshot.MobRow>();
		double partyDamageDealt = 0, partyDamageTaken = 0;

		for (UUID uuid : s.getParty().keySet()) {
			PlayerFightData pdata = userData.remove(uuid);
			PlayerSessionData data = pdata.getSessionData();
			Player p = pdata.getPlayer();
			if (pdata != null) {
				data.getSessionStats().aggregate(pdata.getStats(), this);
				if (recordAnalytics) {
					FightStatistics fs = pdata.getStats();
					partyDamageDealt += fs.getTotalDamageDealt();
					partyDamageTaken += fs.getTotalDamageTaken();
					mobIds.addAll(fs.getDamageTaken().keySet());
					String playerClass = data.getPlayerClass() != null ? data.getPlayerClass().name() : "UNKNOWN";
					for (Entry<String, HashMap<DamageType, Double>> mobEnt : fs.getDamageTaken().entrySet()) {
						HashMap<DamageType, Double> byType = mobEnt.getValue();
						double mobTotal = 0;
						for (double amt : byType.values()) mobTotal += amt;
						if (mobTotal <= 0) continue;
						mobRows.add(new FightSnapshot.MobRow(mobEnt.getKey(), uuid.toString(), playerClass, mobTotal,
								new HashMap<DamageType, Double>(byType)));
					}
					gatherContributions(uuid.toString(), fs, equipRows, statusRows);
				}
				pdata.cleanup();
				if (p != null) {
					if (pdata.isDead()) {
						pdata.setDeath(false);
					}
					s.broadcast(pdata.getStats().getStatLine(Boolean.FALSE.equals(fightWon)));
					data.updateHealth();
					data.syncHealth();
					p.setFoodLevel(20);
					data.revertMaxHealth();
					data.updateCoinsBar();
					p.clearActivePotionEffects();
					p.getAttribute(Attribute.JUMP_STRENGTH)
						.removeModifier(NamespacedKey.fromString("jump", NeoRogue.inst()));
					p.getAttribute(Attribute.JUMP_STRENGTH)
						.removeModifier(NamespacedKey.fromString("withered", NeoRogue.inst()));
				}
			}
			FightData fdata = fightData.remove(uuid);
			if (fdata != null)
				fdata.cleanup();
		}

		ArrayList<FightData> toKill = new ArrayList<FightData>();
		for (FightData fd : fightData.values()) {
			if (fd == null)
				continue;
			if (fd.getInstance() != this)
				continue;
			fd.cleanup();
			if (fd.getEntity() != null)
				toKill.add(fd);
		}

		for (Corpse c : corpses) {
			c.remove();
		}

		for (UUID ind : indicators) {
			Entity ent = Bukkit.getEntity(ind);
			if (ent != null)
				ent.remove();
		}

		if (!pluginDisable) {
			for (BukkitRunnable cleanupTask : cleanupTasks) {
				cleanupTask.runTask(NeoRogue.inst());
			}

			for (BukkitTask task : tasks) {
				task.cancel();
			}
		}

		// Remove all mobs spawned for this fight, no matter how it ended (including plugin disable).
		// Use direct removal rather than lethal damage: bosses/adds can be invincible or immune to
		// generic damage (so li.damage would leave them alive), and killing them here would fire
		// MythicMobs death skills that could summon even more mobs mid-cleanup.
		for (FightData fd : toKill) {
			LivingEntity li = fd.getEntity();
			if (li == null) continue;
			li.remove();
		}

		if (recordAnalytics) {
			long now = System.currentTimeMillis();
			String mobs = String.join(",", mobIds);
			if (mobs.length() > 255) mobs = mobs.substring(0, 255);
			FightSnapshot snap = new FightSnapshot(UUID.randomUUID().toString(), now, AnalyticsManager.BALANCE_VERSION,
					s.getHost().toString(), s.getSaveSlot(), s.getRegion().getType().name(),
					s.getNode().getType().name(), s.getLevel(), s.getRegionsCompleted(), s.getParty().size(),
					s.getNotoriety(), s.isEndless(), now - startTime, fightWon, partyDamageDealt, partyDamageTaken, mobs);
			snap.equipRows.addAll(equipRows);
			snap.statusRows.addAll(statusRows);
			snap.mobRows.addAll(mobRows);
			AnalyticsManager.recordFight(snap);
		}
	}

	// Resolves a player's per-equipment contributions into denormalized analytics rows. Variant keys
	// that do not resolve to a registered Equipment (status-driven damage, misc sources) are dropped.
	private void gatherContributions(String playerUuid, FightStatistics fs,
			ArrayList<FightSnapshot.EquipRow> equipRows, ArrayList<FightSnapshot.StatusRow> statusRows) {
		for (EquipmentContribution ec : fs.exportContributions().values()) {
			if (!ec.hasContribution()) continue;
			boolean upgraded = ec.variantKey.endsWith("+");
			String baseId = upgraded ? ec.variantKey.substring(0, ec.variantKey.length() - 1) : ec.variantKey;
			Equipment eq = Equipment.get(baseId, upgraded);
			if (eq == null) continue; // Non-equipment source
			String rarity = eq.getRarity() != null ? eq.getRarity().name() : "NONE";
			String type = eq.getType() != null ? eq.getType().name() : "NONE";
			String eclass = joinEquipmentClasses(eq);
			equipRows.add(new FightSnapshot.EquipRow(playerUuid, baseId, upgraded, rarity, type, eclass,
					ec.damageDealt, ec.damageBuffAdded, ec.damageMitigated, ec.shieldsApplied, ec.healingDone,
					ec.statusTotal()));
			for (Entry<Status.StatusType, Integer> sEnt : ec.statuses.entrySet()) {
				if (sEnt.getValue() == 0) continue;
				statusRows.add(new FightSnapshot.StatusRow(playerUuid, baseId, upgraded, sEnt.getKey(), sEnt.getValue()));
			}
		}
	}

	private static String joinEquipmentClasses(Equipment eq) {
		if (eq.getEquipmentClasses() == null || eq.getEquipmentClasses().length == 0) return "NONE";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < eq.getEquipmentClasses().length; i++) {
			if (i > 0) sb.append(",");
			sb.append(eq.getEquipmentClasses()[i].name());
		}
		return sb.toString();
	}
	
	public static FightData removeFightData(UUID uuid) {
		FightData fd = fightData.remove(uuid);
		if (fd == null) return null;
		if (fd.getInstance() == null) return fd;
		return fd;
	}
	
	public void addToTickList(UUID uuid) {
		if (isIteratingToTick) {
			pendingToTick.add(uuid);
		} else {
			toTick.add(uuid);
		}
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
	
	protected double getInitialSpawnBudget() {
		double rngBonus = NeoRogue.gen.nextDouble(-1, 1);
		return rngBonus + (map.getEffectiveSize() / 2) + 2;
	}
	
	// Returns attempted - actual spawns
	protected double activateSpawner(double value) {
		double current = 0;
		if (spawners.isEmpty())
			return value;
		if (value <= 0)
			return value;
		int spawnCount = 0;
		while (current < value) {
			MapSpawnerInstance spawner = spawners.get(NeoRogue.gen.nextInt(spawners.size()));
			if (!spawner.canSpawn()) {
				spawner = unlimitedSpawners.get(NeoRogue.gen.nextInt(unlimitedSpawners.size()));
			}
			spawner.spawnMob();
			current += spawner.getMob().getSpawnValue();
			spawnCount++;
		}
		if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] activateSpawner: budget=" + value
				+ " spent=" + current + " spawnCalls=" + spawnCount + " leftover=" + (value - current));
		return value - current;
	}
	
	@Override
	public String serialize(HashMap<UUID, PlayerSessionData> party) {
		Bukkit.getLogger().warning("[NeoRogue] FightInstance attempted to save, this should never happen");
		return null;
	}
	
	public abstract String serializeInstanceData();

	// Separates the mob modifier segment from the map segment in serialized instance data.
	private static final String MODIFIER_DELIMITER = "@@";

	// Rolls this fight's mob modifier if the notoriety setting is active. Only called on fresh
	// creation (never on deserialization), so an existing assignment is preserved across relogs.
	// Standard fights pass allowBossModifiers=false to exclude boss-only modifiers.
	public void generateModifier(boolean allowBossModifiers) {
		if (!NotorietySetting.MOB_MODIFIERS.isActive(s)) return;
		modifier = MobModifier.generate(allowBossModifiers);
	}

	public MobModifier getModifier() {
		return modifier;
	}

	// Decides whether a newly spawned mob should receive this fight's modifier. The base behavior
	// (miniboss/boss) applies it to every non-normal mob. StandardFightInstance overrides this to
	// apply it to any mob with a configurable chance.
	public MobModifier rollModifierForMob(Mob mob) {
		if (modifier == null || mob == null) return null;
		if (mob.getType() == Mob.MobType.NORMAL) return null;
		return modifier;
	}

	// The modifier to preview for a mob in the Fight Info inventory. Base behavior mirrors
	// rollModifierForMob (non-normal mobs); StandardFightInstance shows it on all mobs since any of
	// them may randomly spawn with it.
	public MobModifier getDisplayModifier(Mob mob) {
		if (modifier == null || mob == null) return null;
		return mob.getType() == Mob.MobType.NORMAL ? null : modifier;
	}

	// Restores the fight's modifier from serialized data. Unknown/blank ids leave it unset.
	protected void loadModifier(String data) {
		if (data == null || data.isBlank()) return;
		modifier = MobModifier.get(data);
	}

	// Prefixes the map serialization with the modifier segment.
	protected String serializeWithModifier(String prefix) {
		return prefix + (modifier != null ? modifier.getId() : "") + MODIFIER_DELIMITER + map.serialize();
	}

	public static FightInstance deserializeInstanceData(Session s, HashMap<UUID, PlayerSessionData> party, String str) {
		try {
			int colon = str.indexOf(':');
			String type = str.substring(0, colon);
			String body = str.substring(colon + 1);

			// Split off the modifier segment if present (absent in older saves)
			String modData = null, mapData = body;
			int idx = body.indexOf(MODIFIER_DELIMITER);
			if (idx >= 0) {
				modData = body.substring(0, idx);
				mapData = body.substring(idx + MODIFIER_DELIMITER.length());
			}

			FightInstance inst;
			if (type.equals("STANDARD")) {
				inst = StandardFightInstance.create(s, party.keySet(), Map.deserialize(mapData));
			} else if (type.equals("MINIBOSS")) {
				inst = new MinibossFightInstance(s, party.keySet(), Map.deserialize(mapData));
			} else {
				inst = new BossFightInstance(s, party.keySet(), Map.deserialize(mapData));
			}
			inst.loadModifier(modData);
			return inst;
		}
		catch (Exception e) {
			Bukkit.getLogger().severe("[NeoRogue] Failed to deserialize FightInstance data: " + str);
			e.printStackTrace();
			return null;
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
			
		double mhealth = mob.getMaxHealthScale(s);
		am.getEntity().setMaxHealth(mhealth);
		am.getEntity().setHealth(mhealth);
		am.setDespawnMode(DespawnMode.NEVER);
		return am;
	}

	public void addBar(BossBar bar) {
		bars.add(bar);
	}

	public ArrayList<BossBar> getBars() {
		return bars;
	}

	public static void terraformGrass(Location center, int radius) {
		World w = center.getWorld();
		int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
		int radiusSq = radius * radius;
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (x * x + z * z > radiusSq) continue;
				for (int y = -radius; y <= radius; y++) {
					org.bukkit.block.Block b = w.getBlockAt(cx + x, cy + y, cz + z);
					if (!b.getType().isOccluding()) continue;
					org.bukkit.block.Block above = b.getRelative(org.bukkit.block.BlockFace.UP);
					if (above.getType().isAir()) {
						b.setType(Material.GRASS_BLOCK);
					} else {
						b.setType(Material.DIRT);
					}
				}
			}
		}
	}

	public static boolean isOnGrass(LivingEntity entity) {
		Location loc = entity.getLocation();
		org.bukkit.block.Block below = loc.getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
		if (below.getType() == Material.GRASS_BLOCK) return true;
		// Also check the block at feet level (entity standing inside grass block)
		return loc.getBlock().getType() == Material.GRASS_BLOCK;
	}

	// Returns the location of the given player's corpse if they are currently downed, else null.
	public Location getCorpseLocation(UUID uuid) {
		for (Corpse c : corpses) {
			if (c.data.getUniqueId().equals(uuid)) {
				return c.corpseDisplay.getLocation();
			}
		}
		return null;
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
