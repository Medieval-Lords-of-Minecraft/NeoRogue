package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class RadiantChassis extends Equipment {
	private static final String ID = "RadiantChassis";
	private static final int PHYSICAL_REDUCTION = 1;
	private int magicalReduction, returnDamage;

	public RadiantChassis(boolean isUpgraded) {
		super(ID, "Radiant Chassis", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		magicalReduction = isUpgraded ? 4 : 3;
		returnDamage = isUpgraded ? 180 : 120;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, magicalReduction,
				StatTracker.defenseBuffAlly(id + slot + "-magical", this)));
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, PHYSICAL_REDUCTION,
				StatTracker.defenseBuffAlly(id + slot + "-physical", this)));
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			ev.getMeta().getReturnDamage().addDamageSlice(new DamageSlice(data, returnDamage, DamageType.LIGHT,
					DamageStatTracker.of(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_CHESTPLATE, "Reduce " + GlossaryTag.MAGICAL.tag(this)
				+ " damage taken by " + DescUtil.yellow(magicalReduction) + " and "
				+ GlossaryTag.PHYSICAL.tag(this) + " damage taken by " + DescUtil.white(PHYSICAL_REDUCTION)
				+ ". Receiving damage deals " + GlossaryTag.LIGHT.tag(this, returnDamage)
				+ " damage to the attacker.");
	}
}