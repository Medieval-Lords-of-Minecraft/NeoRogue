package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EmeraldGem extends Artifact {
	private int max, starting;

	public EmeraldGem(boolean isUpgraded) {
		super("emeraldGem", "Emerald Gem", isUpgraded, Rarity.EPIC, EquipmentClass.CLASSLESS);

		max = isUpgraded ? 75 : 50;
		starting = isUpgraded ? 15 : 10;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxStamina(max);
		data.addStartingStamina(starting);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EMERALD, "<gray>Increases max stamina by <yellow>" + max + "</yellow> and starting stamina by <yellow>" + starting);
	}
}
