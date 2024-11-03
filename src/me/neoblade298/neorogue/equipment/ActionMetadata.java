package me.neoblade298.neorogue.equipment;

// For most common use cases and storing of data in instance format
public class ActionMetadata {
	private long time;
	private boolean bool;
	private int count;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addCount(int count) {
		this.count += count;
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
}
