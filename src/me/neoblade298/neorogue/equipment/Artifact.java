package me.neoblade298.neorogue.equipment;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public abstract class Artifact extends Equipment {
	protected boolean canStack; // If an artifact can be obtained from a player's droptable more than once
	public Artifact(String id, String display, Rarity rarity, EquipmentClass ec) {
		super(id, display, false, rarity, ec, EquipmentType.ARTIFACT, EquipmentProperties.none());
	}

	public boolean canStack() {
		return canStack;
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// This is not used, ArtifactInstance instead calls the below abstract initialize
	}
	public abstract void initialize(Player p, PlayerFightData data, ArtifactInstance ai);
	public abstract void onAcquire(PlayerSessionData data);
	public abstract void onInitializeSession(PlayerSessionData data);
}
