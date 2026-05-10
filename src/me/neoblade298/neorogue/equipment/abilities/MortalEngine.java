package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class MortalEngine extends Equipment {
	private static final String ID = "MortalEngine";
	private int cutoff, reduc;

	public MortalEngine(boolean isUpgraded) {
		super(ID, "Mortal Engine", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, 0, 0));

		cutoff = 15;
		reduc = isUpgraded ? 2 : 1;
	}

	public void setupReforges() {
		addReforge(Brace.get(), Tireless.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			StandardPriorityAction inst = new StandardPriorityAction(ID);
			inst.setAction((pdata2, in2) -> {
				PreCastUsableEvent ev = (PreCastUsableEvent) in2;
				if (ev.getInstance().getStaminaCost() > 0) {
					ev.addBuff(PropertyType.STAMINA_COST, ID,
							new Buff(data, inst.getCount(), 0, BuffStatTracker.of(id + slot, this, "Stamina cost reduced")));
				}
				if (ev.getInstance().getStaminaCost() < cutoff)
					return TriggerResult.keep();
				inst.addCount(reduc);
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PRE_CAST_USABLE, inst);

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				GlossaryTag.POWER.tag(this) + ". For every ability cast that has a base cost of at least " + DescUtil.white(cutoff)
						+ " stamina, reduce the stamina cost of all abilities by " + DescUtil.yellow(reduc)
						+ ".");
	}
}
