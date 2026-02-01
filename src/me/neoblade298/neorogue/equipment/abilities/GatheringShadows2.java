package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class GatheringShadows2 extends Equipment {
	private static final String ID = "GatheringShadows2";
	private int damage, bonus;
	
	public GatheringShadows2(boolean isUpgraded) {
		super(ID, "Gathering Shadows II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = 12;
		bonus = isUpgraded ? 8 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			if (inst.getCount() <= 0) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			double dmg = (damage * inst.getCount());
			if (inst.getCount() > 2) dmg += bonus * (inst.getCount() - 2);
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, dmg, DamageType.DARK,
					DamageStatTracker.of(ID + slot, this)));
			inst.setCount(0);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (inst.getCount() >= 10) return TriggerResult.keep();
			inst.addCount(1);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"Passive. Gain <white>1</white> stack per second, up to <white>10</white>. On basic attack, deal "
				+ GlossaryTag.DARK.tag(this, damage, true) + " damage per stack plus <yellow>" + bonus + "</yellow> for every stack above "
				+ "<white>2</white> and reset the stack counter.");
	}
}
