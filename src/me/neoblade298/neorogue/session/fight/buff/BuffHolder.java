package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;
import java.util.LinkedList;

public class BuffHolder {
	// As a best practice, buffs should be immutable as soon as they are applied to a buffholder
    // You should always deepclone a buffholder's buffs
	protected HashMap<DamageBuffType, LinkedList<Buff>> damageBuffs = new HashMap<DamageBuffType, LinkedList<Buff>>(),
		defenseBuffs = new HashMap<DamageBuffType, LinkedList<Buff>>();

	// Maybe will need another version of this for non-damage buffs if I ever make those
	public void addBuff(boolean damageBuff, DamageBuffType type, Buff b) {
		LinkedList<Buff> buffs = damageBuff ? damageBuffs.getOrDefault(type, new LinkedList<Buff>()) : defenseBuffs.getOrDefault(type, new LinkedList<Buff>());
		if (damageBuff) damageBuffs.put(type, buffs);
		else defenseBuffs.put(type, buffs);

        // Either combine a buff with a similar one or just add it to the list
        for (Buff curr : buffs) {
            if (curr.isSimilar(b)) {
                curr.combine(b);
                return;
            }
        }
        buffs.add(b);
	}

    public HashMap<DamageBuffType, LinkedList<Buff>> getBuffs(boolean damageBuff) {
        return damageBuff ? damageBuffs : defenseBuffs;
    }
}
