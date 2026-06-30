package me.neoblade298.neorogue.session.analytics;

import java.util.ArrayList;

import me.neoblade298.neorogue.equipment.Equipment;

// Immutable, Bukkit-free snapshot of a single equipment-offer event (a reward "choose 1 of N"
// screen or a shop's equipment stock). Built on the main thread at selection/teardown and handed to
// AnalyticsManager for asynchronous persistence. Each offered equipment becomes one OfferRow with a
// picked flag, enabling picked/offered pickrate analysis.
public class OfferSnapshot {
	// Offer sources. Stored as a column so reward and shop pickrates can be sliced separately.
	public enum OfferSource {
		REWARD, SHOP;
	}

	public final String offerId;
	public final long timestamp;
	public final int balanceVersion;
	public final String playerUuid;
	public final String host;
	public final int slot;
	public final String source;
	public final String regionType;
	public final String nodeType;
	public final int level;

	public final ArrayList<OfferRow> rows = new ArrayList<OfferRow>();

	public OfferSnapshot(String offerId, long timestamp, int balanceVersion, String playerUuid, String host, int slot,
			OfferSource source, String regionType, String nodeType, int level) {
		this.offerId = offerId;
		this.timestamp = timestamp;
		this.balanceVersion = balanceVersion;
		this.playerUuid = playerUuid;
		this.host = host;
		this.slot = slot;
		this.source = source.name();
		this.regionType = regionType;
		this.nodeType = nodeType;
		this.level = level;
	}

	// Adds one offered equipment to this snapshot, extracting its denormalized metadata.
	public void addOffer(Equipment eq, int slotIndex, boolean picked, int price) {
		if (eq == null) return;
		String rarity = eq.getRarity() != null ? eq.getRarity().name() : "NONE";
		String type = eq.getType() != null ? eq.getType().name() : "NONE";
		String eclass = joinEquipmentClasses(eq);
		rows.add(new OfferRow(eq.getId(), eq.isUpgraded(), rarity, type, eclass, slotIndex, picked, price));
	}

	private static String joinEquipmentClasses(Equipment eq) {
		if (eq.getEquipmentClasses() == null || eq.getEquipmentClasses().length == 0) return "NONE";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < eq.getEquipmentClasses().length; i++) {
			if (i > 0) sb.append(",");
			sb.append(eq.getEquipmentClasses()[i].name());
		}
		return sb.toString();
	}

	// A single offered equipment variant within an offer event.
	public static class OfferRow {
		public final String equipmentId;
		public final boolean upgraded;
		public final String rarity;
		public final String equipType;
		public final String equipClass;
		public final int slotIndex;
		public final boolean picked;
		public final int price;

		public OfferRow(String equipmentId, boolean upgraded, String rarity, String equipType, String equipClass,
				int slotIndex, boolean picked, int price) {
			this.equipmentId = equipmentId;
			this.upgraded = upgraded;
			this.rarity = rarity;
			this.equipType = equipType;
			this.equipClass = equipClass;
			this.slotIndex = slotIndex;
			this.picked = picked;
			this.price = price;
		}
	}
}
