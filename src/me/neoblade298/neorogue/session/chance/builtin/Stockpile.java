package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.reward.EquipmentChoiceReward;
import me.neoblade298.neorogue.session.reward.Reward;
import me.neoblade298.neorogue.session.reward.RewardInstance;

public class Stockpile extends ChanceSet {

	public Stockpile() {
		super(AreaType.LOW_DISTRICT, Material.GOLD_INGOT, "Stockpile");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You stumble across a thief's stockpile of equipment. The thief confronts you and looks skilled, but"
				+ " makes it clear they're here to defend their stockpile and nothing else.");

		ChanceChoice choice = new ChanceChoice(Material.GOLD_NUGGET, "Bargain",
				"Gain one random piece of equipment", (s, inst, unused) -> {
					s.broadcast("You tell the thief that your party will leave without telling a soul if they give you something for your"
							+ " troubles. The thief thinks briefly before agreeing, and you leave.");
					for (PlayerSessionData data : s.getParty().values()) {
						Equipment eq = Equipment.getDrop(s.getAreasCompleted() + 1, 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0);
						data.giveEquipment(s.rollUpgrade(eq));
					}
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.IRON_SWORD, "Fight",
				"Everyone takes <red>25%</red> of their max health as damage, and everyone gets to pick from 3 random rare equipment.",
				"Somebody lacks the health to do this!", (s, inst, unused) -> {
					for (PlayerSessionData pd : s.getParty().values()) {
						if ((pd.getHealth() / pd.getMaxHealth()) <= 0.25) return false;
					}

					return true;
				}, (s, inst, data) -> {
					HashMap<UUID, ArrayList<Reward>> generated = new HashMap<UUID, ArrayList<Reward>>();
					for (PlayerSessionData pd : s.getParty().values()) {
						pd.damagePercent(0.25);
						ArrayList<Reward> rewards = new ArrayList<Reward>();
						ArrayList<Equipment> equips = Equipment.getDrop(s.getAreasCompleted() + 2, 3, pd.getPlayerClass(), EquipmentClass.CLASSLESS);
						s.rollUpgrades(equips);
						rewards.add(new EquipmentChoiceReward(equips));
						generated.put(pd.getUniqueId(), rewards);
					}
					inst.setNextInstance(new RewardInstance(s, generated));
					s.broadcast("The thief was formidable, but that gear is all yours now!");
					return null;
				});
		stage.addChoice(choice);
	}
}
