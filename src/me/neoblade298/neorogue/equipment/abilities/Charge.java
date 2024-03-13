package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Charge extends Equipment {
	private static final String ID = "charge";
	private int shields;
	
	public Charge(boolean isUpgraded) {
		super(ID, "Charge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 10 : 7;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BOOTS,
				"Passive. Sprinting costs <white>1</white> additional stamina per second, but grants you " + GlossaryTag.SHIELDS.tag(this, shields, true) +
				" that lasts 3 seconds for every second you sprint.");
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ChargeInstance inst = new ChargeInstance(id);
		data.addTrigger(id, Trigger.PLAYER_TICK, inst);
		data.addSprintCost(1);
		
		data.addTrigger(id, Trigger.TOGGLE_SPRINT, (pdata, in) -> {
			PlayerToggleSprintEvent ev = (PlayerToggleSprintEvent) in;
			if (!ev.isSprinting()) return TriggerResult.keep();
			inst.addShield(pdata);
			return TriggerResult.keep();
		});
	}
	
	private class ChargeInstance extends PriorityAction {
		private long lastUsed = 0L;
		public ChargeInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				Player p = pdata.getPlayer();
				if (!p.isSprinting()) return TriggerResult.keep();
				addShield(pdata);
				return TriggerResult.keep();
			};
		}
		
		private void addShield(PlayerFightData pdata) {
			// 1 second cooldown to adding shield so player can't spam toggle sprint
			if (System.currentTimeMillis() - lastUsed < 1000) return;
			Player p = pdata.getPlayer();
			pdata.addSimpleShield(p.getUniqueId(), shields, 60L);
			lastUsed = System.currentTimeMillis();
		}
	}
}
