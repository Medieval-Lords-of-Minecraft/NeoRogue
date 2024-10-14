package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class BasicFireMastery extends Equipment {
	private static final String ID = "basicFireMastery";
	private int burn;
	
	public BasicFireMastery(boolean isUpgraded) {
		super(ID, "Basic Fire Mastery", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				burn = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addBuff(data, true, false, BuffType.GENERAL, 5);
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, new EquipmentInstance(p, this, slot, es, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.FIRE)) return TriggerResult.keep();
			FightInstance.applyStatus(ev.getTarget(), StatusType.BURN, data, burn, -1);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"Passive. " + GlossaryTag.GENERAL.tag(this) + " damage is increased by <white>5</white>. Dealing fire damage additionally applies "
				+ GlossaryTag.BURN.tag(this, burn, true) + ".");
	}
}
