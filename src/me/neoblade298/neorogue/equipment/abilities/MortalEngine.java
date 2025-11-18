package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CheckCastUsableEvent;

public class MortalEngine extends Equipment {
	private static final String ID = "MortalEngine";
	private int cutoff, reduc;

	public MortalEngine(boolean isUpgraded) {
		super(ID, "Mortal Engine", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.none());

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
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		inst.setAction((pdata, in) -> {
			CheckCastUsableEvent ev = (CheckCastUsableEvent) in;
			if (ev.getInstance().getStaminaCost() > 0) {
				ev.addBuff(PropertyType.STAMINA_COST, ID,
						new Buff(data, inst.getCount(), 0, BuffStatTracker.of(id + slot, this, "Stamina cost reduced")));
			}
			if (ev.getInstance().getStaminaCost() < cutoff)
				return TriggerResult.keep();
			inst.addCount(reduc);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.PRE_CAST_USABLE, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				"Passive. For every ability cast that has a base cost of at least <white>" + cutoff
						+ "</white> stamina, reduce the stamina cost of all abilities by <yellow>" + reduc
						+ "</yellow>.");
	}
}
