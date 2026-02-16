package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Saboteur extends Equipment {
	private static final String ID = "Saboteur";
	private static final int FOCUS_STACKS = 3;
	private static final int DISTANCE_THRESHOLD = 5;
	private double damageBuff, injuryBuff;
	
	public Saboteur(boolean isUpgraded) {
		super(ID, "Saboteur", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY);
		damageBuff = isUpgraded ? 0.15 : 0.10;
		injuryBuff = isUpgraded ? 0.30 : 0.20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Apply focus at start of fight
		data.applyStatus(StatusType.FOCUS, data, FOCUS_STACKS, -1);
		
		String buffId = UUID.randomUUID().toString();
		
		// Trap damage buff
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.TRAP)) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL, DamageOrigin.TRAP), 
					Buff.multiplier(data, damageBuff, StatTracker.damageBuffAlly(buffId + "-trap", this)));
			return TriggerResult.keep();
		});
		
		// Distance-based damage buff
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			Player p = data.getPlayer();
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			double distSq = DISTANCE_THRESHOLD * DISTANCE_THRESHOLD;
			if (ev.getTarget().getLocation().distanceSquared(p.getLocation()) > distSq) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
					Buff.multiplier(data, damageBuff, StatTracker.damageBuffAlly(buffId + "-distance", this)));
			return TriggerResult.keep();
		});
		
		// Injury application buff
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INJURY)) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.multiplier(data, injuryBuff, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"Start fights with <white>" + FOCUS_STACKS + "</white> " + GlossaryTag.FOCUS.tag(this) + ". " +
				GlossaryTag.TRAP.tag(this) + " damage and damage dealt to enemies within <white>" + DISTANCE_THRESHOLD + "</white> blocks " +
				"is increased by " + DescUtil.yellow(damageBuff * 100 + "%") + ". " +
				GlossaryTag.INJURY.tag(this) + " application is increased by " + DescUtil.yellow(injuryBuff * 100 + "%") + ".");
	}
}
