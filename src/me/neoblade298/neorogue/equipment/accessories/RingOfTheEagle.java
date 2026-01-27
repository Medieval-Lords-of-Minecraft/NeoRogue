package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class RingOfTheEagle extends Equipment {
	private static final String ID = "RingOfTheEagle";
	private int thres;
	
	public RingOfTheEagle(boolean isUpgraded) {
		super(ID, "Ring of the Eagle", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		thres = isUpgraded ? 10 : 12;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			// Only count projectile damage
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			
			am.addCount(1);
			if (am.getCount() >= thres) {
				am.setCount(0);
				for (EquipmentInstance inst : data.getActiveEquipment().values()) {
					inst.addCooldown(-1);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Passive. Every <white>" + (thres == 10 ? "10th" : "12th") + "</white> time you hit an enemy with a projectile, " +
				"decrease all ability cooldowns by <white>1s</white>.");
	}
}
