package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Enlighten extends Equipment implements Power {
	private static final String ID = "Enlighten";
	private int sanct;
	
	public Enlighten(boolean isUpgraded) {
		super(ID, "Enlighten", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		
		sanct = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata, in) -> {
			am.setBool(true);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.LIGHT)) {
				am.addCount(1);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (!am.getBool() || am.getCount() < 1) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am2 = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.GRANT_SHIELDS, (pdata2, in2) -> {
			am2.setBool(true);
			return TriggerResult.keep();
		});

		data.addTrigger(id + "-attack", Trigger.BASIC_ATTACK, (pdata3, in3) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in3;
			if (am2.getBool()) {
				FightInstance.applyStatus(ev.getTarget(), StatusType.SANCTIFIED, data, sanct, -1);
				am2.setBool(false);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				GlossaryTag.POWER.tag(this) + ". Activates after granting shields and dealing " + GlossaryTag.LIGHT.tag(this) + " damage. Every time you apply " + GlossaryTag.SHIELDS.tag(this) + ", your next basic attack applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ". Does not stack.");
	}
}
