package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Charge extends Equipment {
	private static final String ID = "Charge";
	private int shields;
	
	public Charge(boolean isUpgraded) {
		super(ID, "Charge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, isUpgraded ? 15 : 30, 0, 0));
		shields = isUpgraded ? 6 : 4;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BOOTS,
				"On cast, activate this for the rest of the fight. Sprinting costs <white>1</white> additional stamina per second, and every <white>3s</white> of sprinting grants you "
				+ GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>3s</white>].");
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ChargeInstance inst = new ChargeInstance(id);
		data.addTrigger(id, Trigger.PLAYER_TICK, inst);
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			if (inst.isActive()) return TriggerResult.remove();
			inst.activate();
			pdata.addSprintCost(1);
			return TriggerResult.remove();
		}));
	}
	
	private class ChargeInstance extends PriorityAction {
		private long lastUsed = 0L;
		private boolean active;
		public ChargeInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				if (!active) return TriggerResult.keep();
				Player p = pdata.getPlayer();
				if (!p.isSprinting()) return TriggerResult.keep();
				addShield(pdata);
				return TriggerResult.keep();
			};
		}

		private void activate() {
			active = true;
		}

		private boolean isActive() {
			return active;
		}
		
		private void addShield(PlayerFightData pdata) {
			if (System.currentTimeMillis() - lastUsed < 3000) return;
			Player p = pdata.getPlayer();
			pdata.addSimpleShield(p.getUniqueId(), shields, 60);
			lastUsed = System.currentTimeMillis();
		}
	}
}
