package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class HuntingSpyglass extends Equipment {
	private static final String ID = "HuntingSpyglass";
	private static final int MAX_FOCUS = 5, COOLDOWN_FOCUS_THRESHOLD = 2, COOLDOWN_REDUCTION = 1;
	private double damagePerFocus;
	private int damagePercent;

	public HuntingSpyglass(boolean isUpgraded) {
		super(ID, "Hunting Spyglass", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.none());
		damagePercent = isUpgraded ? 30 : 20;
		damagePerFocus = damagePercent * 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			if (event.getMeta().isBasicAttack()) return TriggerResult.keep();
			int focus = Math.min(MAX_FOCUS, data.getStatus(StatusType.FOCUS).getStacks());
			if (focus > 0) {
				event.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.multiplier(data,
						damagePerFocus * focus, BuffStatTracker.damageBuffAlly(id + slot, this)));
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			if (data.getStatus(StatusType.FOCUS).getStacks() <= COOLDOWN_FOCUS_THRESHOLD) return TriggerResult.keep();
			PreCastUsableEvent event = (PreCastUsableEvent) in;
			event.addBuff(PropertyType.COOLDOWN, id + slot,
					new Buff(data, COOLDOWN_REDUCTION, 0, BuffStatTracker.of(id + slot, this, "Cooldown reduced")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS, "Non-basic-attack damage is increased by "
				+ DescUtil.yellow(damagePercent + "%") + " per " + GlossaryTag.FOCUS.tag(this) + ", capped at "
				+ DescUtil.white(MAX_FOCUS) + " Focus. While above " + DescUtil.white(COOLDOWN_FOCUS_THRESHOLD)
				+ " Focus, reduce all ability cooldowns by " + DescUtil.white(COOLDOWN_REDUCTION + "s") + ".");
	}
}