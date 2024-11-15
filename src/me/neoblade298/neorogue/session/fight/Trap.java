package me.neoblade298.neorogue.session.fight;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class Trap {
	private PlayerFightData owner;
	private UUID uuid;
	private Location loc;
	private BukkitTask task;
	private String taskId;
	private int durationTicks, tickPeriod;

	public Trap(PlayerFightData owner, Location loc, int durationTicks) {
		this(owner, loc, durationTicks, 20);
	}


	public Trap(PlayerFightData owner, Location loc, int durationTicks, int tickPeriod) {
		this.uuid = UUID.randomUUID();
		this.loc = loc;
		this.owner = owner;
		this.durationTicks = durationTicks;
		this.tickPeriod = tickPeriod;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Location getLocation() {
		return loc;
	}

	public int getDuration() {
		return durationTicks;
	}
	
	public void setDuration(int durationTicks) {
		this.durationTicks = durationTicks;
	}

	public boolean isActive() {
		return task != null && !task.isCancelled();
	}

	public void activate() {
		task = new BukkitRunnable() {
			private int tick;
			@Override
			public void run() {
				tick();
				if (durationTicks > 0 && ++tick * tickPeriod >= durationTicks) {
					deactivate();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0, tickPeriod);
		taskId = UUID.randomUUID().toString();
		owner.addTask(taskId, task);	
	}

	public void deactivate() {
		if (task != null) {
			task.cancel();
			owner.removeTask(taskId);
			owner.runActions(owner, Trigger.LAY_TRAP, null);
			task = null;
		}
	}
	
	public abstract void tick();
}
