package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class GatheringShadows extends Equipment {
	private static final String ID = "GatheringShadows";
	private int damage;
	
	public GatheringShadows(boolean isUpgraded) {
		super(ID, "Gathering Shadows", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 0, 0, 0));
		damage = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(BasicManaManipulation.get(), GatheringShadows2.get(), ShadowImbuement.get(), Atrophy.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			StandardPriorityAction inst = new StandardPriorityAction(ID);
			inst.setAction((pdata2, in2) -> {
				if (inst.getCount() <= 0) return TriggerResult.keep();
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in2;
				ev.getMeta().addDamageSlice(new DamageSlice(pdata2, damage * inst.getCount(), DamageType.DARK,
						DamageStatTracker.of(ID + slot, this)));
				inst.setCount(-1);
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.PLAYER_TICK, (pdata3, in3) -> {
				if (inst.getCount() >= 10) return TriggerResult.keep();
				inst.addCount(1);
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, inst);

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				GlossaryTag.POWER.tag(this) + ". On cast, gain " + DescUtil.white(1) + " stack per second, up to " + DescUtil.white(10) + ". On basic attack, deal "
				+ GlossaryTag.DARK.tag(this, damage, true) + " damage per stack and reset the stack counter.");
	}
}
