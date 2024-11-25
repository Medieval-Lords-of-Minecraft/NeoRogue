package me.neoblade298.neorogue.area;

import java.util.HashMap;

public enum AreaType {
	LOW_DISTRICT("Low District"), HARVEST_FIELDS("Harvest Fields"), ARGENT_PLAZA("Argent Plaza"),
	FROZEN_WASTES("Frozen Wastes"), CAILIRIC_ARCHIVES("Cailiric Archives"), OUTER_ADMIRATIO("Outer Admiratio"),
	DEADMANS_MARSH("Deadman's Marsh"), OAKHELM("Oakhelm");

	private static HashMap<AreaType, AreaType> nextArea = new HashMap<AreaType, AreaType>();
	static {
		nextArea.put(LOW_DISTRICT, HARVEST_FIELDS);
		nextArea.put(HARVEST_FIELDS, FROZEN_WASTES);
	}
	
	private String display;

	private AreaType(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public static AreaType getNextArea(AreaType type, boolean endless) {
		return endless ? LOW_DISTRICT : nextArea.get(type); // TODO: update in future as new areas are completed
	}
}
