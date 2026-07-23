package me.neoblade298.neorogue.session.analytics;

// Immutable, Bukkit-free snapshot of a finished run's outcome, built on the main thread when a run
// ends (win or loss) and handed to AnalyticsManager for asynchronous persistence. Keyed by the
// session's stable runId so per-run analytics (e.g. chance-choice winrate) can join on it.
public class RunSnapshot {
	public final String runId;
	public final long timestamp;
	public final int balanceVersion;
	public final String host;
	public final int slot;
	public final String sessionType;
	public final String regionType;
	public final int regionsCompleted;
	public final int level;
	public final int partySize;
	public final int notoriety;
	public final boolean endless;
	public final boolean won;

	public RunSnapshot(String runId, long timestamp, int balanceVersion, String host, int slot, String sessionType,
			String regionType, int regionsCompleted, int level, int partySize, int notoriety, boolean endless,
			boolean won) {
		this.runId = runId;
		this.timestamp = timestamp;
		this.balanceVersion = balanceVersion;
		this.host = host;
		this.slot = slot;
		this.sessionType = sessionType;
		this.regionType = regionType;
		this.regionsCompleted = regionsCompleted;
		this.level = level;
		this.partySize = partySize;
		this.notoriety = notoriety;
		this.endless = endless;
		this.won = won;
	}
}
