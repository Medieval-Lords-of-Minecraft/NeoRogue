package me.neoblade298.neorogue.session;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.Area;

public class NodeSelectInstance implements Instance {
	private Session s;
	private BukkitTask task;
	
	public NodeSelectInstance(Session s) {
		this.s = s;
	}

	@Override
	public void start(Session s) {
		Area area = s.getArea();
		area.update(s.getNode());
		task = new BukkitRunnable() {
			public void run() {
				for (Player p : s.getOnlinePlayers()) {
					area.tickParticles(p, s.getNode());
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 20L);
	}

	@Override
	public void cleanup() {
		task.cancel();
	}
}
