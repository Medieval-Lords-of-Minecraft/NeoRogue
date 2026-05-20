package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;

public class EnduranceTraining extends Equipment {
	private static final String ID = "EnduranceTraining";
	private int dur, shields;
	
	public EnduranceTraining(boolean isUpgraded) {
		super(ID, "Endurance Training", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				dur = isUpgraded ? 3 : 2;
				shields = 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		data.addPermanentShield(p.getUniqueId(), shields);
		data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata, in) -> {
			GrantShieldsEvent ev = (GrantShieldsEvent) in;
			ev.getAmountBuff().add(Buff.increase(data, dur, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_INGOT,
				"Passive. Increase duration of all applied " + GlossaryTag.SHIELDS.tag(this) + " by " + DescUtil.yellow(dur) + ". " +
				"Start fights with " + GlossaryTag.SHIELDS.tag(this, shields, false) + ".");
	}
}
