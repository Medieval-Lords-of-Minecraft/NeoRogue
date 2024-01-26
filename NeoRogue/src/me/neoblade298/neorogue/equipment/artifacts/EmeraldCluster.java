package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EmeraldCluster extends Artifact {
	private int max, starting;

	public EmeraldCluster() {
		super("emeraldCluster", "Emerald Cluster", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 25;
		starting = 5;
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
