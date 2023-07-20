package me.neoblade298.neorogue.session.instance;

import java.util.HashMap;
import java.util.UUID;

import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.equipment.*;

public class FightData {
	private HashMap<Trigger, Usable> triggers = new HashMap<Trigger, Usable>();
	
	public FightData(PlayerSessionData data) {
		for (Accessory acc : data.getAccessories()) {
			if (acc == null) continue;
			acc.initialize();
		}
		for (Armor armor : data.getArmor()) {
			if (armor == null) continue;
			armor.initialize();
		}
		for (Usable hotbar : data.getHotbar()) {
			if (hotbar == null) continue;
			hotbar.initialize();
		}
		for (Usable other : data.getOtherBinds()) {
			if (other == null) continue;
			other.initialize();
		}
		for (Artifact art : data.getArtifacts()) {
			if (art == null) continue;
			art.initialize();
		}
		data.getOffhand().initialize();
	}
	
	public FightData(UUID uuid) {
		// Used to initialize mobs
	}
}
