package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class TomeOfWisdom extends Artifact {

	public TomeOfWisdom() {
		super("tomeOfWisdom", "Tome Of Wisdom", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		canStack = true;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {	
		data.increaseAbilityLimit(1);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Increases the number of abilities you can equip by <white>1</white>.");
	}
}
