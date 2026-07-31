package me.neoblade298.neorogue.session.fight;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;

public abstract class Marker {
	protected PlayerFightData owner;
	private final Equipment sourceEquipment;
	protected UUID uuid;
	protected Location loc;
	protected BukkitTask task;
	protected String taskId;
	protected int durationTicks, tickPeriod;

	public Marker(PlayerFightData owner, Location loc, int durationTicks) {
		this(owner, loc, durationTicks, 20, null);
	}

	public Marker(PlayerFightData owner, Location loc, int durationTicks, Equipment sourceEquipment) {
		this(owner, loc, durationTicks, 20, sourceEquipment);
	}

	public Marker(PlayerFightData owner, Location loc, int durationTicks, int tickPeriod) {
		this(owner, loc, durationTicks, tickPeriod, null);
	}

	public Marker(PlayerFightData owner, Location loc, int durationTicks, int tickPeriod, Equipment sourceEquipment) {
		this.uuid = UUID.randomUUID();
		this.loc = loc;
		this.owner = owner;
		this.durationTicks = durationTicks;
		this.tickPeriod = tickPeriod;
		this.sourceEquipment = sourceEquipment;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Equipment getSourceEquipment() {
		return sourceEquipment;
	}

	public Location getLocation() {
		return loc;
	}

	public void setLocation(Location loc) {
		this.loc = loc;
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
					cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0, tickPeriod);
		taskId = UUID.randomUUID().toString();
		owner.addTask(taskId, task);	
	}

	public void deactivate() {
		if (task != null) {
			task = null;
			owner.removeAndCancelTask(taskId);
			onDeactivate();
			owner.removeMarker(this);
		}
	}
	
	public abstract void tick();
	public abstract void onDeactivate();
}
