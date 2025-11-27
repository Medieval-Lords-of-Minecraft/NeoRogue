package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.area.NodeType;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldShard;
import me.neoblade298.neorogue.equipment.artifacts.RubyShard;
import me.neoblade298.neorogue.equipment.artifacts.SapphireShard;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.reward.CoinsReward;
import me.neoblade298.neorogue.session.reward.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.reward.EquipmentReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StandardFightInstance extends FightInstance {
	
	private static final HashMap<Integer, Double> SCORE_REQUIRED = new HashMap<Integer, Double>();

	private BossBar timeBar, scoreBar;
	private double time, score, scoreRequired;
	private FightScore fightScore = FightScore.S;
	private boolean isActive = true;

	static {
		SCORE_REQUIRED.put(1, 15D);
		SCORE_REQUIRED.put(2, 25D);
		SCORE_REQUIRED.put(3, 32D);
		SCORE_REQUIRED.put(4, 40D);
	}

	public StandardFightInstance(Session s, Set<UUID> players, AreaType type, int nodesVisited) {
		super(s, players);
		/*
		 * Currently scrapped due to map sizes being unnecessarily large
		 * double rand = NeoRogue.gen.nextDouble((nodesVisited + 1) / 12.0);
		 * double min = 2 + nodesVisited / 12.0;
		 * int max = (int) Math.min(rand + min, 6);
		 */
		map = Map.generate(type, NeoRogue.gen.nextInt(3, 6));
	}

	public StandardFightInstance(Session s, Set<UUID> players, Map map) {
		super(s, players);
		this.map = map;
	}
	
	@Override
	protected void setupInstance(Session s) {
		scoreRequired = SCORE_REQUIRED.getOrDefault(s.getParty().size(), SCORE_REQUIRED.get(4));
		scoreBar = Bukkit.createBossBar("Objective: Kill Enemies", BarColor.RED, BarStyle.SEGMENTED_6);
		timeBar = Bukkit.createBossBar("Current Rating: " + fightScore.getDisplay(), BarColor.WHITE, BarStyle.SOLID);
		timeBar.addFlag(BarFlag.CREATE_FOG);
		bars.add(scoreBar);
		bars.add(timeBar);
		scoreBar.setProgress(0);

		for (Player p : s.getOnlinePlayers()) {
			timeBar.addPlayer(p);
			scoreBar.addPlayer(p);
		}

		for (UUID uuid : s.getSpectators().keySet()) {
			Player p = Bukkit.getPlayer(uuid);
			timeBar.addPlayer(p);
			scoreBar.addPlayer(p);
		}

		tasks.add(new BukkitRunnable() {
			@Override
			public void run() {
				time++;
				timeBar.setProgress(time / ((1 - s.getFightTimeReduction()) * fightScore.getThreshold()));

				if (time >= fightScore.getThreshold()) {
					if (fightScore.getNext() == null) {
						this.cancel();
					} else {
						fightScore = fightScore.getNext();
						time = 0;
						timeBar.setTitle("Current Rating: " + fightScore.getDisplay());
					}
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
	}

	@Override
	public void handleMobKill(String id, boolean playerKill) {
		Mob mob = Mob.get(id);
		if (mob == null)
			return;
		if (!playerKill)
			return;

		if (s.getInstance() != this)
			return; // If we've moved on to reward instance don't spam the user
			
		score += mob.getKillValue();
		scoreBar.setProgress(Math.min(1, score / scoreRequired));
		if (score >= scoreRequired) {
			// Have a time lag so the ability that ended the fight has time to complete
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!isActive)
						return;
					isActive = false;
					FightInstance.handleWin();
					timeBar.removeAll();
					scoreBar.removeAll();
					s.broadcast(
							Component.text("You completed the fight with a score of ", NamedTextColor.GRAY)
									.append(fightScore.getComponentDisplay()).append(Component.text("!"))
					);
					s.setInstance(new RewardInstance(s, generateRewards(s, fightScore), NodeType.FIGHT));
				}
			}.runTask(NeoRogue.inst());
			return;
		}
	}

	public static HashMap<UUID, ArrayList<Reward>> generateRewards(Session s, FightScore fightScore) {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		boolean dropPotion = false;
		if (NeoRogue.gen.nextInt(100) < s.getPotionChance()) {
			s.addPotionChance(-25);
			dropPotion = true;
		} else {
			s.addPotionChance(10);
		}
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			RewardFightEvent ev = new RewardFightEvent(NodeType.FIGHT);
			data.trigger(SessionTrigger.REWARD_FIGHT, ev);
			list.add(new CoinsReward((int) ((1 - s.getGoldReduction()) * fightScore.getCoins()) + ev.getBonusGold()));

			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getBaseDropValue() + ev.getBonusRarity();
			switch (fightScore) {
			case S:
				equipDrops.addAll(Equipment.getDrop(value + 1, 2, ec, EquipmentClass.CLASSLESS));
				equipDrops.addAll(Equipment.getDrop(value, 2 + ev.getBonusEquipment(), equipDrops, ec, EquipmentClass.CLASSLESS));
				break;
			case A:
				equipDrops.addAll(Equipment.getDrop(value + 1, 1, ec, EquipmentClass.CLASSLESS));
				equipDrops.addAll(Equipment.getDrop(value, 3 + ev.getBonusEquipment(), equipDrops, ec, EquipmentClass.CLASSLESS));
				break;
			case B:
				equipDrops.add(Equipment.getDrop(value + 1, ec, EquipmentClass.CLASSLESS));
				equipDrops.addAll(Equipment.getDrop(value, 2 + ev.getBonusEquipment(), equipDrops, ec, EquipmentClass.CLASSLESS));
				break;
			case C:
				equipDrops.addAll(Equipment.getDrop(value, 3 + ev.getBonusEquipment(), ec, EquipmentClass.CLASSLESS));
				break;
			case D:
				equipDrops.addAll(Equipment.getDrop(value, 2 + ev.getBonusEquipment(), ec, EquipmentClass.CLASSLESS));
				break;
			}

			s.rollUpgrades(equipDrops, fightScore.getUpgradeModifier() + ev.getBonusUpgradeChance());
			list.add(new EquipmentChoiceReward(equipDrops));
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(RubyShard.get());
			equipDrops.add(EmeraldShard.get());
			equipDrops.add(SapphireShard.get());
			list.add(new EquipmentChoiceReward(equipDrops));
			if (dropPotion) {
				Consumable cons = Equipment.getConsumable(value, ec, EquipmentClass.CLASSLESS);
				list.add(new EquipmentReward(s.rollUpgrade(cons, fightScore.getUpgradeModifier())));
			}

			rewards.put(uuid, list);
		}
		return rewards;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		timeBar.removeAll();
		scoreBar.removeAll();
	}

	@Override
	public String serializeInstanceData() {
		return "STANDARD:" + map.serialize();
	}
	
	@Override
	public void addSpectator(Player p) {
		timeBar.addPlayer(p);
		scoreBar.addPlayer(p);
	}
	
	@Override
	public void removeSpectator(Player p) {
		timeBar.removePlayer(p);
		scoreBar.removePlayer(p);
	}
	
	@Override
	public void handlePlayerLeaveParty(Player p) {

	}

	@Override
	public void updateBoardLines() {

	}
}
