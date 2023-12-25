package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SapphireCluster extends Artifact {
	private int inc;

	public SapphireCluster(boolean isUpgraded) {
		super("sapphireCluster", "Sapphire Cluster", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);

		inc = isUpgraded ? 35 : 25;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxMana(inc);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI, "<gray>Increases max mana by <yellow>" + inc);
	}
}
