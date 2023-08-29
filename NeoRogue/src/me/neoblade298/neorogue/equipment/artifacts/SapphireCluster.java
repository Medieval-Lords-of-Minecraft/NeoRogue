package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class SapphireCluster extends Artifact {
	private int inc;

	public SapphireCluster(boolean isUpgraded) {
		super("sapphireCluster", isUpgraded, Rarity.RARE, EquipmentClass.CLASSLESS);

		display = "Sapphire Cluster";
		inc = isUpgraded ? 35 : 25;
		item = createItem(Material.LAPIS_LAZULI, "ARTIFACT",
				null, "&7Increases max mana by &e" + inc, display);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxMana(inc);
	}
}
