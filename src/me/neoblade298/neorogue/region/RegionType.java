package me.neoblade298.neorogue.region;

import java.util.HashMap;

public enum RegionType {
	// Outer Admiratio exists so the Tester chance can exist
	LOW_DISTRICT("Low District", 16, Layout.STANDARD),
	HARVEST_FIELDS("Harvest Fields", 16, Layout.STANDARD),
	MEADOWOOD("Meadowood", 5, Layout.TUTORIAL),
	OUTER_ADMIRATIO("Outer Admiratio", 16, Layout.STANDARD),
	LOW_DISTRICT_DEBUG("Low District (Debug Mode)", 16, Layout.STANDARD),
	HARVEST_FIELDS_DEBUG("Harvest Fields (Debug Mode)", 16, Layout.STANDARD),
	MEADOWOOD_DEBUG("Meadowood (Debug Mode)", 5, Layout.TUTORIAL),
	OUTER_ADMIRATIO_DEBUG("Outer Admiratio (Debug Mode)", 16, Layout.STANDARD);
	
	// ARGENT_PLAZA("Argent Plaza"),
	// FROZEN_WASTES("Frozen Wastes"), CAILIRIC_ARCHIVES("Cailiric Archives"), 
	// DEADMANS_MARSH("Deadman's Marsh"), OAKHELM("Oakhelm");

	private static HashMap<RegionType, RegionType> nextRegion = new HashMap<RegionType, RegionType>();
	static {
		nextRegion.put(LOW_DISTRICT, HARVEST_FIELDS);
	}
	
	private String display;
	private int rowCount;
	private Layout layout;

	private RegionType(String display, int rowCount, Layout layout) {
		this.display = display;
		this.rowCount = rowCount;
		this.layout = layout;
	}
	
	public String getDisplay() {
		return display;
	}

	public int getRowCount() {
		return rowCount;
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
            case OUTER_ADMIRATIO: return OUTER_ADMIRATIO_DEBUG;
		default:
			return type;
        }
    }

	public enum Layout {
		STANDARD,
		TUTORIAL
	}
}
