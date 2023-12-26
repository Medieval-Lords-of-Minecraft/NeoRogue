package me.neoblade298.neorogue.session.chance.builtin;

import java.util.Collection;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.RewardInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.FightScore;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;

public class AmbushChance extends ChanceSet {

	public AmbushChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "Ambush", "Ambush");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You catch sight of a group of enemies. Looks like they haven't noticed you yet.");

		stage.addChoice(new ChanceChoice(Material.DIAMOND_SWORD, "Gather yourself",
				"Start the fight, but at max mana and stamina.",
				(s, inst, data) -> {
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
				(s, inst, data) -> {
					inst.setNextInstance(new NodeSelectInstance());
					s.broadcast("You sneak around the group without issue.");
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.FLINT, "Steal from them",
				"<yellow>50%</yellow> chance you get an S tier reward, <yellow>50%</yellow chance a normal fight starts.",
				(s, inst, unused) -> {
					if (NeoRogue.gen.nextBoolean()) {
						inst.setNextInstance(new RewardInstance(StandardFightInstance.generateRewards(s, FightScore.S)));
						s.broadcast("Success! You take your pick of the loot and go on your way.");
					}
					else {
						Collection<PlayerSessionData> party = s.getParty().values();
						PlayerSessionData data = (PlayerSessionData) party.toArray()[NeoRogue.gen.nextInt(party.size())];
						s.broadcast("They spot you as <yellow>" + data.getData().getDisplay() +
								"</yellow> lumbers over and snaps a few twigs. You groan and prepare to fight.");
					}
					return null;
				}));
	}
	
	@Override
	public void initialize(Session s, ChanceInstance inst) {
		inst.setNextInstance(new StandardFightInstance(s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited()));
	}
}
