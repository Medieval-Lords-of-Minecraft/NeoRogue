package me.neoblade298.neorogue.equipment.accessories;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class EarthenRing extends Equipment {
	private static final String ID = "EarthenRing";
	private static final int ATTACKS_PER_PROC = 5;
	private int conc, damage;
	
	public EarthenRing(boolean isUpgraded) {
		super(ID, "Earthen Ring", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		conc = isUpgraded ? 6 : 4;
		damage = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta attacks = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (attacks.addCount(1) < ATTACKS_PER_PROC) return TriggerResult.keep();
			attacks.setCount(0);
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.EARTHEN,
					DamageStatTracker.of(id + slot, this)));
			FightInstance.applyStatus(ev.getTarget(), StatusType.CONCUSSED, data, conc, -1, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS, "Every " + DescUtil.val(ATTACKS_PER_PROC) + " basic attacks, deal an additional "
				+ GlossaryTag.EARTHEN.tag(this, damage) + " damage and apply " + GlossaryTag.CONCUSSED.tag(this, conc) + ".");
	}
}
