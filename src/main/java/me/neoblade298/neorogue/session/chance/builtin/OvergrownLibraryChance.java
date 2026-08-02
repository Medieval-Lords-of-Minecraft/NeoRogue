package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.artifacts.DarkArtsTreatise;
import me.neoblade298.neorogue.equipment.artifacts.EarthenTome;
import me.neoblade298.neorogue.equipment.artifacts.HolyScriptures;
import me.neoblade298.neorogue.equipment.artifacts.InfernalTome;
import me.neoblade298.neorogue.equipment.artifacts.Pumped;
import me.neoblade298.neorogue.equipment.artifacts.ScrollOfFrost;
import me.neoblade298.neorogue.equipment.artifacts.TreatiseOnElectricity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class OvergrownLibraryChance extends ChanceSet {

	public OvergrownLibraryChance() {
		super(RegionType.HARVEST_FIELDS, Material.LECTERN, "OvergrownLibrary", "Overgrown Library", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "Along the forest path is an overgrown stone wall that seemed to have one point " +
			"been a shelf. On it are some books that, while old, seem surprisingly well-kept.");

		stage.addChoice(equipmentChoice(Material.BLAZE_POWDER, "Read \"Infernal Tome\"",
				"Permanently increase " + GlossaryTag.FIRE.tag + " by <white>20%</white>, stackable",
				InfernalTome.get()));

		stage.addChoice(equipmentChoice(Material.BLUE_ICE, "Read \"Scroll of Frost\"",
				"Permanently increase " + GlossaryTag.ICE.tag + " by <white>20%</white>, stackable",
				ScrollOfFrost.get()));

		stage.addChoice(equipmentChoice(Material.LIGHTNING_ROD, "Read \"Treatise on Electricity\"",
				"Permanently increase " + GlossaryTag.LIGHTNING.tag + " by <white>20%</white>, stackable",
				TreatiseOnElectricity.get()));

		stage.addChoice(equipmentChoice(Material.GRASS_BLOCK, "Read \"Earthen Tome\"",
				"Permanently increase " + GlossaryTag.EARTHEN.tag + " by <white>20%</white>, stackable",
				EarthenTome.get()));

		stage.addChoice(equipmentChoice(Material.NETHER_STAR, "Read \"Holy Scriptures\"",
				"Permanently increase " + GlossaryTag.LIGHT.tag + " by <white>20%</white>, stackable",
				HolyScriptures.get()));

		stage.addChoice(equipmentChoice(Material.OBSIDIAN, "Read \"Dark Arts Treatise\"",
				"Permanently increase " + GlossaryTag.DARK.tag + " by <white>20%</white>, stackable",
				DarkArtsTreatise.get()));

		ChanceChoice pushups = new ChanceChoice(Material.IRON_BARS, "Reading is for nerds, I'll do some pushups",
				"Increase your strength by <white>25</white> for <white>2</white> fights.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					data.giveArtifact((Artifact) Pumped.get(), 2);
					s.broadcast(SharedUtil.color("<yellow>" + p.getName() + "</yellow> thinks that reading is for nerds."));
					return null;
				});
		pushups.addGlossaryEquipment(Pumped.get());
		stage.addChoice(pushups);
	}

	private static ChanceChoice equipmentChoice(Material material, String title, String description,
			Equipment equipment) {
		ChanceChoice choice = new ChanceChoice(material, title, description, (s, inst, data) -> {
			data.giveEquipment(equipment);
			return null;
		});
		choice.addGlossaryEquipment(equipment);
		return choice;
	}
}
