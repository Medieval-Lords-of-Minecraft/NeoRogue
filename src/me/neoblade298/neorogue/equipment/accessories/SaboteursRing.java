package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
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
		inc = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.INJURY)) return TriggerResult.remove();
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, inc * fd.getStatus(StatusType.INJURY).getStacks(), 0), BuffOrigin.NORMAL, true);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COBWEB, "Damage dealt to enemies with " + GlossaryTag.INJURY.tag(this) + " is increased by "  + DescUtil.yellow(inc) +
		" for every stack of " + GlossaryTag.INJURY.tag(this) + " they have.");
	}
}