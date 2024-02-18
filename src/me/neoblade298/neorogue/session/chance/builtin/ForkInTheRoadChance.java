package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.ShrineInstance;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;
import me.neoblade298.neorogue.session.fight.buff.BuffType;

public class ForkInTheRoadChance extends ChanceSet {

	public ForkInTheRoadChance() {
		super(AreaType.LOW_DISTRICT, Material.GRAVEL, "ForkInTheRoad", "Fork in the road");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "There seems to be a new lightly-traveled path that isn't on your map."
				+ " It could lead to a nice shrine or potentially some enemies.");
		
		ChanceStage fight = new ChanceStage(this, "fight", "The path led you straight to enemies. You must fight.");
		fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
		fight.addChoice(new ChanceChoice(Material.RED_WOOL, "Welp"));
		fight.addChoice(new ChanceChoice(Material.GREEN_WOOL, "<green>Welp, but green"));

		ChanceChoice choice = new ChanceChoice(Material.COBBLESTONE, "Let's risk it",
				"<gray><yellow>50% </yellow>chance to arrive at a shrine, <yellow>50% </yellow>chance to encounter a fight and start it with 10% reduced damage debuff.",
				(s, inst, data) -> {
					boolean isShrine = NeoRogue.gen.nextBoolean();
					if (isShrine) {
						s.broadcast("The fork takes you to a nice open space by a river. Nice!");
						inst.setNextInstance(new ShrineInstance(s));
						return null;
					}
					else {
						PlayerSessionData psd = inst.chooseRandomPartyMember();
						s.broadcast("You follow <yellow>" + psd.getData().getDisplay() + "'s</yellow> bold instructions to walk along the path straight into an enemy lair.");
						StandardFightInstance sfi = new StandardFightInstance(s, s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited());
						sfi.addInitialTask((fi, fdata) -> {
							for (PlayerFightData pfdata : fdata) {
								pfdata.addBuff(pfdata.getUniqueId(), true, true, BuffType.GENERAL, -0.2);
							}
						});
						inst.setNextInstance(sfi);
						return fight.getId();
					}
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.STONE_BRICKS, "Let's not",
				"Stay on your path and avoid any risk.",
				(s, inst, data) -> {
					s.broadcast("You were never a fan of Robert Frost's works anyway. You get back to walking.");
					return null;
				});
		stage.addChoice(choice);
	}
}
