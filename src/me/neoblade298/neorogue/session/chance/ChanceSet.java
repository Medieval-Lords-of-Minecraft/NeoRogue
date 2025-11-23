package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.builtin.AmbushChance;
import me.neoblade298.neorogue.session.chance.builtin.CaravanRobberyChance;
import me.neoblade298.neorogue.session.chance.builtin.ForgottenWellChance;
import me.neoblade298.neorogue.session.chance.builtin.ForkInTheRoadChance;
import me.neoblade298.neorogue.session.chance.builtin.GreedChance;
import me.neoblade298.neorogue.session.chance.builtin.LabChance;
import me.neoblade298.neorogue.session.chance.builtin.LostRelicChance;
import me.neoblade298.neorogue.session.chance.builtin.ManaPoolChance;
import me.neoblade298.neorogue.session.chance.builtin.OvergrownLibraryChance;
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
		new CaravanRobberyChance();
		new StockpileChance();
		new ForgottenWellChance();
		new ForkInTheRoadChance();
		new GreedChance();
		new LabChance();
		new LostRelicChance();
		new ManaPoolChance();
		new OvergrownLibraryChance();
		new ShiningLightChance();
		new ThiefsCacheChance();
		new VultureChance();
		
		// Validate all chance classes are loaded
		validateAllChanceClassesLoaded();
	}
	
	/**
	 * Validates that all chance event classes in the builtin package are properly instantiated.
	 * Checks if the simple class name without "Chance" exists as a key in the setsById hashmap.
	 */
	private static void validateAllChanceClassesLoaded() {
		try {
			// Get the builtin package path
			String packageName = "me.neoblade298.neorogue.session.chance.builtin";
			ClassLoader classLoader = ChanceSet.class.getClassLoader();
			String path = packageName.replace('.', '/');
			java.net.URL resource = classLoader.getResource(path);
			
			if (resource == null) {
				NeoRogue.inst().getLogger().warning("[ChanceSet] Could not find builtin chance package for validation");
				return;
			}
			
			java.io.File directory = new java.io.File(resource.getFile());
			if (!directory.exists()) {
				NeoRogue.inst().getLogger().warning("[ChanceSet] Builtin chance directory does not exist for validation");
				return;
			}
			
			// Get all .java files that end with "Chance"
			java.io.File[] files = directory.listFiles((dir, name) -> 
				name.endsWith("Chance.java") && !name.equals("TestChance.java"));
			
			if (files == null) {
				NeoRogue.inst().getLogger().warning("[ChanceSet] Could not read builtin chance directory for validation");
				return;
			}
			
			java.util.ArrayList<String> missingClasses = new java.util.ArrayList<>();
			
			for (java.io.File file : files) {
				String className = file.getName().replace("Chance.java", "");
				// Remove "Chance" suffix to get expected ID
				
				if (!setsById.containsKey(className)) {
					missingClasses.add(className + " (expected ID: " + className + ")");
				}
			}
			
			if (!missingClasses.isEmpty()) {
				NeoRogue.inst().getLogger().warning("[NeoRogue] Validation failed: The following chance classes are not loaded:");
				for (String missing : missingClasses) {
					NeoRogue.inst().getLogger().warning("[NeoRogue] Missing: " + missing);
				}
			} else {
				NeoRogue.inst().getLogger().info(String.format(
					"[NeoRogue] Validation passed: All %d chance classes loaded successfully", files.length));
			}
			
		} catch (Exception e) {
			NeoRogue.inst().getLogger().warning("[NeoRogue] Error during chance class validation: " + e.getMessage());
		}
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
