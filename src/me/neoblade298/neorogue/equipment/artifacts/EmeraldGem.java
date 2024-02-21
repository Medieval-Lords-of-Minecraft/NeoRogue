package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class EmeraldGem extends Artifact {
	private int max;
	private double regen;

	public EmeraldGem() {
		super("emeraldGem", "Emerald Gem", Rarity.EPIC, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 50;
		regen = 0.5;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxStamina(max);
		data.addStaminaRegen(regen);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EMERALD, "Increases max stamina by <white>" + max + "</white> and starting stamina by <white>" + regen + "</white>.");
	}
}
