package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneShiv extends Equipment {
	private static final String ID = "StoneShiv";
	private int stamina;
	
	public StoneShiv(boolean isUpgraded) {
		super(ID, "Stone Shiv", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(35, 1.5, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		stamina = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in)-> {
			inst.addCount(1);
			if (inst.getCount() >= 3) {
				inst.setCount(0);
				data.addStamina(stamina);
			}
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			Player p = data.getPlayer();
			weaponSwingAndDamage(p, data, ev.getTarget());
			
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst );
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Gain " + DescUtil.val(stamina) + " stamina on every 3rd hit.");
	}
}
