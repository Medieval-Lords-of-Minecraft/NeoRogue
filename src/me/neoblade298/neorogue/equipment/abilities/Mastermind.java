package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class Mastermind extends Equipment {
	private static final String ID = "Mastermind";
	private double initialMult, finalMult;
	private int physDamage = 20;
	
	public Mastermind(boolean isUpgraded) {
		super(ID, "Mastermind", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		initialMult = isUpgraded ? 0.3 : 0.2;
		finalMult = isUpgraded ? 0.6 : 0.4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
        ActionMeta am = new ActionMeta();
        Equipment eq = this;
		// Add physical damage buff immediately
		data.addDamageBuff(DamageBuffType.of(DamageCategory.PHYSICAL), 
			Buff.increase(data, physDamage, StatTracker.damageBuffAlly(id + slot, this)));
		
		// Start with initial multiplier for status application
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
            if (am.getBool()) return TriggerResult.remove();
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			
			// Only apply to electrified and poison
			if (ev.isStatus(StatusType.ELECTRIFIED) || ev.isStatus(StatusType.POISON)) {
				ev.getStacksBuffList().add(Buff.multiplier(data, initialMult, BuffStatTracker.statusBuff(id + slot, this)));
			}
			
			return TriggerResult.keep();
		});
		
		// After 20 seconds (400 ticks), upgrade to final multiplier
		data.addTask(new BukkitRunnable() {
			public void run() {
				// Remove the old trigger
                am.setBool(true);
				
				// Add new trigger with doubled multiplier
				data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
					PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
					
					// Only apply to electrified and poison
					if (ev.isStatus(StatusType.ELECTRIFIED) || ev.isStatus(StatusType.POISON)) {
						ev.getStacksBuffList().add(Buff.multiplier(data, finalMult, BuffStatTracker.statusBuff(id + slot, eq)));
					}
					
					return TriggerResult.keep();
				});
			}
		}.runTaskLater(NeoRogue.inst(), 400L));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BRAIN_CORAL,
				"Passive. Increase " + GlossaryTag.ELECTRIFIED.tag(this) + " and " + GlossaryTag.POISON.tag(this) + 
				" application by <yellow>" + (int)(initialMult * 100) + "%</yellow>, doubling after <white>20s</white>. " +
                "Increase " + GlossaryTag.PHYSICAL.tag(this) + " damage by <yellow>" + physDamage + "</yellow>.");
	}
}
