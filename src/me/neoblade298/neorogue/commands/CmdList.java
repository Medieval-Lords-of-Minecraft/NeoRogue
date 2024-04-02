package me.neoblade298.neorogue.commands;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.session.Instance;
import me.neoblade298.neorogue.session.LobbyInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.SessionManager;

public class CmdList extends Subcommand {

	public CmdList(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.overrideTabHandler();
		args.setOverride("{--type EquipmentType} {--rarity Rarity} {--class EquipmentClass} {--tags GlossaryTag1,Tag2...}"
				+ " {--reforge Parent/Child/None}");
		args.setMax(-1);
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
					EquipmentType type = EquipmentType.valueOf(str.toUpperCase());
					stream.filter((eq) -> {
						return eq.getType() == type;
					});
					break;
				case EQUIPMENT_CLASS:
					EquipmentClass ec = EquipmentClass.valueOf(str.toUpperCase());
					stream.filter((eq) -> {
						return eq.getEquipmentClass() == ec;
					});
					break;
				case RARITY:
					Rarity rarity = Rarity.valueOf(str.toUpperCase());
					stream.filter((eq) -> {
						return eq.getRarity() == rarity;
					});
					break;
				case REFORGE:
					stream.filter(null)
					break;
				case TAGS:
					break;
				default:
					break;
				}
				filter = null;
			}
		}
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
			return true;
		}),
		CHILD((eq) -> {
			return true;
		}),
		NONE((eq) -> {
			return true;
		});
		
		private Predicate<Equipment> filter;
		private ReforgeType(Predicate<Equipment> filter) {
			this.filter = filter;
		}
	}
}
