package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.artifacts.EmeraldCluster;
import me.neoblade298.neorogue.equipment.artifacts.RubyCluster;
import me.neoblade298.neorogue.equipment.artifacts.SapphireCluster;
import me.neoblade298.neorogue.player.inventory.CustomGlossaryIcon;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class ForgottenWellChance extends ChanceSet {

	public ForgottenWellChance() {
		super(RegionType.LOW_DISTRICT, Material.COBBLESTONE_WALL, "ForgottenWell", "Forgotten Well", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You come across an unassuming mossy well, but there is some sort of magic emanating from it."
				+ " An old rotted sign nearby has \"Make a wish\" written on it.");

		ChanceChoice ruby = new ChanceChoice(Material.REDSTONE, "I wish to become more durable.",
				"Acquire one <blue>Ruby Cluster</blue>.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msgRaw(p, "You feel a gust of wind, and suddenly you feel just a little tougher.");
					data.giveEquipment(RubyCluster.get());
					return null;
				});
		ChanceChoice emerald = new ChanceChoice(Material.EMERALD, "I wish to become more agile.",
				"Acquire one <blue>Emerald Cluster</blue>.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msgRaw(p, "You feel a gust of wind, and suddenly your body feels just a little lighter.");
					data.giveEquipment(EmeraldCluster.get());
					return null;
				});
		ChanceChoice sapphire = new ChanceChoice(Material.LAPIS_LAZULI, "I wish to become more mindful.",
				"Acquire one <blue>Sapphire Cluster</blue>.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msgRaw(p, "You feel a gust of wind, and suddenly your mind feels just a little more insightful.");
					data.giveEquipment(SapphireCluster.get());
					return null;
				});
		stage.addChoice(new ChanceChoice(Material.GOLD_NUGGET, "I wish to become rich.",
				"Acquire <yellow>100 coins</yellow>.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msgRaw(p, "You feel a gust of wind, and then spot <yellow>100 coins</yellow> laying on the ground.");
					data.addCoins(100);
					return null;
				}));
				
		
		ruby.addTag(new CustomGlossaryIcon("rubyCluster", RubyCluster.get().getItem()));
		emerald.addTag(new CustomGlossaryIcon("emeraldCluster", EmeraldCluster.get().getItem()));
		sapphire.addTag(new CustomGlossaryIcon("sapphireCluster", SapphireCluster.get().getItem()));
		stage.addChoice(ruby);
		stage.addChoice(emerald);
		stage.addChoice(sapphire);
	}
}
