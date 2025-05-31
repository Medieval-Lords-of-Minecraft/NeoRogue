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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AvatarState extends Equipment {
	private static final String ID = "avatarState";
	private double mreg, hreg;
	private int shields;

	public AvatarState(boolean isUpgraded) {
		super(ID, "Avatar State", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 40, 0));
		mreg = isUpgraded ? 2.5 : 1.5;
		hreg = isUpgraded ? 1.5 : 1;
		shields = isUpgraded ? 10 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		am.setCount(-1);
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			am.setCount(0);
			Sounds.fire.play(p, p);
			data.addManaRegen(mreg);
			data.addSimpleShield(p.getUniqueId(), shields, 200);
			return TriggerResult.keep();
		}));

		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (am.getCount() == -1)
				return TriggerResult.keep();
			am.addCount(1);
			data.addHealth(hreg);
			if (am.getCount() >= 10) {
				data.addManaRegen(-mreg);
				am.setCount(-1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE,
				"On cast, increase mana regen by " + DescUtil.yellow(mreg) + ",  health regen by "
						+ DescUtil.yellow(hreg) + ", and gain " + GlossaryTag.SHIELDS.tag(this, shields, true)
						+ " for <white>10s</white> or until you receive health damage.");
	}
}
