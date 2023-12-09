package me.neoblade298.neorogue.session.chance;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.CampfireInstance;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class ForkInTheRoadChance extends ChanceSet {

	public ForkInTheRoadChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "ForkInTheRoad", "Fork in the road");
		ChanceStage stage = new ChanceStage("init", "There seems to be a new lightly-traveled path that isn't on your map."
				+ " It could lead to a nice new campfire spot or potentially some enemies.");
		setInitialStage(stage);

		boolean isCampfire = NeoCore.gen.nextBoolean();
		ChanceChoice choice = new ChanceChoice(Material.COBBLESTONE, "Let's risk it",
				"<gray><yellow>50% </yellow>chance to arrive at a campfire, <yellow>50% </yellow>chance to encounter a fight", "",
				(s, run) -> {
					if (!run) return true;

					if (isCampfire) {
						s.broadcast("The fork takes you to a nice open space by a river. Nice!");
						((ChanceInstance) s.getInstance()).setNextInstance(new CampfireInstance());
					}
					else {
						s.broadcast("Looks like the path was made by enemies, and you just walked straight into their lair.");
						((ChanceInstance) s.getInstance()).setNextInstance(new StandardFightInstance(s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited()));
					}
					return true;
				});
		stage.addChoice(choice);
		if (!isCampfire) {
			ChanceStage fight = new ChanceStage("fight", "The path led you straight to enemies. You must fight.");
			choice.setResult(fight);
			fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
			fight.addChoice(new ChanceChoice(Material.BLUE_WOOL, "Welp"));
			fight.addChoice(new ChanceChoice(Material.GREEN_WOOL, "<green>Welp, but green"));
		}
		
		choice = new ChanceChoice(Material.STONE_BRICKS, "Let's not",
				"Stay on your path and avoid any risk.", "",
				(s, run) -> {
					if (!run) return true;

					s.broadcast("You were never a fan of Robert Frost's works anyway. You get back to walking.");
					return true;
				});
		stage.addChoice(choice);
	}
}
