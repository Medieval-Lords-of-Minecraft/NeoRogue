package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.builtin.AmbushChance;
import me.neoblade298.neorogue.session.chance.builtin.ForgottenWellChance;
import me.neoblade298.neorogue.session.chance.builtin.ForkInTheRoadChance;
import me.neoblade298.neorogue.session.chance.builtin.GreedChance;
import me.neoblade298.neorogue.session.chance.builtin.LabChance;
import me.neoblade298.neorogue.session.chance.builtin.LostRelicChance;
import me.neoblade298.neorogue.session.chance.builtin.ManaPoolChance;
import me.neoblade298.neorogue.session.chance.builtin.ShiningLightChance;
import me.neoblade298.neorogue.session.chance.builtin.StockpileChance;
import me.neoblade298.neorogue.session.chance.builtin.ThiefsCacheChance;
import me.neoblade298.neorogue.session.chance.builtin.VultureChance;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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
		return set.get(NeoRogue.gen.nextInt(set.size()));
	}
	
	public static ChanceSet get(String id) {
		return setsById.get(id);
	}
	
	public static void load() {
		for (AreaType type : AreaType.values()) {
			sets.put(type, new ArrayList<ChanceSet>());
		}
		
		new AmbushChance();
		new StockpileChance();
		new ForgottenWellChance();
		new ForkInTheRoadChance();
		new GreedChance();
		new LabChance();
		new LostRelicChance();
		new ManaPoolChance();
		new ShiningLightChance();
		new ThiefsCacheChance();
		new VultureChance();
	}
	
	public ChanceSet(AreaType type, Material mat, String id) {
		this(new AreaType[] { type }, mat, id, id);
	}
	
	public ChanceSet(AreaType[] types, Material mat, String id) {
		this(types, mat, id, id);
	}
	
	public ChanceSet(AreaType type, Material mat, String id, String display) {
		this(new AreaType[] { type }, mat, id, display);
	}

	public ChanceSet(AreaType[] types, Material mat, String id, String display) {
		this.id = id;
		this.display = (TextComponent) SharedUtil.color(display).colorIfAbsent(NamedTextColor.RED)
				.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
		this.mat = mat;
		for (AreaType type : types) {
			sets.get(type).add(this);
		}
		setsById.put(id, this);
	}
	public ChanceSet(AreaType[] types, Material mat, String id, String display, boolean individualChoices) {
		this(types, mat, id, display);
		this.individualChoices = individualChoices;
	}
	public ChanceSet(AreaType type, Material mat, String id, String display, boolean individualChoices) {
		this(new AreaType[] { type }, mat, id, display, individualChoices);
	}
	
	public ChanceStage getInitialStage() {
		return stages.get(INIT_ID);
	}
	
	public ChanceStage getStage(String key) {
		return stages.get(key);
	}
	
	public void addStage(ChanceStage stage) {
		stages.put(stage.getId(), stage);
	}
	
	public void initialize(Session s, ChanceInstance inst) {
		
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
	
	public boolean isIndividual() {
		return individualChoices;
	}
}
