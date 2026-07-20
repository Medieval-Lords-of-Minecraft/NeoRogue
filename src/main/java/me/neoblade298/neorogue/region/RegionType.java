package me.neoblade298.neorogue.region;

import java.util.HashMap;

public enum RegionType {
	LOW_DISTRICT("Low District", 16, 0, Layout.STANDARD),
	HARVEST_FIELDS("Harvest Fields", 16, 1, Layout.STANDARD),
	FROZEN_WASTES("Frozen Wastes", 16, 2, Layout.STANDARD),
	MEADOWOOD("Meadowood", 7, 0, Layout.TUTORIAL),
	LOW_DISTRICT_DEBUG("Low District (Debug Mode)", 0, 16, Layout.STANDARD),
	HARVEST_FIELDS_DEBUG("Harvest Fields (Debug Mode)", 16, 1, Layout.STANDARD),
	MEADOWOOD_DEBUG("Meadowood (Debug Mode)", 7, 0, Layout.TUTORIAL),
	OUTER_ADMIRATIO_DEBUG("Outer Admiratio (Debug Mode)", 16, 2, Layout.STANDARD),
	FROZEN_WASTES_DEBUG("Frozen Wastes (Debug Mode)", 16, 2, Layout.STANDARD),
	TESTER("Tester", 16, 0, Layout.STANDARD);
	
	// ARGENT_PLAZA("Argent Plaza"),
	// CAILIRIC_ARCHIVES("Cailiric Archives"), 
	// DEADMANS_MARSH("Deadman's Marsh"), OAKHELM("Oakhelm");

	private static HashMap<RegionType, RegionType> nextRegion = new HashMap<RegionType, RegionType>();
	static {
		nextRegion.put(LOW_DISTRICT, HARVEST_FIELDS);
		nextRegion.put(HARVEST_FIELDS, FROZEN_WASTES);
	}

	// Fraction of a player's run cargo that is auto-sold upon completing each region.
	private static HashMap<RegionType, Double> cargoSellPercent = new HashMap<RegionType, Double>();
	static {
		cargoSellPercent.put(LOW_DISTRICT, 0.20);
		cargoSellPercent.put(HARVEST_FIELDS, 0.40);
		cargoSellPercent.put(FROZEN_WASTES, 1.00);
	}

	// Reverse of nextRegion: the region completed to reach a given region. Built after nextRegion.
	private static HashMap<RegionType, RegionType> previousRegion = new HashMap<RegionType, RegionType>();
	static {
		for (RegionType type : nextRegion.keySet()) {
			previousRegion.put(nextRegion.get(type), type);
		}
	}
	
	private String display;
	private int rowCount, difficulty;
	private Layout layout;

	private RegionType(String display, int rowCount, int difficulty, Layout layout) {
		this.display = display;
		this.rowCount = rowCount;
		this.difficulty = difficulty;
		this.layout = layout;
	}
	
	public String getDisplay() {
		return display;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public Layout getLayout() {
		return layout;
	}
	
	public static RegionType getNextRegion(RegionType curr, boolean endless) {
		return endless && !nextRegion.containsKey(curr) ? LOW_DISTRICT : nextRegion.get(curr);
	}

	// The region whose completion leads into curr, or null if curr has no predecessor (the first region).
	public static RegionType getPreviousRegion(RegionType curr) {
		return previousRegion.get(curr);
	}

	// Fraction (0-1) of run cargo auto-sold when this region is completed. 0 if not sellable.
	public double getCargoSellPercent() {
		return cargoSellPercent.getOrDefault(this, 0.0);
	}
    
    public static RegionType getDebugRegion(RegionType type) {
        switch(type) {
            case LOW_DISTRICT: return LOW_DISTRICT_DEBUG;
            case HARVEST_FIELDS: return HARVEST_FIELDS_DEBUG;
            case MEADOWOOD: return MEADOWOOD_DEBUG;
            case FROZEN_WASTES: return FROZEN_WASTES_DEBUG;
		default:
			return type;
        }
    }
    
    public boolean usesMountainousGeneration() {
        return this == FROZEN_WASTES || this == FROZEN_WASTES_DEBUG;
    }

	public enum Layout {
		STANDARD,
		TUTORIAL
	}
}
