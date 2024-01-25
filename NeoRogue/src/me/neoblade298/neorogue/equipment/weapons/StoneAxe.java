package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneAxe extends Equipment {
	
	public StoneAxe(boolean isUpgraded) {
		super("stoneAxe", "Stone Axe", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 115 : 70, 0.5, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		double damage = properties.getDamage();
		double kb = properties.getKnockback();
		DamageType type = properties.getType();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwing(p, pdata);
			DamageMeta dm = new DamageMeta(data, damage, type);
			dm.addDamageSlice(new DamageSlice(data.getUniqueId(), 0, type));
			dm.addDamageSlice(new DamageSlice(data.getUniqueId(), 0, type));
			// All 3 damage slices are affected by physical increase
			
			BasicAttackEvent basicEv = new BasicAttackEvent(ev.getTarget(), dm, kb, this, null);
			data.runActions(data, Trigger.BASIC_ATTACK, basicEv);
			FightInstance.dealDamage(dm, ev.getTarget());
			if (kb != 0) {
				FightInstance.knockback(p, ev.getTarget(), kb);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_AXE, "Non-multiplicative physical damage buffs are <yellow>3x</yellow> as effective with this weapon.");
	}
}
