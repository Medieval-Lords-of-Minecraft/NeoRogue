package me.neoblade298.neorogue.player.boost;

// Static catalog of exp boost types. Each type carries a display name, a multiplier
// (e.g. 0.30 = +30% exp), and how its duration decays (by time or by runs).
public enum ExpBoostType {
	STARTER("Starter Boost", 0.30, BoostDurationType.RUNS),
	WEEKEND("Weekend Boost", 0.30, BoostDurationType.TIME),
	PATRON("Patron Boost", 0.50, BoostDurationType.TIME);

	private final String displayName;
	private final double multiplier;
	private final BoostDurationType durationType;

	ExpBoostType(String displayName, double multiplier, BoostDurationType durationType) {
		this.displayName = displayName;
		this.multiplier = multiplier;
		this.durationType = durationType;
	}

	public String getDisplayName() {
		return displayName;
	}

	// Additive multiplier, e.g. 0.30 means +30% exp.
	public double getMultiplier() {
		return multiplier;
	}

	public BoostDurationType getDurationType() {
		return durationType;
	}
}
