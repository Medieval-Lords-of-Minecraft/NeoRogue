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
import me.neoblade298.neorogue.session.reward.CoinsReward;
import me.neoblade298.neorogue.session.reward.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StandardFightInstance extends FightInstance {

	private static final double SCORE_REQUIRED = 25;
	
	private BossBar timeBar, scoreBar;
	private double time, score;
	private FightScore fightScore = FightScore.S;
	
	public StandardFightInstance(Set<UUID> players, AreaType type, int nodesVisited) {
		super(players);
		int rand = nodesVisited >= 5 ? NeoRogue.gen.nextInt(nodesVisited / 5) : 0;
		int min = nodesVisited / 10;
		map = Map.generate(type, 2 + rand + min);
	}
	
	public StandardFightInstance(Set<UUID> players, Map map) {
		super(players);
		this.map = map;
	}

	@Override
	protected void setupInstance(Session s) {
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
		if (mob == null) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to find meta-info for mob " + id + " to handle mob kill");
			return;
		}
		
		if (s.getInstance() != this) return; // If we've moved on to reward instance don't spam the user
		
		score += mob.getValue();
		scoreBar.setProgress(Math.min(1, score / SCORE_REQUIRED));
		if (score >= SCORE_REQUIRED) {
			FightInstance.handleWin();
			timeBar.removeAll();
			scoreBar.removeAll();
			s.broadcast(Component.text("You completed the fight with a score of ", NamedTextColor.GRAY)
					.append(fightScore.getComponentDisplay()).append(Component.text("!")));
			s.setInstance(new RewardInstance(generateRewards(s, fightScore)));
			return;
		}
	}
	
	public static HashMap<UUID, ArrayList<Reward>> generateRewards(Session s, FightScore fightScore) {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			list.add(new CoinsReward(fightScore.getCoins()));
			
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
			equipDrops.add(Equipment.get("rubyShard", fightScore == FightScore.S));
			equipDrops.add(Equipment.get("emeraldShard", fightScore == FightScore.S));
			equipDrops.add(Equipment.get("sapphireShard", fightScore == FightScore.S));
			list.add(new EquipmentChoiceReward(equipDrops));
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
