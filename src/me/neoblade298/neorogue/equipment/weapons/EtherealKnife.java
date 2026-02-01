package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class EtherealKnife extends Equipment {
	private static final String ID = "EtherealKnife";
	private int durability;
	
	public EtherealKnife(boolean isUpgraded) {
		super(ID, "Ethereal Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(100, 0.5, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		durability = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(p, data, ev.getTarget());
			inst.addCount(1);
			if (inst.getCount() >= durability) {
				Sounds.breaks.play(p, p);
				p.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
		
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Has <yellow>" + durability + "</yellow> uses per fight.");
	}
}
