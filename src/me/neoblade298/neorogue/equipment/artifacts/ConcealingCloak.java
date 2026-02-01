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

public class ConcealingCloak extends Artifact {
	private static final String ID = "ConcealingCloak";

	public ConcealingCloak() {
		super(ID, "Concealing Cloak", Rarity.COMMON, EquipmentClass.THIEF);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ConcealingCloakInstance inst = new ConcealingCloakInstance(ID, data);
		data.addTrigger(ID, Trigger.PLAYER_TICK, inst);
		
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			inst.reset();
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			inst.reset();
			return TriggerResult.keep();
		});
	}
	
	private class ConcealingCloakInstance extends PriorityAction {
		private int timer = 2;
		private boolean active = false;
		private double staminaRegen, manaRegen;
		private PlayerFightData data;
		public ConcealingCloakInstance(String id, PlayerFightData data) {
			super(id);
			this.data = data;
			staminaRegen = data.getSessionData().getStaminaRegen() * 0.2;
			manaRegen = data.getSessionData().getManaRegen() * 0.2;
			action = (pdata, in) -> {
				if (--timer >= 0 || active) return TriggerResult.keep();
				active = true;
				data.addStaminaRegen(staminaRegen);
				data.addManaRegen(manaRegen);
				return TriggerResult.keep();
			};
		}
		
		public void reset() {
			timer = 2;
			if (active) {
				data.addStaminaRegen(-staminaRegen);
				data.addManaRegen(-manaRegen);
				active = false;
			}
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, 
				"Not taking or dealing damage for <white>2</white> seconds increases your "
				+ "mana and stamina regen by <white>20%</white> of their base.");
	}
}
