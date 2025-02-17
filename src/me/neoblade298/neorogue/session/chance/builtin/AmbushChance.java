package me.neoblade298.neorogue.session.chance.builtin;

import java.util.Collection;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.NodeSelectInstance;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.FightScore;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.StandardFightInstance;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.reward.RewardInstance;

public class AmbushChance extends ChanceSet {

	public AmbushChance() {
		super(new AreaType[] { AreaType.LOW_DISTRICT, AreaType.HARVEST_FIELDS }, Material.GRAVEL, "Ambush", "Ambush");
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
					s.broadcast("You sneak around the group without issue.");
					inst.setNextInstance(new NodeSelectInstance(s));
					return null;
				}));
		
		stage.addChoice(new ChanceChoice(Material.FLINT, "Steal from them",
				"<yellow>50%</yellow> chance you get an S tier reward, <yellow>50%</yellow> chance you start a fight dealing 20% reduced damage.",
				(s, inst, unused) -> {
					if (NeoRogue.gen.nextBoolean()) {
						inst.setNextInstance(new RewardInstance(s, StandardFightInstance.generateRewards(s, FightScore.S)));
						s.broadcast("Success! You take your pick of the loot and go on your way.");
					}
					else {
						Collection<PlayerSessionData> party = s.getParty().values();
						PlayerSessionData data = (PlayerSessionData) party.toArray()[NeoRogue.gen.nextInt(party.size())];
						s.broadcast("They spot you as <yellow>" + data.getData().getDisplay() +
								"</yellow> lumbers over and snaps a few twigs. You groan and prepare to fight.");
						((FightInstance) inst.getNextInstance()).addInitialTask((fi, fdata) -> {
							for (PlayerFightData pfdata : fdata) {
								pfdata.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(pfdata, 0, -0.2, BuffStatTracker.ignored("ambushChance")));
							}
						});
					}
					return null;
				}));
	}
	
	@Override
	public void initialize(Session s, ChanceInstance inst) {
		inst.setNextInstance(new StandardFightInstance(s, s.getParty().keySet(), s.getArea().getType(), s.getNodesVisited()));
	}
}
