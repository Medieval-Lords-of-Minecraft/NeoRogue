package me.neoblade298.neorogue.area;

public enum AreaType {
	LOW_DISTRICT("Low District"),
	HARVEST_FIELDS("Harvest Fields"),
	ARGENT_PLAZA("Argent Plaza"),
	FROZEN_WASTES("Frozen Wastes"),
	CAILIRIC_ARCHIVES("Cailiric Archives"),
	OUTER_ADMIRATIO("Outer Admiratio"),
	DEADMANS_MARSH("Deadman's Marsh"),
	OAKHELM("Oakhelm");
	
	private String display;
	private AreaType(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
}
