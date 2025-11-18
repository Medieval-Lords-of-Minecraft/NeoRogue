package me.neoblade298.neorogue.equipment.cursed;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.weapons.SilverFang;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.CustomGlossaryIcon;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class RustySword extends Equipment {
	private static final String ID = "RustySword";
	
	public RustySword() {
		super(ID, "Rusty Sword", EquipmentType.ACCESSORY);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {}

	@Override
	public void setupItem() {
		item = createItem(Material.DANGER_POTTERY_SHERD, "When purified at a shop, you receive a " + NeoCore.miniMessage().serialize(SilverFang.get().getDisplay()) +
				"<gray>. Right click for more info.");
	}
	
	@Override
	public void postSetup() {
		tags.add(new CustomGlossaryIcon("silverFang", SilverFang.get().getItem()));
	}
	
	@Override
	public void onPurify(PlayerSessionData data) {
		data.giveEquipment(SilverFang.get());
	}
}
