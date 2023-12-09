package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SapphireGem extends Artifact {
	private int inc;

	public SapphireGem(boolean isUpgraded) {
		super("sapphireGem", "Sapphire Gem", isUpgraded, Rarity.EPIC, EquipmentClass.CLASSLESS);

		inc = isUpgraded ? 75 : 50;
		item = createItem(Material.LAPIS_LAZULI, "ARTIFACT",
				null, "<gray>Increases max mana by <yellow>"+ inc);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxMana(inc);
	}
}
