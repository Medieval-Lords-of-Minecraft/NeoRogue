package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldShard;
import me.neoblade298.neorogue.equipment.artifacts.RubyShard;
import me.neoblade298.neorogue.equipment.artifacts.SapphireShard;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.NodeType;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.region.RegionType.Layout;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardBuilder;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class StandardFightInstance extends FightInstance {
	private static final int KILLS_TO_SCALE = 7; // number of mobs to kill before increasing total mobs by 1
	private static final HashMap<Integer, Double> SCORE_REQUIRED = new HashMap<Integer, Double>();

	// Chance for each mob in a standard fight to spawn with the fight's modifier. Configurable.
	public static double MODIFIER_SPAWN_CHANCE = 0.2;

	protected double totalKillValue; // Keeps track of total mob spawns, to handle scaling of spawning
	private BossBar timeBar, scoreBar;
	private double time, score;
	protected double scoreRequired;
	private FightScore fightScore = FightScore.S;

	static {
		SCORE_REQUIRED.put(1, 15D);
		SCORE_REQUIRED.put(2, 25D);
		SCORE_REQUIRED.put(3, 32D);
		SCORE_REQUIRED.put(4, 40D);
	}

	public StandardFightInstance(Session s, Set<UUID> players, RegionType type, int nodesVisited) {
		super(s, players);
		/*
		 * Currently scrapped due to map sizes being unnecessarily large
		 * double rand = NeoRogue.gen.nextDouble((nodesVisited + 1) / 12.0);
		 * double min = 2 + nodesVisited / 12.0;
		 * int max = (int) Math.min(rand + min, 6);
		 */
		map = Map.generate(type, NeoRogue.gen.nextInt(3, 6), s.isDebug());
		generateModifier(false);
	}

	public StandardFightInstance(Session s, Set<UUID> players, Map map) {
		super(s, players);
		this.map = map;
	}

	public static StandardFightInstance create(Session s, Set<UUID> players, RegionType type, int nodesVisited) {
		if (s.getRegion().getType().getLayout() == Layout.TUTORIAL) {
			return new TutorialFightInstance(s, players, type, nodesVisited);
		}
		return new StandardFightInstance(s, players, type, nodesVisited);
	}

	public static StandardFightInstance create(Session s, Set<UUID> players, Map map) {
		if (s.getRegion().getType().getLayout() == Layout.TUTORIAL) {
			return new TutorialFightInstance(s, players, map);
		}
		return new StandardFightInstance(s, players, map);
	}

	@Override
	public FightScore getFightScore() {
		return fightScore;
	}
	
	@Override
	protected void setupInstance(Session s) {
		scoreRequired = SCORE_REQUIRED.getOrDefault(s.getParty().size(), SCORE_REQUIRED.get(4));
		scoreBar = BossBar.bossBar(Component.text("Objective: Kill Enemies"), 0f, BossBar.Color.RED, BossBar.Overlay.NOTCHED_6);
		timeBar = BossBar.bossBar(Component.text("Current Rating: ").append(fightScore.getComponentDisplay()),
				0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
		timeBar.addFlag(BossBar.Flag.CREATE_WORLD_FOG);
		bars.add(scoreBar);
		bars.add(timeBar);

		for (Player p : s.getOnlinePlayers()) {
			p.showBossBar(timeBar);
			p.showBossBar(scoreBar);
		}

		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				p.showBossBar(timeBar);
				p.showBossBar(scoreBar);
			}
		}

		tasks.add(new BukkitRunnable() {
			@Override
			public void run() {
				time++;
				double fightTimeMult = (s.getRegionsCompleted() > 0 && NotorietySetting.REDUCED_SCORE_THRESHOLDS.isActive(s))
						? NotorietySetting.SCORE_THRESHOLD_MULTIPLIER : 1;
				timeBar.progress((float) (time / (fightScore.getThreshold() * fightTimeMult)));

				if (time >= fightScore.getThreshold() * fightTimeMult) {
					if (fightScore.getNext() == null) {
						this.cancel();
					} else {
						fightScore = fightScore.getNext();
						time = 0;
						timeBar.name(Component.text("Current Rating: ").append(fightScore.getComponentDisplay()));
					}
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 60L, 20L));
	}

	@Override
	public void handleMobKill(FightData fd, String id, boolean playerKill) {
		Mob mob = Mob.get(id);
		if (mob == null)
			return;

		if (!isActive)
			return; // If we've moved on to reward instance don't spam the user

		// This is for despawned mobs that need to be respawned (and Fanatics)
		if (!playerKill) {
			respawnMob(fd, id, true, false);
			return;
		}
			
		score += mob.getKillValue();
		scoreBar.progress((float) Math.min(1, score / scoreRequired));
		if (score >= scoreRequired) {
			hideBarFromAll(timeBar);
			hideBarFromAll(scoreBar);
			s.awardXp(fightScore.getXp());
			Title title = Title.title(Component.text("Victory"),
				Component.text("Your ranking: ").append(fightScore.getComponentDisplay()));
			handleWin(title, new RewardInstance(s, generateRewards(s, fightScore), NodeType.FIGHT));
			return;
		}
		
		respawnMob(fd, id, false, playerKill);
	}

	@Override
	public void handleMobDespawn(FightData fd, String id, boolean despawn, boolean playerKill) {
		respawnMob(fd, id, true, false);
	}
	
	private void respawnMob(FightData data, String id, boolean isDespawn, boolean playerKill) {
		Mob mob = Mob.get(id);
		if (mob == null)
			return;

		if (!isActive)
			return;
		
		if (NeoRogue.isDebugFlag("spawns")) Bukkit.getLogger().info("[NeoRogue Spawn] respawnMob: mob=" + id + " isDespawn=" + isDespawn
				+ " playerKill=" + playerKill + " killValue=" + mob.getKillValue()
				+ " spawnCounter=" + spawnCounter + " totalKillValue=" + totalKillValue);
		
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

	private HashMap<UUID, ArrayList<Reward>> generateRewards(Session s, FightScore fightScore) {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		boolean dropPotion = s.rollPotionChance(10);
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			RewardBuilder rb = new RewardBuilder(s, data, NodeType.FIGHT);
			int value = rb.getBaseValue();

			rb.coins(fightScore.getCoins());

			switch (fightScore) {
			case S:
				rb.equipmentDropsRaw(value + 1, 2);
				rb.equipmentDrops(value, 2, rb.getEquipDrops());
				break;
			case A:
				rb.equipmentDropsRaw(value + 1, 1);
				rb.equipmentDrops(value, 3, rb.getEquipDrops());
				break;
			case B:
				rb.equipmentDropsRaw(value + 1, 1);
				rb.equipmentDrops(value, 2, rb.getEquipDrops());
				break;
			case C:
				rb.equipmentDrops(value, 3);
				break;
			case D:
				rb.equipmentDrops(value, 2);
				break;
			}

			rb.upgradeDrops(fightScore.getUpgradeModifier());
			rb.gems(RubyShard.get(), SapphireShard.get(), EmeraldShard.get());
			if (dropPotion) {
				rb.consumable(value, fightScore.getUpgradeModifier());
			}

			rewards.put(uuid, rb.build());
		}
		return rewards;
	}

	@Override
	public void cleanup(boolean pluginDisable) {
		super.cleanup(pluginDisable);
		hideBarFromAll(timeBar);
		hideBarFromAll(scoreBar);
	}

	@Override
	public String serializeInstanceData() {
		return serializeWithModifier("STANDARD:");
	}

	// Standard fights apply the modifier to any spawned mob with a fixed chance, rather than to all
	// non-normal mobs (of which standard fights have none).
	@Override
	public MobModifier rollModifierForMob(Mob mob) {
		if (modifier == null || mob == null) return null;
		return NeoRogue.gen.nextDouble() < MODIFIER_SPAWN_CHANCE ? modifier : null;
	}

	// Any mob in a standard fight may spawn with the modifier, so preview it on all of them.
	@Override
	public MobModifier getDisplayModifier(Mob mob) {
		return modifier;
	}

	@Override
	public void updateBoardLines() {

	}
}
