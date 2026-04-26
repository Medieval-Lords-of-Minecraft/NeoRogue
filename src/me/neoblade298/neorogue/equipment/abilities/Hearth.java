package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Hearth extends Equipment {
	private static final String ID = "Hearth";
	private static final int INTERVAL = 10; // 1 PLAYER_TICK = 1 second
	private int heal;

	public Hearth(boolean isUpgraded) {
		super(ID, "Hearth", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 60 : 80, 0, 0, 0));
		heal = 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		am.setCount(-1);

		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			am.setCount(0);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (am.getCount() < 0) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() >= INTERVAL) {
				am.setCount(0);
				if (data.hasStatus(StatusType.CORRUPTION)) {
					Player p = data.getPlayer();
					data.applyStatus(StatusType.CORRUPTION, data, -1, -1);
					data.addHealth(heal);
					Sounds.success.play(p, p);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CAMPFIRE,
				"On cast, every [<white>10s</white>], remove " + GlossaryTag.CORRUPTION.tag(this, 1, false) +
				" if you have it to heal for " + DescUtil.yellow(heal) + ".");
	}
}
