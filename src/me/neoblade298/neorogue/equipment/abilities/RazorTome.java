package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class RazorTome extends Equipment {
	private static final String ID = "RazorTome";
	private int rend, thres; // If change thres, fix description
	
	public RazorTome(boolean isUpgraded) {
		super(ID, "Razor Tome", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.none());
				rend = isUpgraded ? 30 : 20;
				thres = 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() >= thres) {
				am.addCount(-thres);
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				FightInstance.applyStatus(ev.getTarget(), StatusType.REND, data, rend, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK,
				"Every " + DescUtil.white(thres + "rd") + " basic attack will apply " + GlossaryTag.REND.tag(this, rend, true) + ".");
	}
}
