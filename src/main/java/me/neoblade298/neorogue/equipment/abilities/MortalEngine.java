package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class MortalEngine extends Equipment implements Power {
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
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent cev = (CastUsableEvent) in;
			if (cev.getInstance().getStaminaCost() < cutoff) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
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
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after casting an ability that costs " + DescUtil.white(cutoff) + "+ stamina. When you cast an ability with base cost of at least " + DescUtil.white(cutoff)
						+ " stamina, reduce the stamina cost of all abilities by " + DescUtil.yellow(reduc)
						+ ".");
	}
}
