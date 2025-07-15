package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class FlintPendant extends Equipment {
	private static final String ID = "flintPendant";
	private int thres, stamina;
	
	public FlintPendant(boolean isUpgraded) {
		super(ID, "Flint Pendant", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY);
		thres = isUpgraded ? 4 : 6;
		stamina = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction action = new StandardPriorityAction(id);

		action.setAction((pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			action.addCount(1);
			if (action.getCount() >= thres) {
				pdata.addStamina(stamina);
				action.addCount(-thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.DEAL_DAMAGE, action);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT, "Every " + DescUtil.yellow(thres) + " times you deal projectile damage, gain " +
				DescUtil.yellow(stamina) + " stamina.");
	}
}
