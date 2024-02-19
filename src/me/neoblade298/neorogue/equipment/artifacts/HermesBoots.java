package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HermesBoots extends Artifact {
	private static int num = 5;

	public HermesBoots() {
		super("hermesBoots", "Hermes Boots", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSprintCost(-4);
		data.addTrigger(id, Trigger.CAST_USABLE, new HermesBootsInstance(p, this, slot, es));
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TARGET, 
				"Sprinting has no stamina cost until you cast <white>" + num + "</white> abilities.");
	}
	
	private class HermesBootsInstance extends EquipmentInstance {
		private int count = 0;

		public HermesBootsInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			
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
