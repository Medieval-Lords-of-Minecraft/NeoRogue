package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RubyCluster extends Artifact {
	private int max;

	public RubyCluster(boolean isUpgraded) {
		super("rubyCluster", "Ruby Cluster", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);

		max = isUpgraded ? 35 : 25;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxHealth(max);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "<gray>Increases max health by <yellow>" + max);
	}
}
