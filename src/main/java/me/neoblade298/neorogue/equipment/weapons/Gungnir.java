package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class Gungnir extends Equipment {
	private static final String ID = "Gungnir";
	private static final int HIT_THRESHOLD = 3;
	private int concussed;

	public Gungnir(boolean isUpgraded) {
		super(ID, "Gungnir", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 90 : 70, 0.7, 0.4, DamageType.PIERCING,
						Sound.ENTITY_PLAYER_ATTACK_CRIT));
		concussed = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta hits = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			DamageMeta meta = new DamageMeta(data);
			meta.addDamageSlice(new DamageSlice(data, properties.get(PropertyType.DAMAGE), properties.getType(),
					DamageType.EARTHEN, DamageStatTracker.of(id + slot, this)));
			meta.setKnockback(properties.get(PropertyType.KNOCKBACK)).isBasicAttack(this, true);
			weaponSwing(data.getPlayer(), data);
			FightInstance.dealDamage(meta, ev.getTarget());

			if (hits.addCount(1) < HIT_THRESHOLD) return TriggerResult.keep();
			hits.setCount(0);
			FightInstance.applyStatus(ev.getTarget(), StatusType.CONCUSSED, data, concussed, -1, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIDENT, "Converts its damage from " + GlossaryTag.PIERCING.tag(this) + " to "
				+ GlossaryTag.EARTHEN.tag(this) + " after buffs are applied. Every " + DescUtil.val("3rd")
				+ " hit applies " + GlossaryTag.CONCUSSED.tag(this, concussed) + ".");
	}
}
