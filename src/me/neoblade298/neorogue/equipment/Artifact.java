package me.neoblade298.neorogue.equipment;

import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class Artifact extends Equipment {
	protected boolean canStack; // If an artifact can be obtained from a player's droptable more than once
	public Artifact(String id, String display, Rarity rarity, EquipmentClass ec) {
		super(id, display, false, rarity, ec, EquipmentType.ARTIFACT, EquipmentProperties.none());
	}

	public boolean canStack() {
		return canStack;
	}
	public abstract void onAcquire(PlayerSessionData data);
	public abstract void onInitializeSession(PlayerSessionData data);
}
