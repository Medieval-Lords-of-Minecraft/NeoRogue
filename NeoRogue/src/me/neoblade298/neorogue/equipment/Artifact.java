package me.neoblade298.neorogue.equipment;

import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class Artifact extends Equipment {
	public Artifact(String id, String display, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, display, isUpgraded, rarity, ec, EquipmentType.ARTIFACT, EquipmentProperties.none());
	}

	public abstract void onAcquire(PlayerSessionData data);
}
