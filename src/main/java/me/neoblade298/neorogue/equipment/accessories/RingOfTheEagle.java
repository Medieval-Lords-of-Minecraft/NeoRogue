package me.neoblade298.neorogue.equipment.accessories;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
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
		thres = isUpgraded ? 12 : 14;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
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
				GlossaryTag.PASSIVE.tag(this) + ". Every " + DescUtil.yellow(thres + "th") + " time you hit an enemy with a projectile, " +
				"decrease all ability cooldowns by " + DescUtil.white("1s") + ".");
	}
}
