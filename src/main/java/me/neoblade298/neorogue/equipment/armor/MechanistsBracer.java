package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MechanistsBracer extends Equipment {
	private static final String ID = "MechanistsBracer";
	private int shields;

	public MechanistsBracer(boolean isUpgraded) {
		super(ID, "Mechanist's Bracer", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ARMOR);
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			Player player = data.getPlayer();
			data.addPermanentShield(player.getUniqueId(), shields, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHAINMAIL_CHESTPLATE, "Whenever you lay a " + GlossaryTag.TRAP.tag(this)
				+ ", gain " + GlossaryTag.SHIELDS.tag(this, shields) + ".");
	}
}