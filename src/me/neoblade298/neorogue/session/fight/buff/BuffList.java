package me.neoblade298.neorogue.session.fight.buff;

import java.util.LinkedList;

public class BuffList {
	// As a best practice, buffs should be immutable as soon as they are applied to a buffholder
    // You should always deepclone a buffholder's buffs
	private LinkedList<Buff> buffs = new LinkedList<Buff>();
    private double increase, mult;

	// Maybe will need another version of this for non-damage buffs if I ever make those
	public void add(Buff b) {
        for (Buff curr : buffs) {
            if (curr.isSimilar(b)) {
                if (curr.getStatTracker().shouldCombine()) {
                    curr.combine(b);
                    increase += curr.getIncrease();
                    mult += curr.getMultiplier();
                    return;
                }
                else {
                    increase = increase - curr.getIncrease() + b.getIncrease();
                    mult = mult - curr.getMultiplier() + b.getMultiplier();
                    curr.replace(b);
                    return;
                }
            }
        }

        // If an existing buff was not found, just add the new buff
        increase += b.getIncrease();
        mult += b.getMultiplier();
        buffs.add(b);
	}

    public void add(BuffList other) {
        for (Buff b : other.buffs) {
            add(b.clone());
        }
    }

    public BuffList clone() {
        BuffList clone = new BuffList();
        clone.add(this);
        return clone;
    }

    // These methods currently avoid fight stats
    public int apply(int base) {
        return (int) apply((double) base);
    }
    
    public double apply(double base) {
        double increase = 0, mult = 0;
        for (Buff b : buffs) {
            increase += b.getIncrease();
            mult += b.getMultiplier();
        }
        return (base * (1 + mult)) + increase;
    }

    public int applyNegative(int base) {
        return (int) applyNegative((double) base);
    }

    public double applyNegative(double base) {
        double increase = 0, mult = 0;
        for (Buff b : buffs) {
            increase += b.getIncrease();
            mult += b.getMultiplier();
        }
        return (base * (1 - mult)) - increase;
    }

    public double getIncrease() {
        return increase;
    }

    public double getMultiplier() {
        return mult;
    }

    public LinkedList<Buff> getBuffs() {
        return buffs;
    }

    public String toString() {
        return buffs.toString();
    }
}
