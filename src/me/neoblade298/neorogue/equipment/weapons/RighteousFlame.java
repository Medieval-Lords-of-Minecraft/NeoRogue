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

public class RighteousFlame extends Equipment {
	private static final String ID = "righteousFlame";
	private int sanct;
	
	public RighteousFlame(boolean isUpgraded) {
		super(ID, "Righteous Flame", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 90 : 70, 1, 0.4, DamageType.SLASHING, new SoundContainer(Sound.ENTITY_BLAZE_SHOOT, 0.5F)));
		properties.addUpgrades(PropertyType.DAMAGE);
		sanct = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			DamageMeta dm = new DamageMeta(pdata);
			dm.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE) / 2, properties.getType(), DamageType.LIGHT,
				DamageStatTracker.of(id + slot + 1, this, "Damage Dealt (Light)")));
			dm.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE) / 2, properties.getType(),
					DamageType.FIRE, DamageStatTracker.of(id + slot, this, "Damage Dealt (Fire)")));
			dm.setKnockback(properties.get(PropertyType.KNOCKBACK)).isBasicAttack(this, true);
			weaponSwing(p, data);
			FightInstance.dealDamage(dm, ev.getTarget());
			FightInstance.applyStatus(ev.getTarget(), StatusType.SANCTIFIED, data, sanct, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "Converts its damage into half " + GlossaryTag.LIGHT.tag(this) + " and half " +
		GlossaryTag.FIRE.tag(this) + " damage after buffs are applied and also applies " +
		GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ".");
	}
}
