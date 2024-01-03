package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SapphireShard extends Artifact {
	private int max, starting;

	public SapphireShard(boolean isUpgraded) {
		super("sapphireShard", "Sapphire Shard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS);

		max = isUpgraded ? 15 : 10;
		starting = isUpgraded ? 3 : 2;
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
