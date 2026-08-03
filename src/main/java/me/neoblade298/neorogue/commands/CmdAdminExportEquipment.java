package me.neoblade298.neorogue.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.commands.EquipmentCategoryClassifier.Classification;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;

public class CmdAdminExportEquipment extends Subcommand {
	public CmdAdminExportEquipment(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		Path output = NeoRogue.inst().getDataFolder().toPath().resolve("equipment.csv");
		try {
			Files.createDirectories(output.getParent());
			try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
				writeHeader(writer);
				for (Equipment equipment : Equipment.getAll()) {
					writeEquipment(writer, equipment);
				}
			}
			Util.msgRaw(sender, "Exported <yellow>" + Equipment.getAll().size()
					+ "</yellow> equipment entries to <white>" + output.toAbsolutePath() + "</white>.");
		} catch (IOException ex) {
			NeoRogue.inst().getLogger().log(java.util.logging.Level.SEVERE, "Failed to export equipment CSV", ex);
			Util.msgRaw(sender, "<red>Failed to export equipment: " + ex.getMessage());
		}
	}

	private static void writeHeader(BufferedWriter writer) throws IOException {
		List<String> columns = new ArrayList<>();
		columns.add("id");
		columns.add("rarity");
		columns.add("droppable");
		columns.add("equipment_classes");
		columns.add("reforge_status");
		columns.add("type");
		columns.add("isOffense");
		columns.add("isDefense");
		for (GlossaryTag tag : GlossaryTag.values()) {
			columns.add(tag.name().toLowerCase());
		}
		writeRow(writer, columns);
	}

	private static void writeEquipment(BufferedWriter writer, Equipment equipment) throws IOException {
		List<String> values = new ArrayList<>();
		Classification classification = EquipmentCategoryClassifier.classify(equipment);
		values.add(equipment.getId());
		values.add(equipment.getRarity().name());
		values.add(Boolean.toString(isDroppable(equipment)));
		values.add(joinClasses(equipment.getEquipmentClasses()));
		values.add(getReforgeStatus(equipment));
		values.add(equipment.getType().name());
		values.add(Boolean.toString(classification.isOffense()));
		values.add(Boolean.toString(classification.isDefense()));
		for (GlossaryTag tag : GlossaryTag.values()) {
			values.add(equipment.getTags().contains(tag) ? "1" : "0");
		}
		writeRow(writer, values);
	}

	private static boolean isDroppable(Equipment equipment) {
		return equipment.canDrop()
				&& (equipment.getReforgeParents().isEmpty() || equipment.overridesReforgeDrop());
	}

	private static String joinClasses(EquipmentClass[] classes) {
		List<String> names = new ArrayList<>();
		for (EquipmentClass equipmentClass : classes) {
			names.add(equipmentClass.name());
		}
		return String.join("|", names);
	}

	private static String getReforgeStatus(Equipment equipment) {
		boolean parent = !equipment.getReforgeOptions().isEmpty();
		boolean child = !equipment.getReforgeParents().isEmpty();
		if (parent && child) return "PARENT_AND_CHILD";
		if (parent) return "PARENT";
		if (child) return "CHILD";
		return "NEITHER";
	}

	private static void writeRow(BufferedWriter writer, List<String> values) throws IOException {
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) writer.write(',');
			writer.write(csv(values.get(i)));
		}
		writer.newLine();
	}

	private static String csv(String value) {
		if (value.indexOf(',') < 0 && value.indexOf('"') < 0
				&& value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
			return value;
		}
		return '"' + value.replace("\"", "\"\"") + '"';
	}
}