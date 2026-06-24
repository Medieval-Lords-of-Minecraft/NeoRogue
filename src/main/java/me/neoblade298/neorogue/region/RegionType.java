package me.neoblade298.neorogue.region;

import java.util.HashMap;

public enum RegionType {
	LOW_DISTRICT("Low District", 16, 0, Layout.STANDARD),
	HARVEST_FIELDS("Harvest Fields", 16, 1, Layout.STANDARD),
	FROZEN_WASTES("Frozen Wastes", 16, 2, Layout.STANDARD),
	MEADOWOOD("Meadowood", 5, 0, Layout.TUTORIAL),
	LOW_DISTRICT_DEBUG("Low District (Debug Mode)", 0, 16, Layout.STANDARD),
	HARVEST_FIELDS_DEBUG("Harvest Fields (Debug Mode)", 16, 1, Layout.STANDARD),
	MEADOWOOD_DEBUG("Meadowood (Debug Mode)", 5, 0, Layout.TUTORIAL),
	OUTER_ADMIRATIO_DEBUG("Outer Admiratio (Debug Mode)", 16, 2, Layout.STANDARD),
	FROZEN_WASTES_DEBUG("Frozen Wastes (Debug Mode)", 16, 2, Layout.STANDARD);
	
	// ARGENT_PLAZA("Argent Plaza"),
	// CAILIRIC_ARCHIVES("Cailiric Archives"), 
	// DEADMANS_MARSH("Deadman's Marsh"), OAKHELM("Oakhelm");

	private static HashMap<RegionType, RegionType> nextRegion = new HashMap<RegionType, RegionType>();
	static {
		nextRegion.put(LOW_DISTRICT, HARVEST_FIELDS);
		nextRegion.put(HARVEST_FIELDS, FROZEN_WASTES);
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
