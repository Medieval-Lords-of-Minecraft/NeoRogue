package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GamblingDenChance extends ChanceSet {

	public GamblingDenChance() {
		super(new RegionType[] { RegionType.HARVEST_FIELDS, RegionType.FROZEN_WASTES }, Material.GOLD_NUGGET,
				"GamblingDen", "Gambling Den", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You hear laughter and cursing fill the air and come across a large tent. "
				+ "Inside, shady figures are playing card games and betting chips. They eye you with hostility, asking if you're joining.");

		// Choice 1: Bet 50 coins
		stage.addChoice(new ChanceChoice(Material.GOLD_NUGGET, "Bet 50 Coins",
				DescUtil.white("50%") + " chance to win " + DescUtil.white("100 coins") + ", "
						+ DescUtil.white("50%") + " chance to win nothing.",
				"You don't have 50 coins!",
				(s, inst, data) -> data.hasCoins(50),
				(s, inst, data) -> {
					Player p = data.getPlayer();
					data.addCoins(-50);
					if (NeoRogue.gen.nextBoolean()) {
						data.addCoins(100);
						Util.msgRaw(p, "The cards fall in your favor! You rake in <yellow>100 coins</yellow>.");
					}
					else {
						Util.msgRaw(p, "Lady luck isn't with you tonight. Your <yellow>50 coins</yellow> are gone.");
					}
					return null;
				}));

		// Choice 2: Bet 100 coins
		stage.addChoice(new ChanceChoice(Material.GOLD_INGOT, "Bet 100 Coins",
				DescUtil.white("25%") + " chance for a random artifact, " + DescUtil.white("25%") + " chance to win "
						+ DescUtil.white("200 coins") + ", " + DescUtil.white("25%") + " chance to lose "
						+ DescUtil.white("10%") + " health, " + DescUtil.white("25%") + " chance to win nothing.",
				"You don't have 100 coins!",
				(s, inst, data) -> data.hasCoins(100),
				(s, inst, data) -> {
					Player p = data.getPlayer();
					data.addCoins(-100);
					switch (NeoRogue.gen.nextInt(4)) {
					case 0:
						giveRandomArtifacts(s, data, 1);
						Util.msgRaw(p, "A dealer slides a curious trinket across the table. A win!");
						break;
					case 1:
						data.addCoins(200);
						Util.msgRaw(p, "The pot is yours! You scoop up <yellow>200 coins</yellow>.");
						break;
					case 2:
						loseHealth(data, 0.10);
						Util.msgRaw(p, "A sore loser shoves you hard against the table. You lose <yellow>10%</yellow> health.");
						break;
					default:
						Util.msgRaw(p, "The hand goes nowhere. Your <yellow>100 coins</yellow> are gone.");
						break;
					}
					return null;
				}));

		// Choice 3: Bet 200 coins
		stage.addChoice(new ChanceChoice(Material.GOLD_BLOCK, "Bet 200 Coins",
				DescUtil.white("25%") + " chance for " + DescUtil.white("3") + " random artifacts, "
						+ DescUtil.white("25%") + " chance to win " + DescUtil.white("400 coins") + " and fully heal, "
						+ DescUtil.white("25%") + " chance to lose a random equipment, " + DescUtil.white("25%")
						+ " chance to win nothing.",
				"You don't have 200 coins!",
				(s, inst, data) -> data.hasCoins(200),
				(s, inst, data) -> {
					Player p = data.getPlayer();
					data.addCoins(-200);
					switch (NeoRogue.gen.nextInt(4)) {
					case 0:
						giveRandomArtifacts(s, data, 3);
						Util.msgRaw(p, "Three glittering trinkets are pushed your way. What a haul!");
						break;
					case 1:
						data.addCoins(400);
						data.healPercent(1.0);
						Util.msgRaw(p, "The table erupts! You win <yellow>400 coins</yellow> and a round of healing drinks on the house.");
						break;
					case 2:
						loseRandomEquipment(data);
						break;
					default:
						Util.msgRaw(p, "The hand goes nowhere. Your <yellow>200 coins</yellow> are gone.");
						break;
					}
					return null;
				}));

		// Choice 4: Leave
		stage.addChoice(new ChanceChoice(Material.BARRIER, "Leave",
				DescUtil.white("50%") + " chance someone slices you as you leave, losing up to " + DescUtil.white("10%")
						+ " health (cannot die from this).",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					if (NeoRogue.gen.nextBoolean()) {
						loseHealth(data, 0.10);
						Util.msgRaw(p, "As you turn to go, a blade nicks your side. You lose up to <yellow>10%</yellow> health.");
					}
					else {
						Util.msgRaw(p, "You slip out of the tent without trouble.");
					}
					return null;
				}));
	}

	private static void giveRandomArtifacts(Session s, PlayerSessionData data, int count) {
		for (Artifact art : Equipment.getArtifact(data.getArtifactDroptable(), s.getBaseDropValue() + 1, count,
				data.getPlayerClass(), EquipmentClass.CLASSLESS)) {
			data.giveEquipment(art);
		}
	}

	private static void loseRandomEquipment(PlayerSessionData data) {
		Player p = data.getPlayer();
		ArrayList<EquipmentMetadata> candidates = data.aggregateEquipment(
				(meta) -> !meta.getEquipment().isCursed() && data.getRemovalRestriction(meta.getEquipment(), null, true, "lose") == null);
		if (candidates.isEmpty()) {
			Util.msgRaw(p, "They reach for your belongings, but you've nothing worth taking.");
			return;
		}
		EquipmentMetadata meta = candidates.get(NeoRogue.gen.nextInt(candidates.size()));
		Equipment lost = data.removeEquipment(meta.getEquipSlot(), meta.getSlot()).getEquipment();
		data.setupInventory();
		Util.msgRaw(p, Component.text("A sore loser swipes your ", NamedTextColor.GRAY)
				.append(lost.getHoverable())
				.append(Component.text(" and vanishes into the crowd.", NamedTextColor.GRAY)));
	}

	// Reduces health by the given fraction of max, but never below 1 (cannot kill)
	private static void loseHealth(PlayerSessionData data, double percent) {
		double target = Math.max(1, data.getHealth() - (percent * data.getMaxHealth()));
		data.setHealth(target);
	}
}
