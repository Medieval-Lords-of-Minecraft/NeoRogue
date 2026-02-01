package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HermesBoots extends Artifact {
	private static final String ID = "HermesBoots";
	private static int num = 5;

	public HermesBoots() {
		super(ID, "Hermes Boots", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addSprintCost(-4);
		data.addTrigger(id, Trigger.CAST_USABLE, new HermesBootsInstance(id));
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_BOOTS,
				"Sprinting's cost is decreased by <white>4</white> until you cast <white>" + num
						+ "</white> abilities.");
	}

	private class HermesBootsInstance extends PriorityAction {
		private int count = 0;

		public HermesBootsInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				if (++count >= num) {
					pdata.addSprintCost(4);
					return TriggerResult.remove();
				}
				return TriggerResult.keep();
			};
		}

	}
}
