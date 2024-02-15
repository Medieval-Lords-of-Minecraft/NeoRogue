package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;

public class ThiefsCacheChance extends ChanceSet {
	private static final Equipment relic = Equipment.get("dullGem", false);

	public ThiefsCacheChance() {
		super(AreaType.LOW_DISTRICT, Material.GOLD_INGOT, "ThiefsCache", "Thief's Cache", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You come across a thief’s cache that has some gold, equipment, and a gem lying around. "
				+ "There's also a note seemingly written by the thief pondering about the existence of some lost relic to \"complete the set\", "
				+ "but its meaning is lost to you. Suddenly you hear noise behind you. The thief is coming and you’ll have to act fast.");

		ChanceChoice choice = new ChanceChoice(Material.PRISMARINE_SHARD, "Pick up the dull gem",
				"Maybe this can come in handy reforging an old weapon.", (s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msg(p, "You pick up the dull gem, hoping it'll come in handy in the future.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to pick up the dull gem!", p);
					data.giveEquipment(relic);
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.LEATHER_CHESTPLATE, "Pick up the gear",
				"Receive a random piece of equipment.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Equipment eq = Equipment.getDrop(s.getAreasCompleted(), data.getPlayerClass());
					Util.msg(p, Component.text("You pick up a(n) ").append(eq.getDisplay())
							.append(Component.text(" and go on your way.")));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to receive a random piece of equipment!", p);
					data.giveEquipment(eq);
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.GOLD_NUGGET, "Pick up the gold",
				"Receive <yellow>100 coins</yellow>.", (s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msg(p, "You pick up <yellow>100 coins</yellow> and go on your way.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to pick up the <yellow>100 coins</yellow>!", p);
					data.addCoins(100);
					return null;
				});
		stage.addChoice(choice);
	}
}
