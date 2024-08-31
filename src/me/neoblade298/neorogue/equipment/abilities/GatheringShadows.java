package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class GatheringShadows extends Equipment {
	private static final String ID = "gatheringShadows";
	private int damage;
	
	public GatheringShadows(boolean isUpgraded) {
		super(ID, "Gathering Shadows", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(GatheringShadows2.get(), ShadowImbuement.get(), Atrophy.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			if (inst.getCount() <= 0) return TriggerResult.keep();
			BasicAttackEvent ev = (BasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage * inst.getCount(), DamageType.DARK));
			inst.setCount(0);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (inst.getCount() >= 10) return TriggerResult.keep();
			inst.addCount(1);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.BASIC_ATTACK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"Passive. Gain <white>1</white> stack per second, up to <white>10</white>. On basic attack, deal "
				+ GlossaryTag.DARK.tag(this, damage, true) + " damage per stack and reset the stack counter.");
	}
}
