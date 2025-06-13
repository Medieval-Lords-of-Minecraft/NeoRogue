package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class BagOfPreparation extends Artifact {
	private static final String ID = "bagOfPreparation";
	
	public BagOfPreparation() {
		super(ID, "Bag of Preparation", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		
	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.addMaxStorage(3);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.BUNDLE, "Increases storage size by <white>3</white>.");
	}
}
