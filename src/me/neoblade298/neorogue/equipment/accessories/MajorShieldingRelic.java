package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;

public class MajorShieldingRelic extends Equipment {
	private static final String ID = "majorShieldingRelic";
	private double mult;
	private int multStr;
	
	public MajorShieldingRelic(boolean isUpgraded) {
		super(ID, "Major Shielding Relic", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		mult = isUpgraded ? 0.1 : 0.05;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		int shields = (int) (data.getMaxHealth() * mult);
		data.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pdata, in) -> {
			GrantShieldsEvent ev = (GrantShieldsEvent) in;
			if (ev.isSecondary()) return TriggerResult.keep();
			ev.getAmountBuff().add(Buff.increase(data, shields, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, "Whenever you gain " + GlossaryTag.SHIELDS.tag(this) + ", increase the amount gained by "
				+ DescUtil.yellow(multStr + "%") + " of your max health.");
	}
}
