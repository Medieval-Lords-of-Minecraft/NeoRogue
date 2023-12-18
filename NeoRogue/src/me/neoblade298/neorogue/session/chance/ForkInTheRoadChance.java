package me.neoblade298.neorogue.session.chance;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.CampfireInstance;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class ForkInTheRoadChance extends ChanceSet {

	public ForkInTheRoadChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "ForkInTheRoad", "Fork in the road");
		ChanceStage stage = new ChanceStage(this, "init", "There seems to be a new lightly-traveled path that isn't on your map."
				+ " It could lead to a nice new campfire spot or potentially some enemies.");
		setInitialStage(stage);
		
		ChanceStage fight = new ChanceStage(this, "fight", "The path led you straight to enemies. You must fight.");
		fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
		fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
		fight.addChoice(new ChanceChoice(Material.GREEN_WOOL, "<green>Welp, but green"));

		ChanceChoice choice = new ChanceChoice(Material.COBBLESTONE, "Let's risk it",
				"<gray><yellow>50% </yellow>chance to arrive at a campfire, <yellow>50% </yellow>chance to encounter a fight",
				(s, inst) -> {
					boolean isCampfire = NeoRogue.gen.nextBoolean();
					if (isCampfire) {
						s.broadcast("The fork takes you to a nice open space by a river. Nice!");
						inst.setNextInstance(new CampfireInstance());
						return null;
					}
					else {
						s.broadcast("Looks like the path was made by enemies, and you just walked straight into their lair.");
						inst.setNextInstance(new StandardFightInstance(s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited()));
						return fight.getId();
					}
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.STONE_BRICKS, "Let's not",
				"Stay on your path and avoid any risk.",
				(s, inst) -> {
					s.broadcast("You were never a fan of Robert Frost's works anyway. You get back to walking.");
					return null;
				});
		stage.addChoice(choice);
	}
}
