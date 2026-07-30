package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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

public class Flametongue extends Equipment {
	private static final String ID = "Flametongue";
	private int bonus;
	
	public Flametongue(boolean isUpgraded) {
		super(ID, "Flametongue", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(45, 1, 0.4, DamageType.SLASHING, new SoundContainer(Sound.ENTITY_BLAZE_SHOOT, 0.5F)));
		bonus = isUpgraded ? 20 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			boolean hasSanctified = FightInstance.getFightData(ev.getTarget()).hasStatus(StatusType.SANCTIFIED);
			DamageMeta dm = new DamageMeta(pdata);
			dm.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE) + (hasSanctified ? bonus : 0), properties.getType(), DamageType.FIRE, DamageStatTracker.of(id + slot, this)));
			dm.setKnockback(properties.get(PropertyType.KNOCKBACK)).isBasicAttack(this, true);
			weaponSwing(data.getPlayer(), data);
			FightInstance.dealDamage(dm, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Converts its damage into " + GlossaryTag.FIRE.tag(this) + " damage after buffs are applied. Deals "
				+ DescUtil.val(bonus)+ " additional damage to enemies affected by " + GlossaryTag.SANCTIFIED.tag(this) + ".");
	}
}
