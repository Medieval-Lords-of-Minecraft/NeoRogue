package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

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

public class MartialStaff extends Equipment {
	private static final String ID = "MartialStaff";
	private static final int HITS = 5;
	private int concussed;

	public MartialStaff(boolean isUpgraded) {
		super(ID, "Martial Staff", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(30, 1, 0.4, 1, DamageType.BLUNT,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		concussed = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta hits = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			LivingEntity target = ev.getTarget();
			weaponSwingAndDamage(data.getPlayer(), data, target);
			if (hits.addCount(1) < HITS) return TriggerResult.keep();
			hits.setCount(0);
			FightInstance.applyStatus(target, StatusType.CONCUSSED, data, concussed, -1, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Every " + DescUtil.val("5th") + " hit applies "
				+ GlossaryTag.CONCUSSED.tag(this, concussed) + ".");
	}
}