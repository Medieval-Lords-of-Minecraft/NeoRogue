package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class GripGloves extends Equipment {
	private static final String ID = "gripGloves";
	private int cutoff;
	private double cutoffPct;
	
	public GripGloves(boolean isUpgraded) {
		super(ID, "Grip Gloves", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		cutoffPct = isUpgraded ? 0.4 : 0.6;
		cutoff = (int) (cutoffPct * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			if (data.getStamina() / data.getMaxStamina() <= cutoffPct) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(p.getUniqueId(), 3, 0), BuffOrigin.NORMAL, false);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT, "While above <yellow>" + cutoff + "%</yellow> stamina, decrease all damage taken by "
				+ "<white>3</white>.");
	}
}
