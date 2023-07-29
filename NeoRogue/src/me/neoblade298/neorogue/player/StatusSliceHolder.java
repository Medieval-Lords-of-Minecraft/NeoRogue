package me.neoblade298.neorogue.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class StatusSliceHolder {
	private LinkedList<StatusSlice> slices = new LinkedList<StatusSlice>();
	private HashMap<UUID, Integer> sliceOwners = new HashMap<UUID, Integer>();
	
	public void add(UUID applier, int amount) {
		StatusSlice last = slices.peekLast();
		if (last != null && last.getUniqueId().equals(applier)) {
			last.addStacks(amount);
		}
		else {
			slices.push(new StatusSlice(applier, amount));
		}

		sliceOwners.put(applier, sliceOwners.getOrDefault(applier, 0) + amount);
	}
	
	public void remove(int amount) {
		remove(amount, true);
	}
	
	public void remove(int amount, boolean front) {
		while (amount > 0) {
			StatusSlice slice = front ? slices.getFirst() : slices.getLast();
			int stacks = slice.getStacks();
			UUID uuid = slice.getUniqueId();
			if (stacks > amount) {
				slice.addStacks(-amount);
				sliceOwners.put(uuid, sliceOwners.get(uuid) - amount);
				amount = 0;
			}
			else {
				sliceOwners.put(uuid, sliceOwners.get(uuid) - stacks);
				amount -= stacks;
				if (front) slices.removeFirst();
				else slices.removeLast();
			}
		}
	}
	
	public HashMap<UUID, Integer> getSliceOwners() {
		return sliceOwners;
	}
}
