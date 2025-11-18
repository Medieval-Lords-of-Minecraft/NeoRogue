package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaflowBand extends Artifact {
	private static final String ID = "ManaflowBand";

	public ManaflowBand() {
		super(ID, "Manaflow Band", Rarity.COMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.PLAYER_TICK, new ManaflowBandInstance(id));
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}
	
	private class ManaflowBandInstance extends PriorityAction {
		private int timer = 20;
		public ManaflowBandInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				if (--timer <= 0) {
					pdata.addManaRegen(pdata.getSessionData().getManaRegen() * 0.2);
					return TriggerResult.remove();
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BREWER_POTTERY_SHERD, 
				"After <white>20</white> seconds in a fight, increase your mana regen by <white>20%</white> of your base regen.");
	}
}
