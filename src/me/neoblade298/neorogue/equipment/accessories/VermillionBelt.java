package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class VermillionBelt extends Equipment {
	private static final String ID = "vermillionBelt";
	private int thres, berserk;
	public VermillionBelt(boolean isUpgraded) {
		super(ID, "Vermillion Belt", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		thres = isUpgraded ? 5 : 4;
		berserk = 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (am.addCount(1) >= thres) {
				Sounds.fire.play(p, p);
				data.applyStatus(StatusType.BERSERK, data, berserk, -1);
				am.setCount(0);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CORAL_FAN, "Every " + DescUtil.yellow(thres) + " basic attacks, apply " + GlossaryTag.BERSERK.tag(this, berserk, false) +
		" to yourself.");
	}
}
