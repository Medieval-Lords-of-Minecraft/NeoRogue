package me.neoblade298.neorogue.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Simple equipment counter that parses Equipment.java source file directly
 * instead of trying to load the classes (which requires full Bukkit/Paper environment).
 * 
 * Usage: java me.neoblade298.neorogue.standalone.SimpleEquipmentCounter [--upgraded]
 * 
 * Run from bin directory:
 *   cd bin
 *   java me.neoblade298.neorogue.standalone.SimpleEquipmentCounter
 */
public class EquipmentCounter {
	
	private static final Pattern NEW_PATTERN = Pattern.compile("new\\s+(\\w+)\\(b\\);");
	private static final Pattern RARITY_PATTERN = Pattern.compile("Rarity\\.(\\w+)");
	private static final Pattern TYPE_PATTERN = Pattern.compile("EquipmentType\\.(\\w+)");
	private static final Pattern CLASS_PATTERN = Pattern.compile("EquipmentClass\\.(\\w+)");
	
	private static class EquipmentInfo {
		String name;
		String rarity;
		String type;
		Set<String> classes = new HashSet<>();
		
		EquipmentInfo(String name, String rarity, String type) {
			this.name = name;
			this.rarity = rarity != null ? rarity : "UNKNOWN";
			this.type = type != null ? type : "UNKNOWN";
		}
	}
	
	private static class Filters {
		Set<String> types = new HashSet<>();
		Set<String> rarities = new HashSet<>();
		Set<String> classes = new HashSet<>();
		
		boolean matches(EquipmentInfo info) {
			if (!types.isEmpty() && !types.contains(info.type)) return false;
			if (!rarities.isEmpty() && !rarities.contains(info.rarity)) return false;
			if (!classes.isEmpty()) {
				boolean hasMatch = false;
				for (String cls : classes) {
					if (info.classes.contains(cls)) {
						hasMatch = true;
						break;
					}
				}
				if (!hasMatch) return false;
			}
			return true;
		}
	}
	
	public static void main(String[] args) {
		System.out.println("=== NeoRogue Equipment Counter (Interactive) ===\n");
		
		// Find Equipment.java source file
		String equipmentFile = "../src/me/neoblade298/neorogue/equipment/Equipment.java";
		String equipmentDir = "../src/me/neoblade298/neorogue/equipment/";
		
		try {
			List<String> equipmentNames = parseEquipmentFile(equipmentFile);
			
			if (equipmentNames.isEmpty()) {
				System.err.println("No equipment found in Equipment.java");
				return;
			}
			
			System.out.println("Found " + equipmentNames.size() + " equipment classes. Analyzing...\n");
			
			// Get detailed info for each equipment
			List<EquipmentInfo> equipmentList = new ArrayList<>();
			for (String name : equipmentNames) {
				EquipmentInfo info = analyzeEquipment(equipmentDir, name);
				if (info != null) {
					equipmentList.add(info);
				}
			}
			
			// Interactive filtering
			Filters filters = new Filters();
			try (Scanner scanner = new Scanner(System.in)) {
				boolean done = false;
				while (!done) {
					displayFilterMenu(filters, equipmentList);
					String input = scanner.nextLine().trim();
					
					if (input.isEmpty() || input.equals("0")) {
						done = true;
					} else {
						processMenuChoice(input, filters, scanner);
					}
				}
			}
			
			// Apply filters and display results
			List<EquipmentInfo> filtered = equipmentList.stream()
				.filter(filters::matches)
				.collect(Collectors.toList());
			
			displayResults(filtered, filters);
			
		} catch (IOException e) {
			System.err.println("Error reading Equipment.java: " + e.getMessage());
			System.err.println("Make sure you're running from the bin directory");
			e.printStackTrace();
		}
	}
	
	private static void displayFilterMenu(Filters filters, List<EquipmentInfo> allEquipment) {
		System.out.println("\n========================================");
		System.out.println("Current Filters:");
		if (filters.types.isEmpty() && filters.rarities.isEmpty() && filters.classes.isEmpty()) {
			System.out.println("  (none - showing all equipment)");
		} else {
			if (!filters.types.isEmpty()) System.out.println("  Types: " + String.join(", ", filters.types));
			if (!filters.rarities.isEmpty()) System.out.println("  Rarities: " + String.join(", ", filters.rarities));
			if (!filters.classes.isEmpty()) System.out.println("  Classes: " + String.join(", ", filters.classes));
		}
		
		// Calculate current match count
		long matchCount = allEquipment.stream().filter(filters::matches).count();
		System.out.println("  Matches: " + matchCount + " / " + allEquipment.size());
		
		System.out.println("\nAdd Filter:");
		System.out.println("  1) Filter by Type");
		System.out.println("  2) Filter by Rarity");
		System.out.println("  3) Filter by Class");
		System.out.println("  4) Clear All Filters");
		System.out.println("  0) Show Results");
		System.out.print("\nChoice: ");
	}
	
