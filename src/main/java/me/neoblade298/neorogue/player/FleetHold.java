package me.neoblade298.neorogue.player;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;

import me.ascheladd.asheconomy.pricing.MaterialPrices;

// One fleet cargo hold: a Cargo of item amounts plus a per-material price snapshot taken at deposit
// time, and a timestamp of when the hold first became non-empty. Fleet holds are auto-sold at the
// next America/Los_Angeles midnight using their snapshot prices, so the sale value is locked in when
// the hold is filled rather than fluctuating with the live market (see PlayerData.resolveFleetSales).
public class FleetHold {
	private final Cargo cargo;
	// Snapshot unit price per material, weighted-averaged across deposits.
	private final LinkedHashMap<Material, Double> unitPrice = new LinkedHashMap<Material, Double>();
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

	// The snapshot unit price of a material (falls back to the live market price if unknown).
	public double getUnitPrice(Material mat) {
		return unitPrice.getOrDefault(mat, MaterialPrices.getPrice(mat));
	}

	// Total sale value at the snapshot prices (not the live market prices).
	public double getSnapshotValue() {
		double total = 0;
		for (Map.Entry<Material, Integer> ent : cargo.getItems().entrySet()) {
			total += getUnitPrice(ent.getKey()) * ent.getValue();
		}
		return total;
	}

	// Deposits up to amount, snapshotting the current market price (weighted-averaged with any
	// existing snapshot for the material). Returns the amount actually added.
	public int addItem(Material mat, int amount) {
		if (mat == null || amount <= 0) return 0;
		int existing = cargo.getCount(mat);
		int added = cargo.addItem(mat, amount);
		if (added <= 0) return 0;
		if (filledAt == 0) filledAt = System.currentTimeMillis();
		double market = MaterialPrices.getPrice(mat);
		double prev = unitPrice.getOrDefault(mat, market);
		unitPrice.put(mat, (prev * existing + market * added) / (existing + added));
		return added;
	}

	// Removes up to amount. Returns the amount actually removed.
	public int removeItem(Material mat, int amount) {
		int removed = cargo.removeItem(mat, amount);
		if (removed <= 0) return 0;
		if (cargo.getCount(mat) == 0) unitPrice.remove(mat);
		if (cargo.getTotalItems() == 0) filledAt = 0;
		return removed;
	}

	// Loads a stored item with its snapshot price and fill time (bypasses limit checks).
	public void load(Material mat, int amount, double price, long filledAt) {
		if (mat == null || amount <= 0) return;
		cargo.load(mat, amount);
		unitPrice.put(mat, price);
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
