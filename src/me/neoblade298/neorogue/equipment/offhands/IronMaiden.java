package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class IronMaiden extends Equipment {
	private static final String ID = "ironMaiden";
	private int reduction, thorns;

	public IronMaiden(boolean isUpgraded) {
		super(ID, "Iron Maiden", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 6 : 8;
		thorns = isUpgraded ? 35 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, inputs) -> {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduction, 0, StatTracker.defenseBuffAlly(am.getId(), this)));
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			data.applyStatus(StatusType.THORNS, data, thorns, -1);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (am.getCount() <= 0) return TriggerResult.keep();
			int stacks = data.getStatus(StatusType.THORNS).getStacks() / 2;
			ev.getMeta().addDamageSlice(new DamageSlice(data, stacks, DamageType.THORNS, DamageStatTracker.of(id + slot, this)));
			am.addCount(-1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduces all damage by <yellow>" + reduction + "</yellow>. Receiving damage while your shield is raised " +
		"grants " + GlossaryTag.THORNS.tag(this, thorns, true) + " and empowers your next basic attack to deal half your current " +
		GlossaryTag.THORNS.tag(this) + " stacks as " + GlossaryTag.THORNS.tag(this) + " damage.");
	}
}
