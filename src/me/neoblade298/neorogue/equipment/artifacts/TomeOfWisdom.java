package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class TomeOfWisdom extends Artifact {
	private static final String ID = "tomeOfWisdom";

	public TomeOfWisdom() {
		super(ID, "Tome Of Wisdom", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		canStack = true;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {	
		data.addMaxAbilities(1);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ENCHANTED_BOOK, "Increases the number of abilities you can equip by <white>1</white>.");
	}
}
