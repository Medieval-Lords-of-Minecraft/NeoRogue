package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HermesBoots extends Artifact {
	private static int num = 5;

	public HermesBoots() {
		super("hermesBoots", "Hermes Boots", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addSprintCost(-4);
		data.addTrigger(id, Trigger.CAST_USABLE, new HermesBootsInstance(id));
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_BOOTS, 
				"Sprinting's cost is decreased by <white>4</white> until you cast <white>" + num + "</white> abilities.");
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
