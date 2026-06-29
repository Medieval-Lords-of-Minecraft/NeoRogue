package me.neoblade298.neorogue.session.chance.builtin;

import java.util.Collection;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class LabChance extends ChanceSet {
	private static final int HEALTH_LOSS = 30;

	public LabChance() {
		super(RegionType.LOW_DISTRICT, Material.GOLD_INGOT, "Lab");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You stumble upon a makeshift lab that has an array of potions brewing.");

		ChanceChoice choice = new ChanceChoice(Material.GOLD_BLOCK, "Drink the health potion",
				"Everyone heals for <white>25</white>.",
				(s, inst, unused) -> {
					for (PlayerSessionData data : s.getParty().values()) {
						data.setHealth(data.getHealth() + 25);
					}
					s.broadcast("Everyone drinks a health potion and feels reinvigorated.");
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.GOLD_BLOCK, "Loot the place",
				"Everyone receives a consumable and <white>50</white> coins.",
				(s, inst, unused) -> {
					for (PlayerSessionData data : s.getParty().values()) {
						data.giveEquipment(Equipment.getConsumable(s.getBaseDropValue(), 1, data.getPlayerClass(), EquipmentClass.CLASSLESS));
						data.addCoins(50);
					}
					s.broadcast("You all get to work scouring the place, finding the coolest-looking potion, and picking up spare coins.");
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.GOLD_BLOCK, "I'm something of a chemist myself",
				"Mix a bunch of potions together. <white>50%</white> chance to receive a random artifact, <white>50%</white> chance everyone takes <white>20</white> damage.",
				"Somebody lacks the health to do this!", (s, inst, unused) -> {
					for (PlayerSessionData pd : s.getParty().values()) {
						if (pd.getHealth() <= HEALTH_LOSS) return false;
					}
					return true;
				}, 
				(s, inst, unused) -> {
					Collection<PlayerSessionData> party = s.getParty().values();
					PlayerSessionData data = (PlayerSessionData) party.toArray()[NeoRogue.gen.nextInt(party.size())];
					if (NeoRogue.gen.nextBoolean()) {
						s.broadcast("<red>You all stare dubiously as <yellow>" + data.getData().getDisplay() + "</yellow> touts the 2-day seminar on potions they took 5 years ago." +
								" As soon as they combine the first two potions together, the room explodes.");
						for (PlayerSessionData pd : s.getParty().values()) {
							pd.setHealth(pd.getHealth() - HEALTH_LOSS);
						}
					}
					else {
						s.broadcast("Somehow you all make it out alive as <yellow>" + data.getData().getDisplay() + "</yellow> wildly and maniacally mixes potions. You left the disgusting"
								+ " mix at the lab, but in the progress found an artifact on the table behind one of the potions they took.");
						for (PlayerSessionData pd : s.getParty().values()) {
							pd.giveEquipment(Equipment.getArtifact(pd.getArtifactDroptable(), s.getBaseDropValue(), 1, EquipmentClass.CLASSLESS, pd.getPlayerClass()));
						}
					}
					return null;
				});
		stage.addChoice(choice);
		
	}
}
