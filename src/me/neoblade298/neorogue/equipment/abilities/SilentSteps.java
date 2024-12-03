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
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class SilentSteps extends Equipment {
	private static final String ID = "silentSteps";
	private int duration, damage;
	
	public SilentSteps(boolean isUpgraded) {
		super(ID, "Silent Steps", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		duration = isUpgraded ? 2 : 1;
		damage = isUpgraded ? 25 : 15;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Fade.get(), Vanish.get(), SilentSteps2.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID,  Trigger.PRE_RECEIVE_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.STEALTH.name())) return TriggerResult.keep();
			ev.getDurationBuffList().addIncrease(data, 20);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			if (!pdata.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			ev.getMeta().addBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(pdata, damage * pdata.getStatus(StatusType.STEALTH).getStacks(), 0), true);
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
