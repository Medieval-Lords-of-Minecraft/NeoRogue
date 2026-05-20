package me.neoblade298.neorogue.session;

import me.neoblade298.neorogue.region.RegionType;

public enum SessionType {
	STANDARD(RegionType.LOW_DISTRICT),
	TUTORIAL(RegionType.MEADOWOOD);

	private RegionType initialRegion;

	private SessionType(RegionType initialRegion) {
		this.initialRegion = initialRegion;
	}

	public RegionType getInitialRegion() {
		return initialRegion;
	}

	public void onInstanceSetup(Session s, Instance inst) {
	}

	public static SessionType fromStorage(String raw) {
		if (raw == null || raw.isBlank()) {
			return STANDARD;
		}
		try {
			return SessionType.valueOf(raw);
		} catch (IllegalArgumentException ex) {
			return STANDARD;
		}
	}
}