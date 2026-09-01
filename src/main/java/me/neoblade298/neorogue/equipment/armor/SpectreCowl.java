package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SpectreCowl extends Equipment {
	private static final String ID = "SpectreCowl";
	private static final int MANA_THRESHOLD = 60, SHIELD_DURATION = 5;
	private int shields;

	public SpectreCowl(boolean isUpgraded) {
		super(ID, "Spectre Cowl", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ARMOR, EquipmentProperties.none());
		shields = isUpgraded ? 3 : 2;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta refresh = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (pdata.getMana() <= pdata.getMaxMana() * MANA_THRESHOLD / 100.0) {
				refresh.setCount(0);
				return TriggerResult.keep();
			}
			if (refresh.addCount(1) >= SHIELD_DURATION) {
				Player player = data.getPlayer();
				data.addSimpleShield(player.getUniqueId(), shields, SHIELD_DURATION * 20, this);
				refresh.setCount(0);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET, GlossaryTag.PASSIVE.tag(this) + ". While above "
				+ DescUtil.val(MANA_THRESHOLD + "%") + " mana, gain " + GlossaryTag.SHIELDS.tag(this, shields)
				+ " " + DescUtil.duration(SHIELD_DURATION) + " every " + DescUtil.val(SHIELD_DURATION + "s") + ".");
	}
}