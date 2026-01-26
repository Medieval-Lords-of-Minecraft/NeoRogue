package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
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

public class SilentSteps2 extends Equipment {
	private static final String ID = "SilentSteps2";
	private int duration, damage;
	
	public SilentSteps2(boolean isUpgraded) {
		super(ID, "Silent Steps II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = 3;
		damage = isUpgraded ? 30 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID,  Trigger.PRE_RECEIVE_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev.getDurationBuffList().add(new Buff(data, duration, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
		
		
		data.addTrigger(ID, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			if (!pdata.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(pdata, damage, 0, StatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"Passive. Whenever you receive " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by <white>" + duration + "</white>"
						+ " and its stacks by <white>1</white>" +
						" Damage dealt is increased by <yellow>" + damage + "</yellow> if you have " + GlossaryTag.STEALTH.tag(this) +".");
	}
}
