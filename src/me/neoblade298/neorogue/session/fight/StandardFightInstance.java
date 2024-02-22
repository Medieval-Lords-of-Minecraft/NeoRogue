package me.neoblade298.neorogue.session.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.map.Map;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.RewardGoldEvent;
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
	
	static {
		SCORE_REQUIRED.put(1, 15D);
		SCORE_REQUIRED.put(2, 25D);
		SCORE_REQUIRED.put(3, 35D);
		SCORE_REQUIRED.put(4, 50D);
	}
	
	public StandardFightInstance(Session s, Set<UUID> players, AreaType type, int nodesVisited) {
		super(s, players);
		int rand = nodesVisited >= 5 ? NeoRogue.gen.nextInt(nodesVisited / 5) : 0;
		int min = nodesVisited / 10;
		map = Map.generate(type, 2 + rand + min);
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
		
		scoreBar.setProgress(0);
		
		for (Player p : s.getOnlinePlayers()) {
			timeBar.addPlayer(p);
			scoreBar.addPlayer(p);
		}
		
		time = fightScore.getThreshold();
		
		tasks.add(new BukkitRunnable() {
			public void run() {
				time--;
				timeBar.setProgress(time / fightScore.getThreshold());
				
				if (time <= 0) {
					if (fightScore.getNext() == null) {
						this.cancel();
					}
					else {
						fightScore = fightScore.getNext();
						time = fightScore.getThreshold();
						timeBar.setTitle("Current Rating: " + fightScore.getDisplay());
					}
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
	}
	
	@Override
	public void handleMobKill(String id) {
		Mob mob = Mob.get(id);
		if (mob == null) return;
		
		if (s.getInstance() != this) return; // If we've moved on to reward instance don't spam the user
		
		score += mob.getValue();
		scoreBar.setProgress(Math.min(1, score / scoreRequired));
		if (score >= scoreRequired) {
			FightInstance.handleWin();
			timeBar.removeAll();
			scoreBar.removeAll();
			s.broadcast(Component.text("You completed the fight with a score of ", NamedTextColor.GRAY)
					.append(fightScore.getComponentDisplay()).append(Component.text("!")));
			s.setInstance(new RewardInstance(s, generateRewards(s, fightScore)));
			return;
		}
	}
	
	public static HashMap<UUID, ArrayList<Reward>> generateRewards(Session s, FightScore fightScore) {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		boolean dropPotion = false;
		if (NeoRogue.gen.nextInt(100) < s.getPotionChance()) {
			s.addPotionChance(-25);
			dropPotion = true;
		}
		else {
			s.addPotionChance(10);
		}
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			RewardGoldEvent ev = new RewardGoldEvent(fightScore.getCoins());
			data.trigger(SessionTrigger.REWARD_GOLD, ev);
			list.add(new CoinsReward(ev.getAmount()));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass();
			int value = s.getAreasCompleted();
			switch (fightScore) {
			case S:
				equipDrops.addAll(Equipment.getDrop(value + 1, 2, ec, EquipmentClass.CLASSLESS));
				equipDrops.addAll(Equipment.getDrop(value, 2, ec, EquipmentClass.CLASSLESS));
				break;
			case A:
				equipDrops.addAll(Equipment.getDrop(value + 1, 1, ec, EquipmentClass.CLASSLESS));
				equipDrops.addAll(Equipment.getDrop(value, 3, ec, EquipmentClass.CLASSLESS));
				break;
			case B:
				equipDrops.add(Equipment.getDrop(value + 1, ec, EquipmentClass.CLASSLESS));
				equipDrops.addAll(Equipment.getDrop(value, 2, ec, EquipmentClass.CLASSLESS));
				break;
			case C:
				equipDrops.addAll(Equipment.getDrop(value, 3, ec, EquipmentClass.CLASSLESS));
				break;
			case D:
				equipDrops.addAll(Equipment.getDrop(value, 2, ec, EquipmentClass.CLASSLESS));
				break;
			}
			
			list.add(new EquipmentChoiceReward(equipDrops));
			equipDrops = new ArrayList<Equipment>(3);
			equipDrops.add(Equipment.get("rubyShard", false));
			equipDrops.add(Equipment.get("emeraldShard", false));
			equipDrops.add(Equipment.get("sapphireShard", false));
			list.add(new EquipmentChoiceReward(equipDrops));
			if (dropPotion) list.add(new EquipmentReward(Equipment.getConsumable(value, ec, EquipmentClass.CLASSLESS)));
			
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
	
	public String serializeInstanceData() {
		return "STANDARD:" + map.serialize();
	}
}
