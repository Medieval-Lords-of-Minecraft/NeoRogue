package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Emberhail extends Equipment {
	private static final String ID = "Emberhail";
	private int damage;
	private double statusBuff;
	
	public Emberhail(boolean isUpgraded) {
		super(ID, "Emberhail", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY);
		damage = isUpgraded ? 30 : 20;
		statusBuff = isUpgraded ? 0.30 : 0.20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		String statusBuffId = UUID.randomUUID().toString();
		
		// Buff non-basic attack projectile damage
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			if (ev.getMeta().isBasicAttack()) return TriggerResult.keep();
            if (!ev.getMeta().hasTag(PlayerFightData.EXTRA_SHOT_TAG)) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
					Buff.increase(data, damage, BuffStatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
		
		// Buff burn and frost application
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.BURN) && !ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.multiplier(data, statusBuff, BuffStatTracker.statusBuff(statusBuffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE,
				"Passive. Any non-basic attack projectiles you fire have their damage increased by " + 
				DescUtil.yellow(damage) + ". " +
				GlossaryTag.BURN.tag(this) + " and " + GlossaryTag.FROST.tag(this) + " application are increased by " + 
				DescUtil.yellow((int)(statusBuff * 100) + "%") + ".");
	}
}
