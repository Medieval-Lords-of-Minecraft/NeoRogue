package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EmeraldShard extends Artifact {
	private int inc;

	public EmeraldShard(boolean isUpgraded) {
		super("emeraldShard", "Emerald Shard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);

		inc = isUpgraded ? 15 : 10;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxStamina(inc);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EMERALD, "Artifact",
				null, "<gray>Increases max stamina by <yellow>" + inc);
	}
}
