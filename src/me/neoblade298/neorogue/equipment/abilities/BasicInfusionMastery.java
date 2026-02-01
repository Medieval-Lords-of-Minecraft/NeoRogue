package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class BasicInfusionMastery extends Equipment {
	private static final String ID = "BasicInfusionMastery";
	private int conc, heal;
	
	public BasicInfusionMastery(boolean isUpgraded) {
		super(ID, "Basic Infusion Mastery", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				conc = isUpgraded ? 4 : 3;
				heal = isUpgraded ? 10 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta meta = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.isStatus(StatusType.CONCUSSED)) {
				ev.getStacksBuffList().add(Buff.increase(data, conc, BuffStatTracker.ignored(this)));
			}
			else if (ev.isStatus(StatusType.SANCTIFIED)) {
				meta.setBool(true);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			Player p = data.getPlayer();
			if (meta.getBool()) {
				FightInstance.giveHeal(p, heal, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI,
				"Passive. Any " + GlossaryTag.CONCUSSED.tag(this) + " you apply is increased by " + DescUtil.yellow(conc) + ". If you apply any " +
				GlossaryTag.SANCTIFIED.tag(this) + " during the fight, heal for " + DescUtil.yellow(heal) + " at the end of the fight.");
	}
}
