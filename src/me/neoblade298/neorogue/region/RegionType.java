package me.neoblade298.neorogue.region;

import java.util.HashMap;

public enum RegionType {
	// Outer Admiratio exists so the Tester chance can exist
	LOW_DISTRICT("Low District"), HARVEST_FIELDS("Harvest Fields"), OUTER_ADMIRATIO("Outer Admiratio");
	
	// ARGENT_PLAZA("Argent Plaza"),
	// FROZEN_WASTES("Frozen Wastes"), CAILIRIC_ARCHIVES("Cailiric Archives"), 
	// DEADMANS_MARSH("Deadman's Marsh"), OAKHELM("Oakhelm");

	private static HashMap<RegionType, RegionType> nextRegion = new HashMap<RegionType, RegionType>();
	static {
		nextRegion.put(LOW_DISTRICT, HARVEST_FIELDS);
	}
	
	private String display;

	private RegionType(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public static RegionType getNextRegion(RegionType curr, boolean endless) {
		return endless && !nextRegion.containsKey(curr) ? LOW_DISTRICT : nextRegion.get(curr);
	}
}
