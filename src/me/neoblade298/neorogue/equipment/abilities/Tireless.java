package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
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
import me.neoblade298.neorogue.session.fight.trigger.event.CheckCastUsableEvent;

public class Tireless extends Equipment {
	private static final String ID = "Tireless";
	private int cutoff, reduc, shields;

	public Tireless(boolean isUpgraded) {
		super(ID, "Tireless", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.none());

		cutoff = 20;
		reduc = isUpgraded ? 3 : 2;
		shields = isUpgraded ? 15 : 10;
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
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.PRE_CAST_USABLE, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				"Passive. For every ability cast that has a base cost of at least <white>" + cutoff
						+ "</white> stamina, reduce the stamina cost of all abilities by <yellow>" + reduc
						+ "</yellow> and gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}
