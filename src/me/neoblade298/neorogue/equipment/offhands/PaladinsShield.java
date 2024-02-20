package me.neoblade298.neorogue.equipment.offhands;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class PaladinsShield extends Equipment {
	private int reduction, sanct;
	
	public PaladinsShield(boolean isUpgraded) {
		super("paladinsShield", "Paladin's Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.OFFHAND);
		reduction = isUpgraded ? 7 : 5;
		sanct = isUpgraded ? 6 : 4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RAISE_SHIELD, (pdata, inputs) -> {
			data.setBarrier(Barrier.centered(p, 4, 2, 2, 0, new HashMap<BuffType, Buff>()));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.SHIELD_TICK, (pdata, inputs) -> {
			data.getBarrier().tick();
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, inputs) -> {
			data.setBarrier(null);
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, inputs) -> {
			if (p.getHandRaised() != EquipmentSlot.OFF_HAND) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) inputs;
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(p.getUniqueId(), reduction, 0), BuffOrigin.SHIELD, false);
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1F, 1F);
			ev.getMeta().getOwner().applyStatus(StatusType.SANCTIFIED, p.getUniqueId(), sanct, -1);
			ev.getMeta().getReturnDamage().addDamageSlice(new DamageSlice(p.getUniqueId(), 5, DamageType.LIGHT));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "When raised, creates a " + GlossaryTag.BARRIER.tag(this) + " of size <white>4x3</white>."
				+ " Reduce all damage by <yellow>" + reduction + "</yellow>, apply <yellow>" + sanct + "</yellow> " + GlossaryTag.SANCTIFIED.tag(this)
				+ " to damagers and deal <white>5</white> " + GlossaryTag.LIGHT.tag(this) + " damage to them.");
	}
}
