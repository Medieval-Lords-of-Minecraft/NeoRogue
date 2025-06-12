package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class ConductiveArmguard extends Equipment {
	private static final String ID = "conductiveArmguard";
	private int shields;

	public ConductiveArmguard(boolean isUpgraded) {
		super(ID, "Conductive Armguard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.none());
		shields = isUpgraded ? 9 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.LIGHTNING))
				return TriggerResult.keep();
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, "Passive. Every time you deal " + GlossaryTag.LIGHTNING.tag(this)
				+ " damage, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}
