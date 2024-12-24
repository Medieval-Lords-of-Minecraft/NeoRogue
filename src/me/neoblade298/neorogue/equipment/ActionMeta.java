package me.neoblade298.neorogue.equipment;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

// For most common use cases and storing of data in instance format
public class ActionMeta {
	private long time;
	private boolean bool;
	private int count;
	private LivingEntity ent;
	private Location loc;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addCount(int count) {
		this.count += count;
	}

	public void setEntity(LivingEntity ent) {
		this.ent = ent;
	}

	public LivingEntity getEntity() {
		return ent;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public boolean getBool() {
		return bool;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}

	public Location getLocation() {
		return loc;
	}

	public void setLocation(Location loc) {
		this.loc = loc;
	}
}
