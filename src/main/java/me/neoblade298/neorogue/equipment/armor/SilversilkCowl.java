package me.neoblade298.neorogue.equipment.armor;
import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
import me.neoblade298.neorogue.session.fight.trigger.event.PreEvadeEvent;

public class SilversilkCowl extends Equipment {
	private static final String ID = "SilversilkCowl";
	private int evade = 2, def = 2;
	private double mult;
	
	public SilversilkCowl(boolean isUpgraded) {
		super(ID, "Silversilk Cowl", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ARMOR);
		// damage per stamina, 1.5 (2.0) damage for 1 stamina
		mult = isUpgraded ? 1.0 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		// Grant evade at the start of the fight
		data.applyStatus(StatusType.EVADE, data, evade, -1, this);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, def, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		
		// Increase the damage mitigated per stamina
		data.addTrigger(id, Trigger.PRE_EVADE, (pdata, in) -> {
			PreEvadeEvent ev = (PreEvadeEvent) in;
			ev.getStaminaCostBuff().add(Buff.multiplier(data, mult, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		int pct = (int)(mult / (1.0 + mult) * 100);
		item = createItem(Material.CHAINMAIL_HELMET,
				"Increase " + GlossaryTag.GENERAL.tag(this) + " defense by " + DescUtil.val(def)
				+ ". Start every fight with " + DescUtil.val(evade) + " " + GlossaryTag.EVADE.tag(this) + ". "
				+ "The amount of stamina that " + GlossaryTag.EVADE.tag(this) + " consumes per damage mitigated is decreased by " + DescUtil.val(pct + "%") + ".");
	}
}
