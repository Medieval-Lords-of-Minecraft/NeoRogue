package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.weapons.ColdArrow;
import me.neoblade298.neorogue.equipment.weapons.LitArrow;
import me.neoblade298.neorogue.equipment.weapons.WoodenArrow;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class BasicElementMastery extends Equipment {
	private static final String ID = "basicElementMastery";
	private int shields, burn;
	
	public BasicElementMastery(boolean isUpgraded) {
		super(ID, "Basic Element Mastery", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				shields = isUpgraded ? 5 : 3;
				burn = isUpgraded ? 5 : 3;
	}

	@Override
	public void setupReforges() {
		addReforge(WoodenArrow.get(), ColdArrow.get(), LitArrow.get());
		addSelfReforge(Firebomb.get());
		addReforge(Sear.get(), Firebomb.get());
		addReforge(AgilityTraining.get(), Blind.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), 5, false);
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			ev.getStacksBuff().addIncrease(data, 1);
			data.addSimpleShield(p.getUniqueId(), shields, 60);
			return TriggerResult.keep();
		}));
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.FIRE)) return TriggerResult.keep();
			FightInstance.applyStatus(ev.getTarget(), StatusType.BURN, data, burn, -1);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_CREAM,
				"Passive. Start fights with " + GlossaryTag.SHIELDS.tag(this, 5, false) + ". Increase application of " + GlossaryTag.FROST.tag(this) +
				" by <white>1</white>. Dealing " + GlossaryTag.FIRE.tag(this) + " damage additionally applies "
				+ GlossaryTag.BURN.tag(this, burn, true) + ".");
	}
}