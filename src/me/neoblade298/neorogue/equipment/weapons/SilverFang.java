package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SilverFang extends Equipment {
	private static final String ID = "silverFang";
	private int sanct;
	
	public SilverFang(boolean isUpgraded) {
		super(ID, "Silver Fang", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 55 : 45, 1, 0.3, DamageType.SLASHING, new SoundContainer(Sound.ENTITY_ALLAY_HURT, 0.8F)));
		properties.addUpgrades(PropertyType.DAMAGE);
		canDrop = false;
		
		sanct = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			DamageMeta dm = new DamageMeta(pdata);
			dm.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE), properties.getType(), DamageType.LIGHT, DamageStatTracker.of(id + slot, this)));
			dm.setKnockback(properties.get(PropertyType.KNOCKBACK)).isBasicAttack(this, true);
			weaponSwing(p, data);
			FightInstance.dealDamage(dm, ev.getTarget());
			FightInstance.getFightData(ev.getTarget()).applyStatus(StatusType.SANCTIFIED, data, sanct, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "This weapon converts its damage into " + GlossaryTag.LIGHT.tag(this) + " damage after buffs are applied and "
				+ "applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ".");
	}
}
