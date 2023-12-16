package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RubyShard extends Artifact {
	private int inc;

	public RubyShard(boolean isUpgraded) {
		super("rubyShard", "Ruby Shard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);

		inc = isUpgraded ? 15 : 10;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxHealth(inc);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Artifact",
				null, "<gray>Increases max health by <yellow>" + inc);
	}
}
