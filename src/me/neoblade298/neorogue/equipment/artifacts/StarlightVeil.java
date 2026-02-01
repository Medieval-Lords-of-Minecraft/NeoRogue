package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class StarlightVeil extends Artifact {
	private static final String ID = "StarlightVeil";
	public StarlightVeil() {
		super(ID, "Starlight Veil", Rarity.RARE, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addStaminaRegen(1);
		data.addManaRegen(1);
		data.addTrigger(id, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			data.addStaminaRegen(-1);
			data.addManaRegen(-1);
			return TriggerResult.remove();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE_DUST, 
				"Increase mana and stamina regen by <white>1</white> until you receive health damage.");
	}
}
