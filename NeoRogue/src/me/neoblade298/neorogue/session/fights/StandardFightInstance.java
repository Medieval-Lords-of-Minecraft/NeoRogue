package me.neoblade298.neorogue.session.fights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.CoinsReward;
import me.neoblade298.neorogue.session.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.Reward;
import me.neoblade298.neorogue.session.RewardInstance;
import me.neoblade298.neorogue.session.Session;

public class StandardFightInstance extends FightInstance {
	private static final double SCORE_REQUIRED = 25;
	
	private BossBar timeBar, scoreBar;
	private double time, score;
	private FightScore fightScore = FightScore.S;

	public StandardFightInstance(Session s) {
		super(s);
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
	
	public void handleMobKill(String id) {
		Mob mob = Mob.get(id);
		if (mob == null) {
			Bukkit.getLogger().warning("[NeoRogue] Failed to find meta-info for mob " + id + " to handle mob kill");
			return;
		}
		
		score += mob.getValue();
		if (score >= SCORE_REQUIRED) {
			timeBar.removeAll();
			scoreBar.removeAll();
			s.setInstance(new RewardInstance(generateRewards()));
			return;
		}
		scoreBar.setProgress(Math.min(1, score / SCORE_REQUIRED));
	}
	
	private HashMap<UUID, ArrayList<Reward>> generateRewards() {
		HashMap<UUID, ArrayList<Reward>> rewards = new HashMap<UUID, ArrayList<Reward>>();
		for (UUID uuid : s.getParty().keySet()) {
			PlayerSessionData data = s.getParty().get(uuid);
			ArrayList<Reward> list = new ArrayList<Reward>();
			list.add(new CoinsReward(fightScore.getCoins()));
			
			ArrayList<Equipment> equipDrops = new ArrayList<Equipment>();
			EquipmentClass ec = data.getPlayerClass().toEquipmentClass();
			int value = s.getAreasCompleted();
			switch (fightScore) {
			case S:
				equipDrops.add(Equipment.getDrop(ec, value + 2));
				equipDrops.add(Equipment.getDrop(ec, value + 1));
				equipDrops.add(Equipment.getDrop(ec, value + 1));
				equipDrops.add(Equipment.getDrop(ec, value));
				break;
			case A:
				equipDrops.add(Equipment.getDrop(ec, value + 1));
				equipDrops.add(Equipment.getDrop(ec, value + 1));
				equipDrops.add(Equipment.getDrop(ec, value));
				break;
			case B:
				equipDrops.add(Equipment.getDrop(ec, value + 1));
				equipDrops.add(Equipment.getDrop(ec, value));
				equipDrops.add(Equipment.getDrop(ec, value));
				break;
			case C:
				equipDrops.add(Equipment.getDrop(ec, value));
				equipDrops.add(Equipment.getDrop(ec, value));
				equipDrops.add(Equipment.getDrop(ec, value));
				break;
			case D:
				equipDrops.add(Equipment.getDrop(ec, value));
				equipDrops.add(Equipment.getDrop(ec, value));
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
		s.broadcast("&7You completed the fight with a score of " + fightScore.getDisplay() + "&7!");
	}
}
