package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class VenomousShiv extends Equipment {
	private static final String ID = "VenomousShiv";
	private static final int DAMAGE = 25, POISON_DURATION = 10;
	private int poisonPerCast;

	public VenomousShiv(boolean isUpgraded) {
		super(ID, "Venomous Shiv", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(DAMAGE, 1.25, 0.2, DamageType.PIERCING,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		poisonPerCast = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta bankedPoison = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(data.getPlayer(), data, ev.getTarget());
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			bankedPoison.addCount(poisonPerCast);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (bankedPoison.getCount() == 0) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			int poison = bankedPoison.getCount();
			bankedPoison.setCount(0);
			FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, poison, POISON_DURATION * 20, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Ability casts bank " + GlossaryTag.POISON.tag(this, poisonPerCast)
				+ ". Your next basic attack applies all banked Poison " + DescUtil.duration(POISON_DURATION) + ".");
	}
}