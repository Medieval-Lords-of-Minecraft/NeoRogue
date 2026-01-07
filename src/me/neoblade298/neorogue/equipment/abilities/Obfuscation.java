package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class Obfuscation extends Equipment {
	private static final String ID = "Obfuscation";
	private int duration;
	private double insanityMult;
	
	public Obfuscation(boolean isUpgraded) {
		super(ID, "Obfuscation", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		duration = isUpgraded ? 8 : 3;
		insanityMult = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(PiercingNight.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			
			// Increase stealth and evade application by 1
			if (ev.isStatus(StatusType.STEALTH) || ev.isStatus(StatusType.EVADE)) {
				ev.getStacksBuffList().add(Buff.increase(data, 1, BuffStatTracker.statusBuff(id, this)));
				ev.getDurationBuffList().add(new Buff(data, duration * 20, 0, BuffStatTracker.ignored(this)));
			}
			// Increase insanity application by 20%/30%
			else if (ev.isStatus(StatusType.INSANITY)) {
				ev.getStacksBuffList().add(Buff.multiplier(data, insanityMult, BuffStatTracker.statusBuff(id, this)));
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				"Passive. " + GlossaryTag.STEALTH.tag(this) + " and " + GlossaryTag.EVADE.tag(this) + 
				" application is increased by <yellow>1</yellow>, and their durations are increased by <yellow>" + 
				duration + "s</yellow>. " + GlossaryTag.INSANITY.tag(this) + 
				" application is increased by <yellow>" + (int)(insanityMult * 100) + "%</yellow>.");
	}
}
