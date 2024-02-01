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

	public RubyCluster() {
		super("rubyCluster", "Ruby Cluster", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 25;
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
		item = createItem(Material.REDSTONE, "Increases max health by <white>" + max + "</white>.");
	}
}
