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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class GuardianSpirit extends Equipment {
	private static final String ID = "guardianSpirit";
	private int shields, sanct;
	
	public GuardianSpirit(boolean isUpgraded) {
		super(ID, "Guardian Spirit", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				shields = isUpgraded ? 6 : 4;
				sanct = isUpgraded ? 20 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addPermanentShield(p.getUniqueId(), shields);
		data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata, in) -> {
			GrantShieldsEvent ev = (GrantShieldsEvent) in;
			ev.getAmountBuff().add(Buff.increase(data, shields, BuffStatTracker.shield(id + slot, this)));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.increase(data, sanct, BuffStatTracker.statusBuff(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"Passive. Increase all " + GlossaryTag.SHIELDS.tag(this) + " application by " + DescUtil.yellow(shields) +
				" and " + GlossaryTag.SANCTIFIED.tag(this) + " application by " + DescUtil.yellow(sanct) + ".");
	}
}
