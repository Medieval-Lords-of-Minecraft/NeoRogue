package me.neoblade298.neorogue.equipment;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.session.fight.Marker;

// For most common use cases and storing of data in instance format
public class ActionMeta {
	private long time;
	private boolean bool;
	private int count;
	private double db;
	private LivingEntity ent;
	private Location loc;
	private Marker trap;
	private Object obj;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addCount(int count) {
		this.count += count;
	}

	public void addDouble(double db) {
		this.db += db;
	}

	public void setDouble(double db) {
		this.db = db;
	}

	public double getDouble() {
		return db;
	}

	public void setObject(Object obj) {
		this.obj = obj;
	}

	public Object getObject() {
		return obj;
	}

	public void setTrap(Marker trap) {
		this.trap = trap;
	}

	public Marker getTrap() {
		return trap;
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

	public void toggleBool() {
		this.bool = !bool;
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
