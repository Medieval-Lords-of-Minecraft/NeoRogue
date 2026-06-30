package me.neoblade298.neorogue.session.chance.builtin;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.artifacts.EchoStone;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.player.inventory.EquipmentSelectInventory;
import me.neoblade298.neorogue.player.inventory.GlossaryIcon;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceInventory;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SageOfTheMountainChance extends ChanceSet {

	public SageOfTheMountainChance() {
		super(RegionType.FROZEN_WASTES, Material.ECHO_SHARD, "SageOfTheMountain", "Sage of the Mountain", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "After wandering into a cave to escape from the blizzard, you encounter "
				+ "an old sage and his few possessions. He looks as though he's been expecting you.");

		// Choice 1: Gaze into the Beyond - store a passive ability in a slot-free Echo Stone
		ChanceChoice gaze = new ChanceChoice(Material.ENDER_EYE, "Gaze into the Beyond",
				"Pay " + DescUtil.white("30%") + " of your current health to store a passive ability in an "
						+ "Echo Stone, converting it into an artifact.",
				"You have no passive abilities to store!",
				(s, inst, data) -> data.aggregateEquipment(SageOfTheMountainChance::isStorablePassive).size() > 0,
				(s, inst, data) -> null); // Never runs; interactive action handles resolution
		gaze.setOnInteract((prev, data) -> openGaze(prev, data, stage));
		stage.addChoice(gaze);

		// Choice 2: Meditate Together - heal 20% of max health
		stage.addChoice(new ChanceChoice(Material.AMETHYST_SHARD, "Meditate Together",
				"Heal " + DescUtil.white("20%") + " of your max health.",
				(s, inst, data) -> {
					data.healPercent(0.2);
					Util.msgRaw(data.getPlayer(), "You sit in silence with the sage. A gentle warmth spreads through your weary body.");
					return null;
				}));
	}

	private void openGaze(ChanceInventory prev, PlayerSessionData data, ChanceStage stage) {
		Player p = data.getPlayer();
		ChanceInstance inst = prev.getInst();
		Session s = inst.getSession();
		UUID uuid = p.getUniqueId();

		new EquipmentSelectInventory(data, Component.text("Gaze into the Beyond", NamedTextColor.GOLD),
				data.aggregateEquipment(SageOfTheMountainChance::isStorablePassive),
				(meta) -> {
					SessionEquipment held = data.removeEquipment(meta.getEquipSlot(), meta.getSlot());
					data.setupInventory();
					data.setHealth(data.getHealth() * 0.7);
					data.giveArtifact(new ArtifactInstance((Artifact) EchoStone.get(), 1, held));
					Util.msg(p, Component.text("The sage's eyes glaze over as he channels your ", NamedTextColor.GRAY)
							.append(held.getEquipment().getHoverable())
							.append(Component.text(" into a humming Echo Stone.", NamedTextColor.GRAY)));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> gazed into the beyond.", p);
					inst.advanceStage(uuid, null);
					s.getInstance().updateBoardLines();
					p.closeInventory();
				},
				() -> new ChanceInventory(p, inst, this, stage));
	}

	// Eligible to store: any owned equipment carrying the Passive glossary tag
	private static boolean isStorablePassive(EquipmentMetadata meta) {
		for (GlossaryIcon tag : meta.getEquipment().getTags()) {
			if (tag == GlossaryTag.PASSIVE) return true;
		}
		return false;
	}
}
