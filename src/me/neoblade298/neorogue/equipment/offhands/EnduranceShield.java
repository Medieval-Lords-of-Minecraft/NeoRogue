package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.accessories.RingOfAnger;
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
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class EnduranceShield extends Equipment {
	private static final String ID = "enduranceShield";
	private int reduction, damage;

	public EnduranceShield(boolean isUpgraded) {
		super(ID, "Endurance Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 6 : 8;
		damage = isUpgraded ? 250 : 150;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(RingOfAnger.get(), VengefulShield.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, inputs) -> {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduction, 0, StatTracker.defenseBuffAlly(am.getId(), this)));
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			am.addCount(1);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (am.getCount() <= 0) return TriggerResult.keep();
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.BLUNT, DamageStatTracker.of(id, this)));
			am.addCount(-1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduces all damage by <yellow>" + reduction + "</yellow>. Receiving damage while your shield is raised " +
		"empowers your next basic attack to deal " + GlossaryTag.BLUNT.tag(this, damage, true) + ".");
	}
}
