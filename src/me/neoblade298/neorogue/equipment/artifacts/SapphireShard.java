package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class SapphireShard extends Artifact {
	private int max;
	private double regen;

	public SapphireShard() {
		super("sapphireShard", "Sapphire Shard", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 10;
		regen = 0.1;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxMana(max);
		data.addManaRegen(regen);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI, "Increases max mana by <white>" + max + "</white> and starting mana by <white>" + regen + "</white>.");
	}
}
