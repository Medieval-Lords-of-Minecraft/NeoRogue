package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class Pumped extends Artifact {
	private static final String ID = "Pumped";
	private int str = 25;
	
	public Pumped() {
		super(ID, "Pumped", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
		canStack = true;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.applyStatus(StatusType.STRENGTH, data, str, -1);
		data.getSessionData().removeArtifact(this);
	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "Grants " + GlossaryTag.STRENGTH.tag(this, str, false) + " at the start of the fight and removes <white>1</white> of itself.");
	}
}
