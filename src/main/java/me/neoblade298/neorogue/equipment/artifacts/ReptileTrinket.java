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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ReptileTrinket extends Artifact {
	private static final String ID = "ReptileTrinket";
	private final int strength, intellect;

	public ReptileTrinket() {
		super(ID, "Reptile Trinket", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		strength = 5;
		intellect = 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.USE_CONSUMABLE, (pdata, in) -> {
			data.applyStatus(StatusType.STRENGTH, data, strength, -1, this);
			data.applyStatus(StatusType.INTELLECT, data, intellect, -1, this);
			return TriggerResult.keep();
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
		item = createItem(Material.TURTLE_SCUTE, "Using a consumable grants "
				+ GlossaryTag.STRENGTH.tag(this, strength) + " and "
				+ GlossaryTag.INTELLECT.tag(this, intellect) + ".");
	}
}