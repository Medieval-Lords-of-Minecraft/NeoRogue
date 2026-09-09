package me.neoblade298.neorogue.session.event;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Consumable;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.DropTableSet;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.region.NodeType;

public class RewardBuildEvent extends RewardFightEvent {
	private final DropTableSet<Equipment> equipmentDroptable;
	private final DropTableSet<Artifact> artifactDroptable;
	private final DropTableSet<Consumable> consumableDroptable;

	public RewardBuildEvent(PlayerSessionData data, NodeType type) {
		super(type);
		equipmentDroptable = data.getData().getEquipmentDroptable().clone();
		artifactDroptable = data.getArtifactDroptable().clone();
		consumableDroptable = data.getData().getConsumableDroptable().clone();
	}

	public DropTableSet<Equipment> getEquipmentDroptable() {
		return equipmentDroptable;
	}

	public DropTableSet<Artifact> getArtifactDroptable() {
		return artifactDroptable;
	}

	public DropTableSet<Consumable> getConsumableDroptable() {
		return consumableDroptable;
	}

	public void multiplyEquipmentTagWeight(GlossaryTag tag, double multiplier) {
		multiplyEquipmentTagWeight(multiplier, tag);
	}

	public void multiplyEquipmentTagWeight(double multiplier, GlossaryTag... tags) {
		equipmentDroptable.multiplyWeight(equipment -> {
			for (GlossaryTag tag : tags) {
				if (equipment.getTags().contains(tag)) return true;
			}
			return false;
		}, multiplier);
	}
}
