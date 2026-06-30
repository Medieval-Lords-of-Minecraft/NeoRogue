package me.neoblade298.neorogue.session.analytics;

import java.util.HashMap;

import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

// Per-equipment (variant-keyed) value-added totals exported from a single player's FightStatistics.
// The variant key is Equipment.serialize() (id + "+" for upgraded). Non-equipment sources
// (status-driven damage, unattributed shields, etc.) are excluded by the exporter.
public class EquipmentContribution {
	public final String variantKey;
	public double damageDealt;
	public double damageBuffAdded;
	public double damageMitigated;
	public double shieldsApplied;
	public double healingDone;
	public final HashMap<StatusType, Integer> statuses = new HashMap<StatusType, Integer>();

	public EquipmentContribution(String variantKey) {
		this.variantKey = variantKey;
	}

	public void addStatus(StatusType type, int stacks) {
		statuses.merge(type, stacks, Integer::sum);
	}

	public int statusTotal() {
		int total = 0;
		for (int stacks : statuses.values()) {
			total += stacks;
		}
		return total;
	}

	public boolean hasContribution() {
		if (damageDealt != 0 || damageBuffAdded != 0 || damageMitigated != 0 || shieldsApplied != 0 || healingDone != 0) {
			return true;
		}
		for (int stacks : statuses.values()) {
			if (stacks != 0) return true;
		}
		return false;
	}
}
