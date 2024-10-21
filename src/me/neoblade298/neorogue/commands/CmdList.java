package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CmdList extends Subcommand {
	private static final ArrayList<String> filterTypes = new ArrayList<String>(),
			ecs = new ArrayList<String>(), types = new ArrayList<String>(),
			rarities = new ArrayList<String>(), reforgeFilters = new ArrayList<String>(),
			tags = new ArrayList<String>(), droppable = new ArrayList<String>();
	
	private static Comparator<Equipment> sorter = new Comparator<Equipment>() {
		@Override
		public int compare(Equipment o1, Equipment o2) {
			int comp = Integer.compare(o1.getRarity().getValue(), o2.getRarity().getValue());
			if (comp != 0) return comp;
			return o1.getId().compareTo(o2.getId());
		}
	};

	public CmdList(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.overrideTabHandler();
		args.setOverride("{--type EquipmentType1,2...} {--rarity Rarity1,2...} {--class EquipmentClass1,2...} {--tags GlossaryTag1,2...}"
				+ " {--reforge Parent/Child/None}");
		args.setMax(-1);
		
		filterTypes.add("--type");
		filterTypes.add("--rarity");
		filterTypes.add("--class");
		filterTypes.add("--tags");
		filterTypes.add("--reforge");
		filterTypes.add("--droppable");
		for (EquipmentClass ec : EquipmentClass.values()) {
			ecs.add(ec.name());
		}
		for (EquipmentType type : EquipmentType.values()) {
			types.add(type.name());
		}
		for (Rarity rar : Rarity.values()) {
			rarities.add(rar.name());
		}
		for (ReforgeType rt : ReforgeType.values()) {
			reforgeFilters.add(rt.name());
		}
		for (GlossaryTag gt : GlossaryTag.values()) {
			tags.add(gt.name());
		}
		droppable.add("true");
		droppable.add("false");
	}
	
	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		if (args.length <= 2) {
			return filterTypes;
		}
		
		FilterType ft = FilterType.fromString(args[args.length - 2]);
		if (ft == null) {
			return filterTypes;
		}
		String currArg = args[args.length - 1];
		int lastComma = currArg.lastIndexOf(',');
		boolean hasCommas = lastComma != -1;
		String currArgSnip = hasCommas ? currArg.substring(lastComma + 1) : currArg;
		String currArgPrefix = hasCommas ? currArg.substring(0, lastComma + 1) : currArg;
		
		ArrayList<String> list = null;
		switch (ft) {
		case EQUIPMENT_CLASS: list = ecs;
		break;
		case EQUIPMENT_TYPE: list = types;
		break;
		case RARITY: list = rarities;
		break;
		case REFORGE: list = reforgeFilters;
		break;
		case TAGS: list = tags;
		break;
		// This one doesn't need commas appended
		case DROPPABLE: list = droppable;
		return list.stream().filter(str -> { return str.startsWith(currArgSnip);}).collect(Collectors.toList());
		}
		if (list == null) return null;
		return list.stream().filter(str -> { return str.startsWith(currArgSnip);})
		.map(str -> (hasCommas ? currArgPrefix : "") + str + ",").collect(Collectors.toList());
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length > 14) {
			Util.msg(s, "<red>Too many arguments! Try using commas instead.");
			return;
		}
		Stream<Equipment> stream = Equipment.getAll().stream();
		
		FilterType filter = null;
		for (String str : args) {
			if (str.length() > 40) {
				Util.msg(s, "<red>Argument is too long! Try reducing the argument size.");
				return;
			}
			if (str.endsWith(",")) str.substring(0, str.length() - 1);
			if (filter == null) {
				filter = FilterType.fromString(str);
			}
			else {
				switch (filter) {
				case EQUIPMENT_TYPE: 
						String[] typeStr = str.toUpperCase().split(",");
						EquipmentType[] types = new EquipmentType[typeStr.length];
						for (int i = 0; i < typeStr.length; i++) {
							try {
								types[i] = EquipmentType.valueOf(typeStr[i]);
							}
							catch (IllegalArgumentException ex) {
								Util.msg(s, "<red>Could not find equipment type " + str);
								return;
							}
						}
						EquipmentType.valueOf(str.toUpperCase());
						stream = stream.filter((eq) -> {
							for (EquipmentType type : types) {
								if (eq.getType() == type) return true;
							}
							return false;
						});
					break;
				case EQUIPMENT_CLASS:
					String[] clsStr = str.toUpperCase().split(",");
					EquipmentClass[] clsses = new EquipmentClass[clsStr.length];
					for (int i = 0; i < clsStr.length; i++) {
						try {
							clsses[i] = EquipmentClass.valueOf(clsStr[i]);
						}
						catch (IllegalArgumentException ex) {
							Util.msg(s, "<red>Could not find equipment class " + str);
							return;
						}
					}
					stream = stream.filter((eq) -> {
						for (EquipmentClass ec : clsses) {
							for (EquipmentClass eqc : eq.getEquipmentClasses()) {
								if (eqc == ec) return true;
							}
						}
						return false;
					});
					break;
				case RARITY:
					String[] rarStr = str.toUpperCase().split(",");
					Rarity[] rarities = new Rarity[rarStr.length];
					for (int i = 0; i < rarStr.length; i++) {
						try {
							rarities[i] = Rarity.valueOf(rarStr[i]);
						}
						catch (IllegalArgumentException ex) {
							Util.msg(s, "<red>Could not find rarity " + str);
							return;
						}
					}
					stream = stream.filter((eq) -> {
						for (Rarity rar : rarities) {
							if (eq.getRarity() == rar) return true;
						}
						return false;
					});
					break;
				case DROPPABLE:
					stream = stream.filter((eq) -> {
						return !Boolean.parseBoolean(str) ^ (eq.canDrop() && (eq.getReforgeParents().isEmpty() || eq.overridesReforgeDrop()));
					});
					break;
				case REFORGE:
					String[] refStr = str.toUpperCase().split(",");
					ReforgeType[] reforgeTypes = new ReforgeType[refStr.length];
					for (int i = 0; i < refStr.length; i++) {
						try {
							reforgeTypes[i] = ReforgeType.valueOf(refStr[i]);
						}
						catch (IllegalArgumentException ex) {
							Util.msg(s, "<red>Could not find reforge type " + refStr[i]);
							return;
						}
					}
					
					Predicate<Equipment> check = reforgeTypes[0].filter;
					for (int i = 1; i < reforgeTypes.length; i++) {
						check.and(reforgeTypes[i].filter);
					}
					stream = stream.filter(check);
					break;
				case TAGS:
					String[] tags = str.toUpperCase().split(",");
					GlossaryTag[] gts = new GlossaryTag[tags.length];
					int idx = 0;
					for (String tag : tags) {
						try {
							gts[idx++] = GlossaryTag.valueOf(tag);
						}
						catch (IllegalArgumentException ex) {
							Util.msg(s, "<red>Failed to find glossary tag " + tag);
							return;
						}
					}
					
					stream = stream.filter((eq) -> {
						for (GlossaryTag gt : gts) {
							if (!eq.getTags().contains(gt)) return false;
						}
						return true;
					});
					break;
				}
				filter = null;
			}
		}

		List<Equipment> list = stream.sorted(sorter).collect(Collectors.toList());
		for (Equipment eq : list) {
			Util.msgRaw(s, Component.text("- ", NamedTextColor.GRAY).append(eq.getHoverable()));
		};
		Util.msgRaw(s, "Found <yellow>" + list.size() + "</yellow> matches");
	}
	
	private enum FilterType {
		EQUIPMENT_TYPE,
		RARITY,
		EQUIPMENT_CLASS,
		TAGS,
		REFORGE,
		DROPPABLE;
		
		public static FilterType fromString(String str) {
			switch (str) {
			case "--type": return EQUIPMENT_TYPE;
			case "--rarity": return RARITY;
			case "--class": return EQUIPMENT_CLASS;
			case "--tags": return TAGS;
			case "--reforge": return REFORGE;
			case "--droppable": return DROPPABLE;
			}
			return null;
		}
	}
	
	private enum ReforgeType {
		PARENT((eq) -> {
			return !eq.getReforgeOptions().isEmpty();
		}),
		CHILD((eq) -> {
			return !eq.getReforgeParents().isEmpty();
		}),
		NONE((eq) -> {
			return eq.getReforgeOptions().isEmpty() && eq.getReforgeParents().isEmpty();
		});
		
		private Predicate<Equipment> filter;
		private ReforgeType(Predicate<Equipment> filter) {
			this.filter = filter;
		}
	}
}
