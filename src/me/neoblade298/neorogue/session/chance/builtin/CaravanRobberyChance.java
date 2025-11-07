package me.neoblade298.neorogue.session.chance.builtin;

import org.bukkit.Material;

import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.artifacts.Exhaustion;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class CaravanRobberyChance extends ChanceSet {

	public CaravanRobberyChance() {
		super(AreaType.HARVEST_FIELDS, Material.SADDLE, "CaravanRobbery", "Caravan Robbery");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "While walking through the forest, you hear a commotion and decide to investigate. " +
			"On a small dirt road, you see a small caravan being robbed by a group of bandits. You could probably save them for a monetary reward, though having to " +
			"protect the merchants would undoubtedly be dangerous.");

		stage.addChoice(new ChanceChoice(Material.DIAMOND_SWORD, "Save the Merchants",
				"Gain <yellow>300 coins</yellow> but reduce your max abilities by <white>1</white> for 3 fights.",
				(s, inst, data) -> {
					for (PlayerSessionData pd: s.getParty().values()) {
						pd.giveEquipment(Exhaustion.get());
						pd.addCoins(300);
					}
					s.broadcast("You save the merchants and are rewarded <yellow>300 coins</yellow>, but you feel exhausted.");
					return null;
				}));

		stage.addChoice(new ChanceChoice(Material.BARRIER, "Leave before they see you",
				"I have enough on my hands as it is.",
				(s, inst, data) -> {
					s.broadcast("You mind your own business and continue on your way.");
					return null;
				}));
	}
}
