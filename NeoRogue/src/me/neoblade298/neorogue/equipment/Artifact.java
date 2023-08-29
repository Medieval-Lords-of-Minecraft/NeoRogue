package me.neoblade298.neorogue.equipment;

import me.neoblade298.neorogue.player.PlayerSessionData;

public abstract class Artifact extends Equipment {
	public Artifact(String id, boolean isUpgraded, Rarity rarity, EquipmentClass ec) {
		super(id, isUpgraded, rarity, ec);
	}

	public abstract void onAcquire(PlayerSessionData data);
}
