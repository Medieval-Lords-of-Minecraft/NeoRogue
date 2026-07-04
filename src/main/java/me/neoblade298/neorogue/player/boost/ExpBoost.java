package me.neoblade298.neorogue.player.boost;

// An active exp boost held by a player. The meaning of "remaining" depends on the
// type's duration type:
//   TIME -> absolute expiry epoch in millis
//   RUNS -> number of runs the boost still applies to
public class ExpBoost {
	private final ExpBoostType type;
	private long remaining;

	public ExpBoost(ExpBoostType type, long remaining) {
		this.type = type;
		this.remaining = remaining;
	}

	// Creates a boost from a fresh duration input. For TIME types, durationSeconds is
	// converted into an absolute expiry from now. For RUNS types, it is a run count.
	public static ExpBoost create(ExpBoostType type, long durationInput) {
		if (type.getDurationType() == BoostDurationType.TIME) {
			return new ExpBoost(type, System.currentTimeMillis() + durationInput * 1000L);
		}
		return new ExpBoost(type, durationInput);
	}

	public ExpBoostType getType() {
		return type;
	}

	public long getRemaining() {
		return remaining;
	}

	public void setRemaining(long remaining) {
		this.remaining = remaining;
	}

	// Whether this boost is currently active (not expired / still has runs left).
	public boolean isActive() {
		if (type.getDurationType() == BoostDurationType.TIME) {
			return remaining > System.currentTimeMillis();
		}
		return remaining > 0;
	}

	// Whether this boost has been fully consumed and should be discarded.
	public boolean isExpired() {
		return !isActive();
	}

	// The additive multiplier this boost contributes while active (0 otherwise).
	public double getMultiplier() {
		return isActive() ? type.getMultiplier() : 0.0;
	}

	// Decrements a RUNS boost by one run. No-op for TIME boosts. Returns true if the
	// boost is now fully consumed and should be removed.
	public boolean tickRun() {
		if (type.getDurationType() == BoostDurationType.RUNS && remaining > 0) {
			remaining--;
		}
		return isExpired();
	}
}
