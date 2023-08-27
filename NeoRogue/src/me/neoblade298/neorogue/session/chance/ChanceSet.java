package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;

public class ChanceSet {
	private static HashMap<AreaType, ArrayList<ChanceSet>> sets = new HashMap<AreaType, ArrayList<ChanceSet>>();
	
	protected ArrayList<ChanceStage> stages = new ArrayList<ChanceStage>();
	private String id, display;
	private Material mat;
	
	public static ChanceSet getSet(AreaType type) {
		ArrayList<ChanceSet> set = sets.get(type);
		return set.get(NeoCore.gen.nextInt(set.size()));
	}
	
	public static void load() {
		for (AreaType type : AreaType.values()) {
			sets.put(type, new ArrayList<ChanceSet>());
		}
	}
	
	public ChanceSet(AreaType type, Material mat, String id) {
		this(type, mat, id, id);
	}
	
	public ChanceSet(AreaType type, Material mat, String id, String display) {
		this.id = id;
		this.display = display;
		this.mat = mat;
		sets.get(type).add(this);
	}
	
	public ArrayList<ChanceStage> getStages() {
		return stages;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public String getId() {
		return id;
	}
	
	public Material getMaterial() {
		return mat;
	}
}
