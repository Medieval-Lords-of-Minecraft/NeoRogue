package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class Initiator extends Equipment {
	private static final String ID = "initiator";
	private int damage;
	
	public Initiator(boolean isUpgraded) {
		super(ID, "Initiator", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		damage = isUpgraded ? 50 : 30;
		
		tags.add(GlossaryTag.SHIELDS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd.hasStatus(p.getName() + "-INITIATOR")) return TriggerResult.keep();
			fd.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, p.getName() + "-INITIATOR",
					fd, true), data, 1, -1, ev.getMeta());
			ev.getMeta().addBuff(BuffType.GENERAL, new Buff(data, 0, damage * 0.01), BuffOrigin.NORMAL, true);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"Passive. The first time you deal non-status damage to an enemy, increase the damage by <yellow>" + damage + "%</yellow>.");
	}
}
