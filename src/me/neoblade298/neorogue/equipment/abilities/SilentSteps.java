package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

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

public class SilentSteps extends Equipment {
	private static final String ID = "silentSteps";
	private int duration, damage;
	
	public SilentSteps(boolean isUpgraded) {
		super(ID, "Silent Steps", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = isUpgraded ? 3 : 2;
		damage = isUpgraded ? 25 : 15;
	}

	@Override
	public void setupReforges() {
		addReforge(BasicManaManipulation.get(), SilentSteps2.get(), Fade.get(), Vanish.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(ID,  Trigger.PRE_RECEIVE_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev.getDurationBuffList().add(new Buff(data, 20, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			if (!pdata.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(pdata, damage * pdata.getStatus(StatusType.STEALTH).getStacks(), 0, StatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_BOOTS,
				"Passive. Whenever you become " + GlossaryTag.STEALTH.tag(this) + ", increase its duration by <yellow>" + duration + "</yellow>." +
				" Damage dealt is increased by <yellow>" + damage + "</yellow> per stack of " + GlossaryTag.STEALTH.tag(this) +".");
	}
}
