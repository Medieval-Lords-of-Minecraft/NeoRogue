package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SapphireCluster extends Artifact {
	private int max, starting;

	public SapphireCluster() {
		super("sapphireCluster", "Sapphire Cluster", Rarity.RARE, EquipmentClass.CLASSLESS);

		max = 25;
		starting = 5;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxMana(max);
		data.addStartingMana(starting);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI, "<gray>Increases max mana by <yellow>" + max + "</yellow> and starting mana by <yellow>" + starting);
	}
}
