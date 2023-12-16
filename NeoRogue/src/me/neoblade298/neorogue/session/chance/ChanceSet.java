package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.area.AreaType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class ChanceSet {
	private static HashMap<AreaType, ArrayList<ChanceSet>> sets = new HashMap<AreaType, ArrayList<ChanceSet>>();
	private static HashMap<String, ChanceSet> setsById = new HashMap<String, ChanceSet>();
	public static final String INIT_ID = "init";
	
	private HashMap<String, ChanceStage> stages = new HashMap<String, ChanceStage>();
	private String id;
	private TextComponent display;
	private Material mat;
	private boolean individualChoices = false;
	
	public static ChanceSet getSet(AreaType type) {
		ArrayList<ChanceSet> set = sets.get(type);
		return set.get(NeoCore.gen.nextInt(set.size()));
	}
	
	public static ChanceSet get(String id) {
		return setsById.get(id);
	}
	
	public static void load() {
		for (AreaType type : AreaType.values()) {
			sets.put(type, new ArrayList<ChanceSet>());
		}
		
		new GreedChance();
		new ForkInTheRoadChance();
	}
	
	public ChanceSet(AreaType type, Material mat, String id) {
		this(type, mat, id, id);
	}
	
	public ChanceSet(AreaType type, Material mat, String id, String display) {
		this.id = id;
		this.display = (TextComponent) SharedUtil.color("<gold>" + display).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
		this.mat = mat;
		sets.get(type).add(this);
		setsById.put(id, this);
	}
	
	public ChanceStage getInitialStage() {
		return stages.get(INIT_ID);
	}
	
	public TextComponent getDisplay() {
		return display;
	}
	
	public String getId() {
		return id;
	}
	
	public Material getMaterial() {
		return mat;
	}
	
	public void setInitialStage(ChanceStage stage) {
		stages.put(INIT_ID, stage);
	}
	
	public boolean isIndividual() {
		return individualChoices;
	}
}
