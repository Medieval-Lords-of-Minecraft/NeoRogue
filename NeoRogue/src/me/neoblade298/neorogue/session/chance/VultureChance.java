package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;

public class VultureChance extends ChanceSet {
	private ChanceStage fightMiniboss;

	public VultureChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "Vulture", "Vulture");
		
		fightMiniboss = new ChanceStage(this, "miniboss", "You're cornered by a powerful foe.");
		fightMiniboss.addChoice(new ChanceChoice(Material.IRON_SWORD, "<red>You know what time it is!"));
		
		setInitialStage(createStage(true, false, false, false));
		boolean[] b = new boolean[] {false, true};
		for (boolean foundArtifact : b) {
			for (boolean foundHeal : b) {
				for (boolean foundEquipment : b) {
					createStage(false, foundArtifact, foundHeal, foundEquipment);
				}
			}
		}
	}

	private ChanceStage createStage(boolean init, boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		int id = getId(foundArtifact, foundHeal, foundEquipment);
		int remaining = getRemaining(foundArtifact, foundHeal, foundEquipment);
		int failPercent = 25 + (remaining * 25);
		ChanceStage stage = new ChanceStage(this, init ? "init" : "find" + id,
				init ? "You come across a dead adventurer lying against a wall. "
						+ "Something dangerous was nearby and it may not be a good idea to stay for long, but the adventurer "
						+ "looks to have had some useful items on them." : "You successfully loot some items from the adventurer.");

		ChanceChoice stay = new ChanceChoice(Material.FLINT, "Steal from the dead adventurer",
				"<yellow>" + failPercent + "%</yellow> chance you will fail and be forced to fight a Miniboss.",
				(s, inst) -> {
					if (NeoRogue.gen.nextInt(100) < failPercent) {
						s.broadcast("<red>As you loot the body, the enemy returns!");
						inst.setNextInstance(new MinibossFightInstance(s.getParty().keySet(), s.getArea().getType()));
						return null;
					}
					else {
						ItemFound nextFind = chooseNextFind(foundArtifact, foundHeal, foundEquipment, remaining);
						s.broadcast(nextFind.getDesc());
						return "found" + getIdAfter(nextFind, foundArtifact, foundHeal, foundEquipment);
					}
				});
		

		ChanceChoice leave = new ChanceChoice(Material.FLINT, "Leave while you can",
				"No need to risk anything else.");
		stage.addChoice(stay);
		stage.addChoice(leave);
		return stage;
	}

	private int getId(boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		return (foundArtifact ? 4 : 0) + (foundHeal ? 2 : 0) + (foundEquipment ? 1 : 0);
	}

	public int getRemaining(boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		return (foundArtifact ? 1 : 0) + (foundHeal ? 1 : 0) + (foundEquipment ? 1 : 0);
	}

	public ItemFound chooseNextFind(boolean foundArtifact, boolean foundHeal, boolean foundEquipment, int remaining) {
		ArrayList<ItemFound> finds = new ArrayList<ItemFound>(3);
		if (!foundHeal) finds.add(ItemFound.HEAL);
		if (!foundArtifact) finds.add(ItemFound.ARTIFACT);
		if (!foundEquipment) finds.add(ItemFound.EQUIPMENT);
		return finds.get(NeoRogue.gen.nextInt(finds.size()));
	}

	public int getIdAfter(ItemFound found, boolean foundArtifact, boolean foundHeal, boolean foundEquipment) {
		switch (found) {
		case ARTIFACT:
			foundArtifact = true;
			break;
		case HEAL:
			foundHeal = true;
			break;
		case EQUIPMENT:
			foundEquipment = true;
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