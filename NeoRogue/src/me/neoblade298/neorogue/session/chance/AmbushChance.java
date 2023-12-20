package me.neoblade298.neorogue.session.chance;

import org.bukkit.Material;

import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class AmbushChance extends ChanceSet {

	public AmbushChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "Ambush", "Ambush");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You catch sight of a group of enemies. Looks like they haven't noticed you yet.");
		ChanceStage fight = new ChanceStage(this, "fight", "The path led you straight to enemies. You must fight.");
		fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
		fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
		fight.addChoice(new ChanceChoice(Material.GREEN_WOOL, "<green>Welp, but green"));

		stage.addChoice(new ChanceChoice(Material.DIAMOND_SWORD, "Gather yourself",
				"Start the fight, but at max mana and stamina.",
				(s, inst) -> {
					s.broadcast("You get centered and are well-prepared for the fight before charging in.");
					((FightInstance) inst.getNextInstance()).addInitialTask((fi, fdata) -> {
						for (PlayerFightData pfdata : fdata) {
							pfdata.setMana(pfdata.getSessionData().getMaxMana());
							pfdata.setStamina(pfdata.getSessionData().getMaxStamina());
						}
					});
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.LEATHER_BOOTS, "Skirt around them",
				"Skip the fight entirely.",
				(s, inst) -> {
					s.broadcast("You sneak around the group without issue.");
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.FLINT, "Steal from them",
				"<yellow>50%</yellow> chance you get an S tier reward, <yellow>50%</yellow chance a normal fight starts.",
				(s, inst) -> {
					s.broadcast("You sneak around the group without issue.");
					return null;
				}));
	}
	
	@Override
	public void initialize(Session s, ChanceInstance inst) {
		inst.setNextInstance(new StandardFightInstance(s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited()));
	}
}
