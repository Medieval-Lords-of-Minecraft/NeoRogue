package me.neoblade298.neorogue.session.chance;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.CampfireInstance;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class VultureChance extends ChanceSet {

	public VultureChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "Vulture", "Vulture");
		ChanceStage stage = new ChanceStage(this, "init", "You come across a dead adventurer lying against a wall. "
				+ "Something dangerous was nearby and it may not be a good idea to stay for long, but the adventurer "
				+ "looks to have had some useful items on them.");
		setInitialStage(stage);

		ChanceChoice choice = new ChanceChoice(Material.FLINT, "Steal some items",
				"<gray><yellow>50% </yellow>chance to arrive at a campfire, <yellow>50% </yellow>chance to encounter a fight", "",
				(s, inst, run) -> {
					if (!run) return true;

					if (isCampfire) {
						s.broadcast("The fork takes you to a nice open space by a river. Nice!");
						inst.setNextInstance(new CampfireInstance());
					}
					else {
						s.broadcast("Looks like the path was made by enemies, and you just walked straight into their lair.");
						inst.setNextInstance(new StandardFightInstance(s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited()));
					}
					return true;
				});
		stage.addChoice(choice);
		if (!isCampfire) {
			ChanceStage fight = new ChanceStage(this, "fight", "The path led you straight to enemies. You must fight.");
			choice.setResult(fight);
			fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
			fight.addChoice(new ChanceChoice(Material.BLUE_WOOL, "Welp"));
			fight.addChoice(new ChanceChoice(Material.GREEN_WOOL, "<green>Welp, but green"));
		}
		
		choice = new ChanceChoice(Material.STONE_BRICKS, "Let's not",
				"Stay on your path and avoid any risk.", "",
				(s, inst, run) -> {
					if (!run) return true;

					s.broadcast("You were never a fan of Robert Frost's works anyway. You get back to walking.");
					return true;
				});
		stage.addChoice(choice);
	}
	
	private void createStage(ItemFound found, boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		int id = getId(foundArtifact, foundHeal, foundEquipment);
		int failPercent = 25 + (foundArtifact ? 25 : 0) + (foundHeal ? 25 : 0) + (foundEquipment ? 25 : 0);
		ChanceStage stage = new ChanceStage(this, "find" + id, found.getDesc());
		
		ChanceChoice choice = new ChanceChoice(Material.FLINT, "Steal from the dead adventurer",
				"<yellow>" + failPercent + "%</yellow> chance you will fail and be forced to fight a Miniboss.", null,
				(s, inst, run) -> {
					if (!run) return true;

					s.broadcast("You were never a fan of Robert Frost's works anyway. You get back to walking.");
					return true;
				});
	}
	
	private int getId(boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		return (foundArtifact ? 4 : 0) + (foundHeal ? 2 : 0) + (foundEquipment ? 1 : 0);
	}
	
	public int getIdAfter(ItemFound found, boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		switch (found) {
		case ARTIFACT: foundArtifact = true;
		break;
		case HEAL: foundHeal = true;
		break;
		case EQUIPMENT: foundEquipment = true;
		break;
		}
		return getId(foundArtifact, foundHeal, foundEquipment);
	}
	
	private enum ItemFound {
		ARTIFACT("You find an artifact for your troubles!"),
		HEAL("You get healed for <yellow>25%</yellow> of your max health!"),
		EQUIPMENT("You find some equipment for your troubles!");
		private String desc;
		private ItemFound(String desc) { 
			this.desc = desc;
		}
		
		public String getDesc() {
			return desc;
		}
	}
}
