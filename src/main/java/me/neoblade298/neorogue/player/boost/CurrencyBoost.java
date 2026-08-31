package me.neoblade298.neorogue.player.boost;

// An active currency reward boost held by a player. The meaning of "remaining" depends on the
// type's duration type:
//   TIME -> absolute expiry epoch in millis
//   RUNS -> number of runs the boost still applies to
public class CurrencyBoost {
	private final CurrencyBoostType type;
	private long remaining;

	public CurrencyBoost(CurrencyBoostType type, long remaining) {
		if (!type.isGrantable()) throw new IllegalArgumentException("Permission-only boosts cannot be persisted");
		this.type = type;
		this.remaining = remaining;
	}

	public static CurrencyBoost create(CurrencyBoostType type, long durationInput) {
		if (type.getDurationType() == BoostDurationType.TIME) {
			return new CurrencyBoost(type, System.currentTimeMillis() + durationInput * 1000L);
		}
		return new CurrencyBoost(type, durationInput);
	}

	public CurrencyBoostType getType() {
		return type;
	}

	public long getRemaining() {
		return remaining;
	}

	public void setRemaining(long remaining) {
		this.remaining = remaining;
	}

	public long getRemainingDuration() {
		if (type.getDurationType() == BoostDurationType.TIME) {
			long remainingMillis = Math.max(0, remaining - System.currentTimeMillis());
			return (remainingMillis + 999) / 1000;
		}
		return Math.max(0, remaining);
	}

	public boolean isActive() {
		if (!type.isRegistered() || !type.isGrantable()) return false;
		if (type.getDurationType() == BoostDurationType.TIME) {
			return remaining > System.currentTimeMillis();
		}
		return remaining > 0;
	}

	public boolean isExpired() {
		return !isActive();
	}

	public double getMultiplier() {
		return isActive() ? type.getMultiplier() : 0.0;
	}

	public boolean tickRun() {
		if (type.isRegistered() && type.getDurationType() == BoostDurationType.RUNS && remaining > 0) {
			remaining--;
		}
		return isExpired();
	}
}
