package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class IronBolt extends Ammunition {
	private static final String ID = "IronBolt";
	private double mult;
	
	public IronBolt(boolean isUpgraded) {
		super(ID, "Iron Bolt", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(isUpgraded ? 40 : 30, 0.1, DamageType.PIERCING));
		mult = isUpgraded ? 1.0 : 0.5;
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		super.initialize(data, bind, es, slot);
		
		// Add trigger to increase negative status applications
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.getStatusClass() != StatusClass.NEGATIVE) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.multiplier(data, mult, BuffStatTracker.of(id + slot, this, "Status stacks increased")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void onStart(ProjectileInstance inst) {
		inst.getVelocity().multiply(0.8);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW,
				"Decreased arrow velocity. Applying negative statuses to enemies hit increases them by " +
				DescUtil.yellow((int) (mult * 100) + "%") + ".");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.GRAY);
		item.setItemMeta(pm);
	}
}
