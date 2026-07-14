package me.neoblade298.neorogue.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Reusable key=value filter parser shared by the /nrlytics views. Each view declares which columns
// it exposes via FilterOption; user tokens like "class=warrior" are validated and turned into
// parameterized WHERE fragments so every query filters in the same consistent way.
public class AnalyticsFilters {

	// Declares one filterable column: the user-facing key, the backing SQL column, whether the column
	// stores a comma-separated set (needs FIND_IN_SET instead of =), and the allowed values (uppercased).
	public static class FilterOption {
		public final String key, column;
		public final boolean multiValued;
		public final List<String> allowed; // valid values (uppercased); null means accept anything

		public FilterOption(String key, String column, boolean multiValued, List<String> allowed) {
			this.key = key;
			this.column = column;
			this.multiValued = multiValued;
			this.allowed = allowed;
		}
	}

	private final ArrayList<String> clauses = new ArrayList<String>();
	private final ArrayList<String> binds = new ArrayList<String>();
	private final ArrayList<String> summary = new ArrayList<String>();
	private final ArrayList<String> errors = new ArrayList<String>();

	private AnalyticsFilters() {}

	// Parses key=value tokens (from args[startIdx..]) against the given options. Unknown keys or
	// invalid values are collected in getErrors() so the caller can report them without failing.
	public static AnalyticsFilters parse(String[] args, int startIdx, List<FilterOption> options) {
		AnalyticsFilters f = new AnalyticsFilters();
		for (int i = startIdx; i < args.length; i++) {
			String token = args[i];
			int eq = token.indexOf('=');
			if (eq <= 0 || eq == token.length() - 1) {
				f.errors.add("Ignored '" + token + "' (expected key=value)");
				continue;
			}
			String key = token.substring(0, eq).toLowerCase();
			String value = token.substring(eq + 1).toUpperCase();

			FilterOption opt = null;
			for (FilterOption o : options) {
				if (o.key.equalsIgnoreCase(key)) {
					opt = o;
					break;
				}
			}
			if (opt == null) {
				f.errors.add("Unknown filter '" + key + "'");
				continue;
			}
			if (opt.allowed != null && !opt.allowed.contains(value)) {
				f.errors.add("Invalid " + key + " '" + value + "'");
				continue;
			}

			f.clauses.add(opt.multiValued ? "FIND_IN_SET(?, " + opt.column + ")" : opt.column + " = ?");
			f.binds.add(value);
			f.summary.add(key + "=" + value);
		}
		return f;
	}

	// Appends " AND <clause>" fragments onto an existing WHERE clause.
	public void appendWhere(StringBuilder sql) {
		for (String c : clauses) {
			sql.append(" AND ").append(c);
		}
	}

	// Binds the filter values starting at the given 1-based index; returns the next free index.
	public int bind(PreparedStatement ps, int idx) throws SQLException {
		for (String b : binds) {
			ps.setString(idx++, b);
		}
		return idx;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public List<String> getErrors() {
		return errors;
	}

	// Human-readable summary for the report header.
	public String summary() {
		return summary.isEmpty() ? "no filters" : String.join(", ", summary);
	}
}
