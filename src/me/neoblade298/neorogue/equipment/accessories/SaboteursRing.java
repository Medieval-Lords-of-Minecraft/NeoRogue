package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class SaboteursRing extends Equipment {
	private static final String ID = "saboteursRing";
	private double inc;
	public SaboteursRing(boolean isUpgraded) {
		super(ID, "Saboteur's Ring", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 0.6 : 0.4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.INJURY)) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
				new Buff(data, inc * fd.getStatus(StatusType.INJURY).getStacks(), 0, StatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COBWEB, "Damage dealt to enemies with " + GlossaryTag.INJURY.tag(this) + " is increased by "  + DescUtil.yellow(inc) +
		" for every stack of " + GlossaryTag.INJURY.tag(this) + " they have.");
	}
}
