package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
			tags = new ArrayList<String>();

	public CmdList(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.overrideTabHandler();
		args.setOverride("{--type EquipmentType} {--rarity Rarity} {--class EquipmentClass} {--tags GlossaryTag1,Tag2...}"
				+ " {--reforge Parent/Child/None}");
		args.setMax(-1);
		
		filterTypes.add("--type");
		filterTypes.add("--rarity");
		filterTypes.add("--class");
		filterTypes.add("--tags");
		filterTypes.add("--reforge");
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
		switch (ft) {
		case EQUIPMENT_CLASS: return ecs;
		case EQUIPMENT_TYPE: return types;
		case RARITY: return rarities;
		case REFORGE: return reforgeFilters;
		case TAGS: return tags;
		}
		return null;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Stream<Equipment> stream = Equipment.getAll().stream();
		
		FilterType filter = null;
		for (String str : args) {
			if (filter == null) {
				filter = FilterType.fromString(str);
			}
			else {
				switch (filter) {
				case EQUIPMENT_TYPE: 
					try {
						EquipmentType type = EquipmentType.valueOf(str.toUpperCase());
						stream = stream.filter((eq) -> {
							return eq.getType() == type;
						});
					}
					catch (IllegalArgumentException ex) {
						Util.msg(s, "<red>Could not find equipment type " + str);
						return;
					}
					break;
				case EQUIPMENT_CLASS:
					try {
						EquipmentClass ec = EquipmentClass.valueOf(str.toUpperCase());
						stream = stream.filter((eq) -> {
							return eq.getEquipmentClass() == ec;
						});
					}
					catch (IllegalArgumentException ex) {
						Util.msg(s, "<red>Could not find equipment class " + str);
						return;
					}
					break;
				case RARITY:
					try {
						Rarity rarity = Rarity.valueOf(str.toUpperCase());
						stream = stream.filter((eq) -> {
							return eq.getRarity() == rarity;
						});
					}
					catch (IllegalArgumentException ex) {
						Util.msg(s, "<red>Could not find rarity " + str);
						return;
					}
					break;
				case REFORGE:
					try {
						stream = stream.filter(ReforgeType.valueOf(str.toUpperCase()).filter);
					}
					catch (IllegalArgumentException ex) {
						Util.msg(s, "<red>Could not find reforge type " + str);
						return;
					}
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
		
		stream.forEach((eq) -> {
			Util.msgRaw(s, Component.text("- ", NamedTextColor.GRAY).append(eq.getHoverable()));
		});
	}
	
	private enum FilterType {
		EQUIPMENT_TYPE,
		RARITY,
		EQUIPMENT_CLASS,
		TAGS,
		REFORGE;
		
		public static FilterType fromString(String str) {
			switch (str) {
			case "--type": return EQUIPMENT_TYPE;
			case "--rarity": return RARITY;
			case "--class": return EQUIPMENT_CLASS;
			case "--tags": return TAGS;
			case "--reforge": return REFORGE;
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
