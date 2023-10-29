package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class RubyCluster extends Artifact {
	private int inc;

	public RubyCluster(boolean isUpgraded) {
		super("rubyCluster", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);

		display = "Ruby Cluster";
		inc = isUpgraded ? 35 : 25;
		item = createItem(Material.REDSTONE, "ARTIFACT",
				null, "<gray>Increases max health by <yellow>" + inc);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxHealth(inc);
	}
}
