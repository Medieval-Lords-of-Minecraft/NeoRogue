package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class LightningCutter extends Equipment {
	private static final String ID = "lightningCutter";
	
	public LightningCutter(boolean isUpgraded) {
		super(ID, "Lightning Cutter", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 60 : 45, 1, 0.2, DamageType.PIERCING, Sounds.firework));
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			DamageMeta dm = new DamageMeta(pdata);
			dm.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE), properties.getType(), DamageType.LIGHTNING));
			weaponSwing(p, data);
			weaponDamage(p, data, ev.getTarget(), dm);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "This weapon converts its damage into " + GlossaryTag.LIGHTNING.tag(this) + " damage after buffs are applied.");
	}
}
