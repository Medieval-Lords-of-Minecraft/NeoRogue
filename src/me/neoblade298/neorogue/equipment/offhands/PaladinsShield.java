package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffList;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageBarrierEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class PaladinsShield extends Equipment {
	private static final String ID = "PaladinsShield";
	private int reduction, sanct;
	
	public PaladinsShield(boolean isUpgraded) {
		super(ID, "Paladin's Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 7 : 5;
		sanct = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RAISE_SHIELD, (pdata, inputs) -> {
			if (am.getCount() >= 5) return TriggerResult.remove();
			Player p = data.getPlayer();
			Barrier b = Barrier.centered(p, 4, 2, 2, 0, new HashMap<DamageBuffType, BuffList>(), null, true);
			UUID uuid = data.addBarrier(b);
			b.tick();
			am.setUniqueId(uuid);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.SHIELD_TICK, (pdata, inputs) -> {
			if (am.getCount() >= 5) return TriggerResult.remove();
			Barrier b = data.getBarrier(am.getUniqueId());
			if (b == null) return TriggerResult.keep();
			b.tick();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, inputs) -> {
			if (am.getCount() >= 5) return TriggerResult.remove();
			data.removeBarrier(am.getUniqueId());
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RECEIVE_DAMAGE_BARRIER, (pdata, in) -> {
			ReceiveDamageBarrierEvent ev = (ReceiveDamageBarrierEvent) in;
			Barrier b = ev.getBarrier();
			if (!b.getUniqueId().equals(am.getUniqueId())) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() >= 5) {
				Player p = data.getPlayer();
				data.removeBarrier(am.getUniqueId());
				p.playSound(p, Sound.ITEM_SHIELD_BREAK, 1F, 1F);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, inputs) -> {
			Player p = data.getPlayer();
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND || !p.isHandRaised()) return TriggerResult.keep();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) inputs;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, reduction, 0, StatTracker.defenseBuffAlly(am.getId(), this)));
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			ev.getMeta().getOwner().applyStatus(StatusType.SANCTIFIED, data, sanct, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, reduces all damage by <yellow>" + reduction + "</yellow> and applies <yellow>" + sanct + "</yellow> " + GlossaryTag.SANCTIFIED.tag(this)
				+ " to damagers. Also creates a " + GlossaryTag.BARRIER.tag(this) + " that blocks <white>5</white> projectiles before breaking.");
	}
}
