package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LayTrapEvent;

public class EngineersCap extends Equipment {
	private static final String ID = "engineersCap";
	private int dur, dec;
	
	public EngineersCap(boolean isUpgraded) {
		super(ID, "Engineer's Cap", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		dur = isUpgraded ? 5 : 3;
		dec = isUpgraded ? 4 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, dec, StatTracker.defenseBuffAlly(this)));
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			LayTrapEvent ev = (LayTrapEvent) in;
			ev.getDurationBuffList().add(Buff.increase(data, dur * 20, StatTracker.IGNORED));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, "Decrease " + GlossaryTag.PHYSICAL.tag(this) + " damage taken by " + DescUtil.yellow(dec) + ". Any " + GlossaryTag.TRAP.tagPlural(this) +
				" you lay will last " + DescUtil.yellow(dur + "s") + " longer.");
	}
}
