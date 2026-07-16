package me.neoblade298.neorogue.player;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;

// Read-only view over a player's finished-run history. Every requested statistic (winrate and
// winstreaks, lifetime or monthly, global or per-class, per notoriety) is derived here from the
// same immutable list of run records, so the storage layer only needs to append one row per run.
//
// Scope conventions used by the query methods:
//   - EquipmentClass ec == null  -> "global": all classes combined.
//   - EquipmentClass ec != null  -> only runs played on that class.
//   - Integer notoriety == null  -> all notoriety levels combined (used for headline winrates).
//   - Integer notoriety != null  -> only runs at that notoriety level (the "notoriety context").
// Winstreaks are always bucketed by a specific notoriety level (a loss at notoriety N only breaks
// the streak at notoriety N).
public class RunStats {
	// A single finished run for one player. playerClass is the class actually played (never null).
	public static class RunRecord {
		public final long ts;
		public final EquipmentClass playerClass;
		public final int notoriety;
		public final boolean won;

		public RunRecord(long ts, EquipmentClass playerClass, int notoriety, boolean won) {
			this.ts = ts;
			this.playerClass = playerClass;
			this.notoriety = notoriety;
			this.won = won;
		}
	}

	public static class Winrate {
		public final int wins, total;

		public Winrate(int wins, int total) {
			this.wins = wins;
			this.total = total;
		}

		public double rate() {
			return total == 0 ? 0.0 : (double) wins / total;
		}

		public boolean hasRuns() {
			return total > 0;
		}
	}

	public static class Streak {
		public final int current, best;

		public Streak(int current, int best) {
			this.current = current;
			this.best = best;
		}
	}

	private final List<RunRecord> records; // ascending by ts

	public RunStats(List<RunRecord> records) {
		this.records = new ArrayList<RunRecord>(records);
		this.records.sort(Comparator.comparingLong(r -> r.ts));
	}

	// wins/total for a scope. ec == null => all classes; notoriety == null => all notoriety levels;
	// monthOnly restricts to the current calendar month.
	public Winrate winrate(EquipmentClass ec, Integer notoriety, boolean monthOnly) {
		long monthStart = monthOnly ? startOfMonth() : Long.MIN_VALUE;
		int wins = 0, total = 0;
		for (RunRecord r : records) {
			if (ec != null && r.playerClass != ec) continue;
			if (notoriety != null && r.notoriety != notoriety) continue;
			if (r.ts < monthStart) continue;
			total++;
			if (r.won) wins++;
		}
		return new Winrate(wins, total);
	}

	// Current (trailing) and best consecutive win run at a single notoriety level for the scope.
	public Streak streak(EquipmentClass ec, int notoriety) {
		int best = 0, run = 0;
		for (RunRecord r : records) { // ascending, so run ends holding the trailing win count
			if (ec != null && r.playerClass != ec) continue;
			if (r.notoriety != notoriety) continue;
			if (r.won) {
				run++;
				if (run > best) best = run;
			}
			else {
				run = 0;
			}
		}
		return new Streak(run, best);
	}

	// The best win run across every notoriety level in the scope (a scope-level headline number).
	public int bestStreakAnyNotoriety(EquipmentClass ec) {
		int best = 0;
		for (int n : playedNotorieties(ec)) {
			best = Math.max(best, streak(ec, n).best);
		}
		return best;
	}

	// Notoriety levels the scope has at least one recorded run at, ascending.
	public TreeSet<Integer> playedNotorieties(EquipmentClass ec) {
		TreeSet<Integer> out = new TreeSet<Integer>();
		for (RunRecord r : records) {
			if (ec != null && r.playerClass != ec) continue;
			out.add(r.notoriety);
		}
		return out;
	}

	public boolean isEmpty() {
		return records.isEmpty();
	}

	// Epoch millis at the start of the current calendar month, system time zone.
	public static long startOfMonth() {
		LocalDate first = LocalDate.now().withDayOfMonth(1);
		return first.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
}
