package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HavenTome extends Equipment {
	private static final String ID = "HavenTome";
	private static final int THRES_SQ = 25;
	private int shields;

	public HavenTome(boolean isUpgraded) {
		super(ID, "Haven Tome", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(10, 0, 10, 0));
		shields = isUpgraded ? 10 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.fire.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			return TriggerResult.keep();
		},
		(p2, pdata2, in2) -> {
			for (Rift rift : data.getRifts().values()) {
				if (rift.getLocation().distanceSquared(p2.getLocation()) <= THRES_SQ) {
					return true;
				}
			}
			return false;
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On right click, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) +
				" [<white>5s</white>] if you are within <white>5</white> blocks of one of your " + GlossaryTag.RIFT.tagPlural(this) + ".");
	}
}
