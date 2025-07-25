package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveHealthDamageEvent;

public class Gauze extends Equipment {
	private static final String ID = "gauze";
	private int pct, max;
	
	public Gauze(boolean isUpgraded) {
		super(ID, "Gauze", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		pct = isUpgraded ? 60 : 30;
		max = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		GauzeInstance inst = new GauzeInstance(ID);
		data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, inst);
		data.addTrigger(ID, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			inst.use(data);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_CARPET, "Gaining " + GlossaryTag.STEALTH.tag(this) + " within <white>2</white> seconds of "
				+ "taking health damage heals back <yellow>" + pct + "%</yellow> of the last damage taken, with a maximum heal of "
						+ "<yellow>" + max + "</yellow>.");
	}
	
	private class GauzeInstance extends PriorityAction {
		public GauzeInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				ReceiveHealthDamageEvent ev = (ReceiveHealthDamageEvent) in;
				damage = ev.getTotalDamage();
				timestamp = System.currentTimeMillis();
				return TriggerResult.keep();
			};
		}

		private double damage;
		private long timestamp;
		
		public void use(PlayerFightData data) {
			if (timestamp + 2000 >= System.currentTimeMillis()) {
				data.addHealth(Math.min(max, damage * pct * 0.01));
				damage = 0;
				timestamp = 0;
			}
		}
	}
}
