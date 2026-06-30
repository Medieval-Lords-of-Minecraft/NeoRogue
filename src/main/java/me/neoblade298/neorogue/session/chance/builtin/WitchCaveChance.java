package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.artifacts.EverlastingHealth;
import me.neoblade298.neorogue.equipment.artifacts.TemporaryHealth;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class WitchCaveChance extends ChanceSet {

	private static final int LEAVE_COST = 50;

	public WitchCaveChance() {
		super(RegionType.FROZEN_WASTES, Material.HONEY_BOTTLE, "WitchCave", "Witch Cave", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You wander into a frost-rimed cave where an old witch cackles by a bubbling cauldron. "
				+ "She turns to greet you with a knowing smile. \"A potion for the road, dearie? Or would you rather pay your way out?\"");

		stage.addChoice(new ChanceChoice(Material.HONEY_BOTTLE, "Everlasting Health",
				"Heal " + DescUtil.white("20%") + " of your max health, but you can no longer gain " + DescUtil.white("shields")
						+ " after " + DescUtil.white("10s") + " of a fight.",
				(s, inst, data) -> {
					data.healPercent(0.2);
					data.giveArtifact((Artifact) EverlastingHealth.get(), 1);
					Util.msgRaw(data.getPlayer(), "The witch presses a warm vial into your hand. You feel energized, but oddly fragile.");
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.SPLASH_POTION, "Temporary Health",
				"Take " + DescUtil.white("10%") + " of your max health as damage, but heal " + DescUtil.white("30%")
						+ " after winning " + DescUtil.white("2") + " fights.",
				"You need at least 10% health to drink the brew!",
				(s, inst, data) -> data.getHealth() / data.getMaxHealth() >= 0.10,
				(s, inst, data) -> {
					data.damagePercent(0.10);
					data.giveArtifact((Artifact) TemporaryHealth.get(), 2);
					Util.msgRaw(data.getPlayer(), "The bitter brew stings going down, but you convince yourself that bitter things are healthier for you in the long run.");
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.GOLD_INGOT, "Leave",
				"Pay " + DescUtil.white(LEAVE_COST + " coins") + " to convince the witch to let you live.",
				"You don't have " + LEAVE_COST + " coins!",
				(s, inst, data) -> data.hasCoins(LEAVE_COST),
				(s, inst, data) -> {
					data.addCoins(-LEAVE_COST);
					Util.msgRaw(data.getPlayer(), "You toss the witch some coins. She waves you off with a chuckle, and you slip back into the cold.");
					return null;
				}));
	}
}
