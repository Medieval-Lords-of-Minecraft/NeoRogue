package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;

public class VultureChance extends ChanceSet {
	private ChanceStage fightMiniboss;
	private static final ChanceChoice leave = new ChanceChoice(Material.FLINT, "Leave while you can",
			"No need to risk anything else.", (s, inst, unused) -> {
		s.broadcast("This is a disaster waiting to happen. The party sneaks away.");
		return null;
	});

	public VultureChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "Vulture");
		
		fightMiniboss = new ChanceStage(this, "miniboss", "You're cornered by a powerful foe.");
		fightMiniboss.addChoice(new ChanceChoice(Material.IRON_SWORD, "<red>You know what time it is!"));
		
		createStage(true, false, false, false);
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
		ChanceStage stage = new ChanceStage(this, init ? INIT_ID : "find" + id,
				init ? "You come across a dead adventurer lying against a wall. "
						+ "Something dangerous was nearby and it may not be a good idea to stay for long, but the adventurer "
						+ "looks to have had some useful items on them." : "You successfully loot some items from the adventurer.");

		ChanceChoice stay = new ChanceChoice(Material.FLINT, "Steal from the dead adventurer",
				"<yellow>" + failPercent + "%</yellow> chance you will fail and be forced to fight a Miniboss dealing <white>20%</white> reduced damage.",
				(s, inst, unused) -> {
					if (NeoRogue.gen.nextInt(100) < failPercent) {
						s.broadcast("<red>As you loot the body, the enemy returns!");
						inst.setNextInstance(new MinibossFightInstance(s, s.getParty().keySet(), s.getArea().getType()));
						((FightInstance) inst.getNextInstance()).addInitialTask((fi, fdata) -> {
							for (PlayerFightData pfdata : fdata) {
								pfdata.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(pfdata, 0, -0.2, BuffStatTracker.ignored("vultureChance")));
							}
						});
						return "miniboss";
					}
					else {
						ItemFound nextFind = chooseNextFind(foundArtifact, foundHeal, foundEquipment, remaining);
						s.broadcast(nextFind.desc);
						nextFind.action.run(s);
						return "find" + getIdAfter(nextFind, foundArtifact, foundHeal, foundEquipment);
					}
				});
		

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
		ARTIFACT("You find an artifact for your troubles!", (s) -> {
			for (PlayerSessionData data : s.getParty().values()) {
				Artifact drop = Equipment.getArtifact(data.getArtifactDroptable(), s.getAreasCompleted() + 1, 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0);
				data.giveEquipment(drop);
			}
		}),
		HEAL("You get healed for <yellow>25%</yellow> of your max health!", (s) -> {
			for (PlayerSessionData data : s.getParty().values()) {
				data.healPercent(0.25);
			}
		}),
		EQUIPMENT("You find some equipment for your troubles!", (s) -> {
			for (PlayerSessionData data : s.getParty().values()) {
				Equipment eq = Equipment.getDrop(s.getAreasCompleted() + 1, 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0);
				data.giveEquipment(s.rollUpgrade(eq));
			}
		});
		private String desc;
		private RunnableFind action;

		private ItemFound(String desc, RunnableFind action) {
			this.desc = desc;
			this.action = action;
		}
	}
	
	private interface RunnableFind {
		public void run(Session s);
	}
}
