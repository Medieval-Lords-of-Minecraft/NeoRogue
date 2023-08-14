package me.neoblade298.neorogue.session;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;

public class NodeSelectInstance implements Instance {
	private BukkitTask task;

	@Override
	public void start(Session s) {
		task = new BukkitRunnable() {
			Area area = s.getArea();
			int count = 0; 
			public void run() {
				count++;
				if (count > 20) {
					this.cancel();
				}

				for (Player p : s.getOnlinePlayers()) {
					area.tickParticles(p, area.getNodes()[1][2]);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 20L);
	}

	@Override
	public void cleanup() {
		task.cancel();
	}
}
