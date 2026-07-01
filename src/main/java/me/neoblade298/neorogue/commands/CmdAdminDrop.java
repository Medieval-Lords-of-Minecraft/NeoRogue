package me.neoblade298.neorogue.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerData;
import me.neoblade298.neorogue.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CmdAdminDrop extends Subcommand {
	private static final List<String> TYPES = List.of("equipment", "artifact", "consumable");

	public CmdAdminDrop(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
		this.overrideTabHandler();
		ArrayList<String> ecTab = new ArrayList<>();
		for (EquipmentClass ec : EquipmentClass.values()) ecTab.add(ec.name());
		args.add(new Arg("type"), new Arg("player", false), new Arg("value"), new Arg("amount", false), new Arg("classes...").setTabOptions(ecTab));
	}

	@Override
	public List<String> getTabOptions(CommandSender s, String[] args) {
		if (args.length <= 1) return TYPES;
		if (args.length == 2) {
			ArrayList<String> players = new ArrayList<>();
			for (Player p : Bukkit.getOnlinePlayers()) players.add(p.getName());
			return players;
		}
		ArrayList<String> ecs = new ArrayList<>();
		for (EquipmentClass ec : EquipmentClass.values()) ecs.add(ec.name());
		return ecs;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length < 2) {
			Util.msgRaw(s, "<red>Usage: drop <type> [player] <value> [amount] [classes...]");
			return;
		}

		String typeArg = args[0].toLowerCase();
		if (!TYPES.contains(typeArg)) {
			Util.msgRaw(s, "<red>Invalid type: " + typeArg + ". Use: equipment, artifact, consumable");
			return;
		}

		// Determine target player and index where value starts
		Player target;
		int offset = 1;
		if (Bukkit.getPlayer(args[1]) != null) {
			target = Bukkit.getPlayer(args[1]);
			offset = 2;
		} else if (s instanceof Player) {
			target = (Player) s;
		} else {
			Util.msgRaw(s, "<red>Must specify a player when running from console!");
			return;
		}

		if (args.length <= offset) {
			Util.msgRaw(s, "<red>Usage: drop <type> [player] <value> [amount] [classes...]");
			return;
		}

		int value;
		try {
			value = Integer.parseInt(args[offset]);
		} catch (NumberFormatException e) {
			Util.msgRaw(s, "<red>Invalid value: " + args[offset]);
			return;
		}

		// Optional amount — if next arg parses as a number it's the amount, otherwise start of classes
		int amount = 1;
		int classOffset = offset + 1;
		if (args.length > classOffset) {
			try {
				amount = Integer.parseInt(args[classOffset]);
				classOffset++;
			} catch (NumberFormatException ignored) {
				// Not a number; treat as beginning of classes
			}
		}

		EquipmentClass[] ecs;
		if (args.length > classOffset) {
			ecs = new EquipmentClass[args.length - classOffset];
			for (int i = classOffset; i < args.length; i++) {
				try {
					ecs[i - classOffset] = EquipmentClass.valueOf(args[i].toUpperCase());
				} catch (IllegalArgumentException e) {
					Util.msgRaw(s, "<red>Invalid equipment class: " + args[i]);
					return;
				}
			}
		} else {
			ecs = new EquipmentClass[] { EquipmentClass.CLASSLESS };
		}

		PlayerData pd = PlayerManager.getPlayerData(target.getUniqueId());
		if (pd == null) {
			Util.msgRaw(s, "<red>No player data found for " + target.getName() + ".");
			return;
		}

		ArrayList<? extends Equipment> drops;
		switch (typeArg) {
			case "equipment": {
				DropTableSet<Equipment> set = pd.getEquipmentDroptable();
				drops = Equipment.getDrop(set, value, amount, ecs);
				break;
			}
			case "artifact": {
				DropTableSet<Artifact> set = pd.getArtifactDroptable();
				drops = Equipment.getArtifact(set, value, amount, ecs);
				break;
			}
			case "consumable": {
				DropTableSet<Consumable> set = pd.getConsumableDroptable();
				drops = set.getMultiple(value, amount, ecs);
				break;
			}
			default:
				return;
		}

		Util.msgRaw(s, "<yellow>Rolled " + amount + " " + typeArg + " drop(s) at value " + value + " for " + target.getName() + ":");
		for (Equipment eq : drops) {
			Util.msgRaw(s, Component.text("- ", NamedTextColor.GRAY).append(eq.getHoverable()));
		}
	}
}
