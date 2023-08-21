package me.neoblade298.neorogue.session.fights;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;

public class StandardFightInstance extends FightInstance {
	private static final double FIGHT_TIME = 180;
	
	private BossBar timeBar, scoreBar;
	private double timeRemaining, score;
	private FightScore fightScore = FightScore.D;

	public StandardFightInstance(Session s) {
		super(s);
	}

	@Override
	protected void setupInstance(Session s) {
		timeBar = Bukkit.createBossBar("Time Remaining", BarColor.WHITE, BarStyle.SOLID);
		scoreBar = Bukkit.createBossBar("Current Rating: " + fightScore.getDisplay(), BarColor.RED, BarStyle.SEGMENTED_6);
		scoreBar.setProgress(0);
		
		for (Player p : s.getOnlinePlayers()) {
			timeBar.addPlayer(p);
			scoreBar.addPlayer(p);
		}
		
		timeRemaining = FIGHT_TIME;
		
		tasks.add(new BukkitRunnable() {
			public void run() {
				timeRemaining--;
				
				if (timeRemaining <= 0) {
					this.cancel();
				}
				else {
					timeBar.setProgress(timeRemaining / FIGHT_TIME);
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
		
		if (fightScore != FightScore.S) {
			scoreBar.setProgress(score / fightScore.getThreshold());
		}
		
		if (score >= fightScore.getThreshold()) {
			score -= fightScore.getThreshold();
			fightScore = fightScore.getNext();
			if (fightScore == FightScore.S) {
				scoreBar.setProgress(1);
			}
			else {
				scoreBar.setProgress(score / fightScore.getThreshold());
			}
			scoreBar.setTitle("Current Score: " + fightScore.getDisplay());
			for (Player p : s.getOnlinePlayers()) {
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
			}
			s.broadcast("&7Your fight rating increased to " + fightScore.getDisplay() + "&7!");
		}
	}
}
