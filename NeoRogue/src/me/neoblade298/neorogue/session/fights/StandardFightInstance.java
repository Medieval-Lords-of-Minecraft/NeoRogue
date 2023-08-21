package me.neoblade298.neorogue.session.fights;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.Session;

public class StandardFightInstance extends FightInstance {
	private BossBar timeBar, scoreBar;
	private static final double FIGHT_TIME = 180;
	private double timeRemaining, score;

	public StandardFightInstance(Session s) {
		super(s);
	}

	@Override
	protected void setupInstance(Session s) {
		timeBar = Bukkit.createBossBar("Time Remaining", BarColor.WHITE, BarStyle.SOLID);
		scoreBar = Bukkit.createBossBar("Current Score: F", BarColor.RED, BarStyle.SEGMENTED_6);
		scoreBar.setProgress(0);
		
		timeRemaining = FIGHT_TIME;
		
		tasks.add(new BukkitRunnable() {
			public void run() {
				timeRemaining--;
				
				if (timeRemaining <= 0) {
					// TODO Add reward instance
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
	}

	private void updateTimeBar() {
		timeBar.setProgress(timeRemaining / FIGHT_TIME);
	}
	
	public void handleMobKill(MythicMobDeathEvent e) {
		e.getMythicMob();
		//Mob mob = e.getMythicMob().getId()
		Mob mob = map.getMobs().get("");
		score += (double) 1 / mob.get
	}
}
