package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SapphireGem extends Artifact {
	private int max, starting;

	public SapphireGem() {
		super("sapphireGem", "Sapphire Gem", Rarity.EPIC, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 50;
		starting = 10;
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
