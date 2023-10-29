package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class EmeraldGem extends Artifact {
	private int inc;

	public EmeraldGem(boolean isUpgraded) {
		super("emeraldGem", isUpgraded, Rarity.EPIC, EquipmentClass.CLASSLESS);

		display = "Emerald Cluster";
		inc = isUpgraded ? 75 : 50;
		item = createItem(Material.EMERALD, "ARTIFACT",
				null, "<gray>Increases max stamina by <yellow>" + inc);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxStamina(inc);
	}
}
