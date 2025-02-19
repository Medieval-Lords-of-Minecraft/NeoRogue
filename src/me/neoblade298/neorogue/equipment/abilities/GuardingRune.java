package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GuardingRune extends Equipment {
	private static final String ID = "guardingRune";
	private int shields;
	
	public GuardingRune(boolean isUpgraded) {
		super(ID, "Guarding Rune", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND, EquipmentProperties.none());
		shields = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Trigger tr = data.getSessionData().getPlayerClass() == EquipmentClass.ARCHER ? Trigger.LEFT_CLICK : Trigger.RIGHT_CLICK;
		data.addTrigger(id, tr, (pdata, in) -> {
			Sounds.blazeDeath.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 200);
			p.getInventory().setItemInOffHand(null);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COBBLESTONE,
				"On right click (left click for <gold>Archer</gold>), gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for <white>10s</white> once per fight.");
	}
}
