package me.neoblade298.neorogue.player.boost;

// Determines how an exp boost's remaining duration is interpreted and decayed.
public enum BoostDurationType {
	// Decays by wall-clock time. Remaining is stored as an absolute expiry epoch (millis).
	TIME,
	// Decays by 1 each time a player starts a run. Remaining is a run count.
	RUNS;
}
