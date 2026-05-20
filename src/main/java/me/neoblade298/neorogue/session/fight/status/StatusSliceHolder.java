package me.neoblade298.neorogue.session.fight.status;

import java.util.HashMap;
import java.util.LinkedList;

import me.neoblade298.neorogue.session.fight.FightData;

public class StatusSliceHolder {
	private LinkedList<StatusSlice> slices = new LinkedList<StatusSlice>();
	private HashMap<FightData, Integer> sliceOwners = new HashMap<FightData, Integer>();
	
	public void add(FightData applier, int amount) {
		StatusSlice last = slices.peekLast();
		if (last != null && last.getUniqueId().equals(applier.getUniqueId())) {
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
			FightData fd = slice.getFightData();
			if (stacks > amount) {
				slice.addStacks(-amount);
				sliceOwners.put(fd, sliceOwners.get(fd) - amount);
				amount = 0;
			}
			else {
				sliceOwners.put(fd, sliceOwners.get(fd) - stacks);
				amount -= stacks;
				if (front) slices.removeFirst();
				else slices.removeLast();
			}
		}
	}
	
	public StatusSlice first() {
		return slices.getFirst();
	}
	
	public HashMap<FightData, Integer> getSliceOwners() {
		return sliceOwners;
	}
}