	private static void processMenuChoice(String choice, Filters filters, Scanner scanner) {
		switch (choice) {
		case "1":
			addTypeFilter(filters, scanner);
			break;
		case "2":
			addRarityFilter(filters, scanner);
			break;
		case "3":
			addClassFilter(filters, scanner);
			break;
		case "4":
			filters.types.clear();
			filters.rarities.clear();
			filters.classes.clear();
			System.out.println("\n✓ All filters cleared");
			break;
		default:
			System.out.println("\nInvalid choice. Try again.");
		}
	}
	
	private static void addTypeFilter(Filters filters, Scanner scanner) {
		System.out.println("\n--- Equipment Types ---");
		String[] types = {"ABILITY", "ACCESSORY", "ARMOR", "WEAPON", "OFFHAND", "ARTIFACT", "CONSUMABLE", "MATERIAL"};
		for (int i = 0; i < types.length; i++) {
			String mark = filters.types.contains(types[i]) ? "[X]" : "[ ]";
			System.out.println("  " + (i + 1) + ") " + mark + " " + types[i]);
		}
		System.out.print("\nSelect type (number) or 0 to cancel: ");
		String input = scanner.nextLine().trim();
		
		try {
			int choice = Integer.parseInt(input);
			if (choice > 0 && choice <= types.length) {
				String type = types[choice - 1];
				if (filters.types.contains(type)) {
					filters.types.remove(type);
					System.out.println("✓ Removed " + type);
				} else {
					filters.types.add(type);
					System.out.println("✓ Added " + type);
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
		}
	}
	
	private static void addRarityFilter(Filters filters, Scanner scanner) {
		System.out.println("\n--- Rarities ---");
		String[] rarities = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"};
		for (int i = 0; i < rarities.length; i++) {
			String mark = filters.rarities.contains(rarities[i]) ? "[X]" : "[ ]";
			System.out.println("  " + (i + 1) + ") " + mark + " " + rarities[i]);
		}
		System.out.print("\nSelect rarity (number) or 0 to cancel: ");
		String input = scanner.nextLine().trim();
		
		try {
			int choice = Integer.parseInt(input);
			if (choice > 0 && choice <= rarities.length) {
				String rarity = rarities[choice - 1];
				if (filters.rarities.contains(rarity)) {
					filters.rarities.remove(rarity);
					System.out.println("✓ Removed " + rarity);
				} else {
					filters.rarities.add(rarity);
					System.out.println("✓ Added " + rarity);
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
		}
	}
	
	private static void addClassFilter(Filters filters, Scanner scanner) {
		System.out.println("\n--- Equipment Classes ---");
		String[] classes = {"THIEF", "WARRIOR", "MAGE", "ARCHER", "CLASSLESS"};
		for (int i = 0; i < classes.length; i++) {
			String mark = filters.classes.contains(classes[i]) ? "[X]" : "[ ]";
			System.out.println("  " + (i + 1) + ") " + mark + " " + classes[i]);
		}
		System.out.print("\nSelect class (number) or 0 to cancel: ");
		String input = scanner.nextLine().trim();
		
		try {
			int choice = Integer.parseInt(input);
			if (choice > 0 && choice <= classes.length) {
				String cls = classes[choice - 1];
				if (filters.classes.contains(cls)) {
					filters.classes.remove(cls);
					System.out.println("✓ Removed " + cls);
				} else {
					filters.classes.add(cls);
					System.out.println("✓ Added " + cls);
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
		}
	}
	
	private static void displayResults(List<EquipmentInfo> filtered, Filters filters) {
		System.out.println("\n========================================");
		System.out.println("=== RESULTS ===");
		System.out.println("========================================\n");
		
		if (filtered.isEmpty()) {
			System.out.println("No equipment matches the selected filters.");
			return;
		}
		
		// Group by rarity
		Map<String, List<EquipmentInfo>> byRarity = new TreeMap<>();
		for (EquipmentInfo info : filtered) {
			byRarity.computeIfAbsent(info.rarity, k -> new ArrayList<>()).add(info);
		}
		
		// Display by rarity
		String[] rarityOrder = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "UNKNOWN"};
		for (String rarity : rarityOrder) {
			List<EquipmentInfo> items = byRarity.get(rarity);
			if (items == null || items.isEmpty()) continue;
			
			System.out.println("[" + rarity + "]: " + items.size());
			for (EquipmentInfo info : items) {
				String classInfo = info.classes.isEmpty() ? "" : " - " + String.join(", ", info.classes);
				System.out.println("  - " + info.name + " [" + info.rarity + "] (" + info.type + ")" + classInfo);
			}
			System.out.println();
		}
		
		System.out.println("=== Total: " + filtered.size() + " equipment ===");
	}
	
	private static List<String> parseEquipmentFile(String filePath) throws IOException {
		List<String> equipmentNames = new ArrayList<>();
		boolean inLoadMethod = false;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Find the load() method
				if (line.contains("public static void load()")) {
					inLoadMethod = true;
					continue;
				}
				
				if (inLoadMethod) {
					// Exit when we reach the end of the method
					if (line.trim().startsWith("}") && !line.contains("new ")) {
						break;
					}
					
					// Look for "new ClassName(b);" patterns
					Matcher matcher = NEW_PATTERN.matcher(line);
					if (matcher.find()) {
						String className = matcher.group(1);
						if (!equipmentNames.contains(className)) {
							equipmentNames.add(className);
						}
					}
				}
			}
		}
		
		return equipmentNames;
	}
	
	private static EquipmentInfo analyzeEquipment(String equipmentDir, String className) {
		// Try to find the source file in various subdirectories
		String[] subdirs = {"abilities", "accessories", "armor", "weapons", "offhands", "artifacts", "consumables", "cursed", "materials"};
		
		for (String subdir : subdirs) {
			File file = new File(equipmentDir + subdir + "/" + className + ".java");
			if (file.exists()) {
				try {
					return parseEquipmentFile(file, className, subdir);
				} catch (IOException e) {
					// Continue to next location
				}
			}
		}
		
		// Not found, return basic info
		return new EquipmentInfo(className, "UNKNOWN", "UNKNOWN");
	}
	
	private static EquipmentInfo parseEquipmentFile(File file, String className, String folder) throws IOException {
		EquipmentInfo info = new EquipmentInfo(className, null, null);
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			int lineCount = 0;
			StringBuilder superCall = new StringBuilder();
			boolean inSuper = false;
			
			while ((line = reader.readLine()) != null && lineCount++ < 150) {
				// Look for super(...) constructor call (may span multiple lines)
				if (line.contains("super(")) {
					inSuper = true;
					superCall.setLength(0); // Reset
				}
				
				if (inSuper) {
					superCall.append(line).append(" ");
					
					// Check if we've reached the end of the super call
					if (line.contains(");")) {
						String fullSuper = superCall.toString();
						
						// Extract rarity
						Matcher rarityMatcher = RARITY_PATTERN.matcher(fullSuper);
						if (rarityMatcher.find()) {
							info.rarity = rarityMatcher.group(1);
						}
						
						// Extract type
						Matcher typeMatcher = TYPE_PATTERN.matcher(fullSuper);
						if (typeMatcher.find() && info.type == null) {
							info.type = typeMatcher.group(1);
						}
						
						// Extract classes (can be multiple)
						Matcher classMatcher = CLASS_PATTERN.matcher(fullSuper);
						while (classMatcher.find()) {
							info.classes.add(classMatcher.group(1));
						}
						
						inSuper = false;
						break; // Found what we need
					}
				}
			}
		}
		
		// Fallback for type based on folder
		if (info.type == null || info.type.equals("UNKNOWN")) {
			info.type = folderToType(folder);
		}
		
		return info;
	}
	
	private static String folderToType(String folder) {
		switch (folder.toLowerCase()) {
		case "abilities": return "ABILITY";
		case "accessories": return "ACCESSORY";
		case "armor": return "ARMOR";
		case "weapons": return "WEAPON";
		case "offhands": return "OFFHAND";
		case "artifacts": return "ARTIFACT";
		case "consumables": return "CONSUMABLE";
		case "materials": return "MATERIAL";
		default: return "UNKNOWN";
		}
	}
}
