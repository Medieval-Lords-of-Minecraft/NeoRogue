package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class GoblinHoarderChance extends ChanceSet {

	public GoblinHoarderChance() {
		super(RegionType.HARVEST_FIELDS, Material.GLASS_BOTTLE, "GoblinHoarder", "Goblin Hoarder", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID,
				"A scrawny goblin scrambles out from behind a hay bale, clutching a worn satchel. "
				+ "\"I need your potions!\" it shrieks, eyes wild with conviction. \"Doomsday is coming — "
				+ "only I can stop it, but I need the supplies!\" It offers to mend your wounds in exchange. "
				+ "If you refuse, it begrudgingly lets you pass — but not before swiping a few coins on the way out.");

		stage.addChoice(new ChanceChoice(Material.GLASS_BOTTLE, "Trade all potions",
				"Give all potions for a <yellow>+30 HP</yellow> heal.",
				"You have no potions to offer!",
				(s, inst, data) -> !data.aggregateEquipment(
						meta -> meta.getEquipment().getType() == EquipmentType.CONSUMABLE).isEmpty(),
				(s, inst, data) -> {
					Player p = data.getPlayer();
					ArrayList<EquipmentMetadata> consumables = data.aggregateEquipment(
							meta -> meta.getEquipment().getType() == EquipmentType.CONSUMABLE);
					for (EquipmentMetadata meta : consumables) {
						data.removeEquipment(meta.getEquipSlot(), meta.getSlot());
					}
					data.setupInventory();
					data.setHealth(data.getHealth() + 30);
					Util.msgRaw(p, "The goblin gleefully snatches every potion you have, then hastily patches your wounds. <green>+30 HP</green>.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> traded their potions to the goblin for a heal.", p);
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.BARRIER, "Refuse",
				"The goblin steals up to <yellow>30 coins</yellow> on the way out.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					int stolen = Math.min(30, data.getCoins());
					data.addCoins(-stolen);
					Util.msgRaw(p, "The goblin grumbles and skulks off — but not before lifting <yellow>" + stolen + " coins</yellow> from your pocket.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> refused the goblin, who stole <yellow>" + stolen + " coins</yellow> from them.", p);
					return null;
				}));
	}
}
