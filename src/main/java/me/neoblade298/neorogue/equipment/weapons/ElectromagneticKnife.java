package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class ElectromagneticKnife extends Equipment {
	private static final String ID = "ElectromagneticKnife";
	private static final int ELECTRIFIED = 2, ELECTRIFIED_THRESHOLD = 5;
	private final int bonusDamage;
	
	public ElectromagneticKnife(boolean isUpgraded) {
		super(ID, "Electromagnetic Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(40, 1, 0.2, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		bonusDamage = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(pdata.getPlayer(), data, ev.getTarget());
			FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, ELECTRIFIED, -1, this);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, inputs) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) inputs;
			if (!ev.getWeapon().equals(this)) return TriggerResult.keep();
			FightData target = FightInstance.getFightData(ev.getTarget());
			if (target != null && target.hasStatus(StatusType.ELECTRIFIED)
					&& target.getStatus(StatusType.ELECTRIFIED).getStacks() >= ELECTRIFIED_THRESHOLD) {
				ev.getMeta().addDamageSlice(new DamageSlice(pdata, bonusDamage, DamageType.SLASHING,
						DamageStatTracker.of(id + slot, this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD,
				"Every basic attack applies " + GlossaryTag.ELECTRIFIED.tag(this, ELECTRIFIED)
						+ ". Deal " + DescUtil.yellow(bonusDamage) + " additional damage to enemies with at least "
						+ GlossaryTag.ELECTRIFIED.tag(this, ELECTRIFIED_THRESHOLD) + ".");
	}
}
