package me.neoblade298.neorogue.session.chance.builtin;

import java.util.Collection;

import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class LabChance extends ChanceSet {
	private static final int HEALTH_LOSS = 20;

	public LabChance() {
		super(AreaType.LOW_DISTRICT, Material.GOLD_INGOT, "Lab");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You stumble upon a makeshift lab that has an array of potions brewing.");

		ChanceChoice choice = new ChanceChoice(Material.GOLD_BLOCK, "Grab what you can",
				"Everyone receives <white>3</white> consumables, but with a <white>25%</white> chance to acquire a <red>curse</red> that reduces your armor slots by <white>1</white>.",
				"At least one player doesn't have an armor slot available!",
				(s, inst, unused) -> {
					for (PlayerSessionData data : s.getParty().values()) {
						int numCurses = data.aggregateEquipment((eq) -> { return eq.getType() == EquipmentType.ARMOR && eq.isCursed(); }).size();
						if (numCurses >= PlayerSessionData.ARMOR_SIZE) return false;
					}
					return true;
				},
				(s, inst, unused) -> {
					for (PlayerSessionData data : s.getParty().values()) {
						data.giveEquipment(Equipment.getConsumable(s.getAreasCompleted(), 3, data.getPlayerClass(), EquipmentClass.CLASSLESS));
					}
					if (NeoRogue.gen.nextDouble() < 0.25) {
						for (PlayerSessionData data : s.getParty().values()) {
							data.giveEquipment(Equipment.get("curseOfBurden", false));
						}
						s.broadcast("<red>In your haste to leave, you acquire a curse.");
					}
					else {
						s.broadcast("Everyone takes what they can get and leaves with <white>3</white> powerful new consumables.");
					}
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.GOLD_BLOCK, "Carefully look through the selection",
				"Everyone receives <white>1</white> consumable.",
				(s, inst, unused) -> {
					for (PlayerSessionData data : s.getParty().values()) {
						data.giveEquipment(Equipment.getConsumable(s.getAreasCompleted() + 1, data.getPlayerClass(), EquipmentClass.CLASSLESS));
					}
					s.broadcast("Everyone carefully peruses before choosing the coolest-looking potion and booking it.");
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
							pd.giveEquipment(Equipment.getArtifact(pd.getArtifactDroptable(), s.getAreasCompleted(), 1, EquipmentClass.CLASSLESS, pd.getPlayerClass()));
						}
					}
					return null;
				});
		stage.addChoice(choice);
		
	}
}
