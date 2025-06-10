package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class BasicDarkArts extends Equipment {
	private static final String ID = "basicDarkArts";
	private int stacks, damage;
	
	public BasicDarkArts(boolean isUpgraded) {
		super(ID, "Basic Dark Arts", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
				stacks = isUpgraded ? 15 : 10;
				damage = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addDamageBuff(DamageBuffType.of(DamageCategory.DARK), Buff.increase(data, damage, StatTracker.damageBuffAlly(buffId, this)));
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.isStatus(StatusType.INSANITY)) {
				ev.getStacksBuffList().add(Buff.increase(data, stacks, BuffStatTracker.ignored(this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLACK_CARPET,
				"Passive. Increase " + GlossaryTag.DARK.tag(this) + " damage by " + DescUtil.yellow(damage) + ". Increase " +
				GlossaryTag.INSANITY.tag(this) + " application stacks by " + DescUtil.yellow(stacks) + ".");
	}
}
