package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class RubyShard extends Artifact {
	private int inc;

	public RubyShard(boolean isUpgraded) {
		super("rubyShard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);

		display = "Ruby Shard";
		inc = isUpgraded ? 15 : 10;
		item = createItem(Material.REDSTONE, "ARTIFACT",
				null, "&7Increases max health by &e" + inc, display);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxHealth(inc);
	}
}
