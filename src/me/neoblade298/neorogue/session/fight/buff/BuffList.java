package me.neoblade298.neorogue.session.fight.buff;

import java.util.LinkedList;

public class BuffList {
	// As a best practice, buffs should be immutable as soon as they are applied to a buffholder
    // You should always deepclone a buffholder's buffs
	private LinkedList<Buff> buffs = new LinkedList<Buff>();

	// Maybe will need another version of this for non-damage buffs if I ever make those
	public void add(Buff b) {
        // Either combine a buff with a similar one or just add it to the list
        for (Buff curr : buffs) {
            if (curr.isSimilar(b)) {
                curr.combine(b);
                return;
            }
        }
	}

    public void add(BuffList other) {
        for (Buff b : other.buffs) {
            buffs.add(b.clone());
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
        return (base * mult) + increase;
    }
}
