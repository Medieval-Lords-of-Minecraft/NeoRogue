package me.neoblade298.neorogue.session.fight;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class Shield implements Comparable<Shield> {
	private double amount, total, decayAmount;
	private int decayDelayTicks, decayPeriodTicks;
	private boolean isPercent;
	private int decayRepetitions;
	private ShieldHolder shieldHolder;
	private UUID applier;
	private BukkitTask task;
	public Shield(UUID applier, double amt, boolean isPercent, int decayDelayTicks, double decayAmount, 
			int decayPeriodTicks, int decayRepetitions) {
		this.total = amt;
		this.amount = amt;
		this.applier = applier;
		this.decayDelayTicks = decayDelayTicks;
		this.decayAmount = decayAmount;
		this.decayPeriodTicks = decayPeriodTicks;
		this.decayRepetitions = decayRepetitions;
		this.isPercent = isPercent;
	}
	
	public BukkitTask getTask() {
		return task;
	}
	
	public Shield(FightData data, UUID applier, double amt) {
		this(applier, amt, true, 0, 0, 0, 0);
	}
	
	// Should only be called before a shield is applied, or else it'll refill the shield and break stuff
	public void applyBuffs(BuffList amountBuff, BuffList durationBuff) {
		this.amount = amountBuff.apply(amount);
		this.total = amountBuff.apply(total);

		if (decayAmount == 0) {
			// If decay amount is 0, then we don't need to apply duration buffs
			return;
		}

		// Simple shield, only decays once after a set delay, buff the delay
		if (decayRepetitions == 1 && decayAmount == 100) {
			this.decayDelayTicks = durationBuff.apply(decayDelayTicks);
		}
		// Currently not used, as I don't use any complex decaying shields, but it would probably require
		// separate buff lists for both the decay delay and period since they can be different
		//else if (decayRepetitions > 1) {
		
		//}
	}
	
	public void initialize(FightData data) {
		this.shieldHolder = data.getShields();
		if (decayRepetitions == 0) return;
		
		task = new BukkitRunnable() {
			int reps = decayRepetitions;
			double total = amount;
			public void run() {
				if (!isUsable()) {
					return;
				}
				boolean outOfShield = false;
				if (isPercent) {
					outOfShield = decayAmount(total * decayAmount * 0.01);
				}
				else {
					outOfShield = decayAmount(decayAmount);
				}
				if (outOfShield || --reps <= 0) {
					this.cancel();
				}
				shieldHolder.update();
			}
		}.runTaskTimer(NeoRogue.inst(), decayDelayTicks, decayPeriodTicks);
	}
	
	// Returns leftover damage
	public double useShield(double damage) {
		if (this.amount <= 0) {
			return damage;
		}
		double original = this.amount;
		this.amount = Math.max(0, this.amount - damage);
		shieldHolder.subtractShields(original - amount);
		FightData fd = FightInstance.getFightData(applier);
		if (fd instanceof PlayerFightData) {
			((PlayerFightData) fd).getStats().addDamageShielded(original - amount);
		}
		new BukkitRunnable() {
			public void run() {
				shieldHolder.update();
			}
		}.runTask(NeoRogue.inst());
		return this.amount > 0 ? 0 : damage - original;
	}
	
	public void remove() {
		shieldHolder.subtractShields(this.amount);
	}
	
	public boolean isUsable() {
		return this.amount > 0;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public double getTotal() {
		return total;
	}
	
	// Needs its own method for bukkittask
	public boolean decayAmount(double amount) {
		double original = this.amount;
		this.amount = Math.max(this.amount - amount, 0);
		shieldHolder.subtractShields(original - this.amount);
		return this.amount <= 0;
	}
	
	private int getMaxDecayTime() {
		if (decayRepetitions == 0) return Integer.MAX_VALUE;
		return (int) (decayDelayTicks + decayPeriodTicks * decayRepetitions);
	}

	@Override
	public int compareTo(Shield o) {
		// Prioritize shields that decay sooner
		int comp = Integer.compare(getMaxDecayTime(), o.getMaxDecayTime());
		if (comp != 0) return comp;
		// Prioritize smaller shields
		return Double.compare(total, o.total);
	}
	
	@Override
	public String toString() {
		return amount + "/" + total + "-" + decayDelayTicks;
	}
}
