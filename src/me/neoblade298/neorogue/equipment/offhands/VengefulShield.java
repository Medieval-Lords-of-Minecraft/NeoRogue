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
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class VengefulShield extends Equipment {
	private static final String ID = "vengefulShield";
	private int reduction, damage, thres;

	public VengefulShield(boolean isUpgraded) {
		super(ID, "Vengeful Shield", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 8 : 6;
		damage = isUpgraded ? 250 : 150;
		thres = isUpgraded ? 30 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, inputs) -> {
			int berserk = data.getStatus(StatusType.BERSERK).getStacks();
			if ((p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) && berserk < thres) return TriggerResult.keep();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) inputs;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduction, 0, StatTracker.defenseBuffAlly(am.getId(), this)));
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			data.applyStatus(StatusType.BERSERK, data, 1, -1);
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
		"grants " + GlossaryTag.BERSERK.tag(this, 1, false) + " and empowers your next basic attack to deal " +
		GlossaryTag.BLUNT.tag(this, damage, true) + ". At " + GlossaryTag.BERSERK.tag(this, thres, true) + ", you no longer need your shield raised to reduce damage.");
	}
}
