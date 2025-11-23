package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.artifacts.DarkArtsTreatise;
import me.neoblade298.neorogue.equipment.artifacts.EarthenTome;
import me.neoblade298.neorogue.equipment.artifacts.HolyScriptures;
import me.neoblade298.neorogue.equipment.artifacts.InfernalTome;
import me.neoblade298.neorogue.equipment.artifacts.Pumped;
import me.neoblade298.neorogue.equipment.artifacts.ScrollOfFrost;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class OvergrownLibraryChance extends ChanceSet {

	public OvergrownLibraryChance() {
		super(AreaType.HARVEST_FIELDS, Material.LECTERN, "OvergrownLibrary", "Overgrown Library");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "Along the forest path is an overgrown stone wall that seemed to have one point " +
			"been a shelf. On it are some books that, while old, seem surprisingly well-kept.");

		stage.addChoice(new ChanceChoice(Material.BLAZE_POWDER, "Read \"Infernal Tome\"",
				"Permanently increase " + GlossaryTag.FIRE.tag + " by <white>20%</white>",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(InfernalTome.get());
					}
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.BLUE_ICE, "Read \"Scroll of Frost\"",
				"Permanently increase " + GlossaryTag.ICE.tag + " by <white>20%</white>",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(ScrollOfFrost.get());
					}
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.LIGHTNING_ROD, "Read \"Treatise on Electricity\"",
				"Permanently increase " + GlossaryTag.LIGHTNING.tag + " by <white>20%</white>",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(ScrollOfFrost.get());
					}
					return null;
		}));

		stage.addChoice(new ChanceChoice(Material.GRASS_BLOCK, "Read \"Earthen Tome\"",
				"Permanently increase " + GlossaryTag.EARTHEN.tag + " by <white>20%</white>",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(EarthenTome.get());
					}
					return null;
		}));

		stage.addChoice(new ChanceChoice(Material.NETHER_STAR, "Read \"Holy Scriptures\"",
				"Permanently increase " + GlossaryTag.LIGHT.tag + " by <white>20%</white>",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(HolyScriptures.get());
					}
					return null;
		}));

		stage.addChoice(new ChanceChoice(Material.OBSIDIAN, "Read \"Dark Arts Treatise\"",
				"Permanently increase " + GlossaryTag.DARK.tag + " by <white>20%</white>",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(DarkArtsTreatise.get());
					}
					return null;
		}));

		stage.addChoice(new ChanceChoice(Material.IRON_SWORD, "Reading is for nerds, I'll do some pushups",
				"Increase your strength by <white>50</white> for <white>2</white> fights.",
				(s, inst, data) -> {
					for (PlayerSessionData psd : s.getParty().values()) {
						psd.giveEquipment(Pumped.get());
					}
					Player p = data.getPlayer();
					data.giveArtifact((Artifact) Pumped.get(), 2);
					s.broadcast(SharedUtil.color("<yellow>" + p.getName() + "</yellow> thinks that reading is for nerds."));
					return null;
				}));
	}
}
