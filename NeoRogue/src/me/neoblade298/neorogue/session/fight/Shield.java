package me.neoblade298.neorogue.session.fight;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;

public class Shield {
	private double amount;
	private double total;
	private ShieldHolder shieldHolder;
	private UUID applier;
	public Shield(FightData data, UUID applier, double amt, boolean decayPercent, double decayDelay, double decayAmount, double decayPeriod, int decayRepetitions) {
		this.total = amt;
		this.amount = amt;
		this.applier = applier;
		this.shieldHolder = data.getShields();
		new BukkitRunnable() {
			int reps = decayRepetitions;
			double total = amt;
			public void run() {
				if (!isUsable()) {
					return;
				}
				boolean outOfShield = false;
				if (decayPercent) {
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
		}.runTaskTimer(NeoRogue.inst(), (long) (decayDelay * 20), (long) (decayPeriod * 20));
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
		return this.amount > 0 ? 0 : damage - original;
	}
	
	public boolean isUsable() {
		return this.amount > 0;
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
}
