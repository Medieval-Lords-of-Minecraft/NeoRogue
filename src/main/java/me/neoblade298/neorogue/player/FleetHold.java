package me.neoblade298.neorogue.player;

import java.util.LinkedHashMap;
import java.util.Map;

// One fleet cargo hold: a Cargo of item amounts plus a per-variant price snapshot taken at deposit
// time, and a timestamp of when the hold first became non-empty. Fleet holds are auto-sold at the
// next America/Los_Angeles midnight using their snapshot prices, so the sale value is locked in when
// the hold is filled rather than fluctuating with the live market (see PlayerData.resolveFleetSales).
public class FleetHold {
	private final Cargo cargo;
	// Snapshot unit price per variant, weighted-averaged across deposits.
	private final LinkedHashMap<CargoItem, Double> unitPrice = new LinkedHashMap<CargoItem, Double>();
	// Epoch millis of when this hold went empty -> non-empty; 0 while empty.
	private long filledAt;

	public FleetHold(int capacity, int slots) {
		this.cargo = new Cargo(capacity, slots);
	}

	public Cargo getCargo() {
		return cargo;
	}

	public long getFilledAt() {
		return filledAt;
	}

	public boolean isEmpty() {
		return cargo.getTotalItems() == 0;
	}

	// The snapshot unit price of a variant (falls back to the live exact-item price if unknown).
	public double getUnitPrice(CargoItem item) {
		return unitPrice.getOrDefault(item, item.getEffectivePrice());
	}

	// Total sale value at the snapshot prices (not the live market prices).
	public double getSnapshotValue() {
		double total = 0;
		for (Map.Entry<CargoItem, Integer> ent : cargo.getItems().entrySet()) {
			total += getUnitPrice(ent.getKey()) * ent.getValue();
		}
		return total;
	}

	// Deposits up to amount, snapshotting the current market price (weighted-averaged with any
	// existing snapshot for the variant). Returns the amount actually added.
	public int addItem(CargoItem item, int amount) {
		if (item == null || amount <= 0) return 0;
		int existing = cargo.getCount(item);
		int added = cargo.addItem(item, amount);
		if (added <= 0) return 0;
		if (filledAt == 0) filledAt = System.currentTimeMillis();
		double market = item.getEffectivePrice();
		double prev = unitPrice.getOrDefault(item, market);
		unitPrice.put(item, (prev * existing + market * added) / (existing + added));
		return added;
	}

	// Removes up to amount. Returns the amount actually removed.
	public int removeItem(CargoItem item, int amount) {
		int removed = cargo.removeItem(item, amount);
		if (removed <= 0) return 0;
		if (cargo.getCount(item) == 0) unitPrice.remove(item);
		if (cargo.getTotalItems() == 0) filledAt = 0;
		return removed;
	}

	// Loads a stored item with its snapshot price and fill time (bypasses limit checks).
	public void load(CargoItem item, int amount, double price, long filledAt) {
		if (item == null || amount <= 0) return;
		cargo.load(item, amount);
		unitPrice.put(item, price);
		if (filledAt > this.filledAt) this.filledAt = filledAt;
	}

	public void clear() {
		cargo.clear();
		unitPrice.clear();
		filledAt = 0;
	}

	public void setCapacity(int capacity) {
		cargo.setCapacity(capacity);
	}

	public void setSlots(int slots) {
		cargo.setSlots(slots);
	}
}
