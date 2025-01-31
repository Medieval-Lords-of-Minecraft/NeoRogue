package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class FaeriePendant extends Artifact {
	private static final String ID = "faeriePendant";
	public FaeriePendant() {
		super(ID, "Faerie Pendant", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.applyStatus(StatusType.PROTECT, data, 5, -1);
		data.applyStatus(StatusType.SHELL, data, 5, -1);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, 
				"Gain " + GlossaryTag.PROTECT.tag(this, 5, false) + " and " + GlossaryTag.SHELL.tag(this, 5, false) + " for the first <white>20s</white> of a fight.");
	}
}
