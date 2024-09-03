package me.neoblade298.neorogue.equipment.cursed;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.weapons.Nightmare;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.CustomGlossaryIcon;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class DullDagger extends Equipment {
	private static final String ID = "dullDagger";
	
	public DullDagger() {
		super(ID, "Dull Dagger", EquipmentType.ACCESSORY);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {}
	

	@Override
	public void postSetup() {
		tags.add(new CustomGlossaryIcon("nightmare", Nightmare.get().getItem()));
	}
	
	@Override
	public void onPurify(PlayerSessionData data) {
		data.giveEquipment(Nightmare.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLADE_POTTERY_SHERD, "When purified at a shop, you receive a " + NeoCore.miniMessage().serialize(Nightmare.get().getDisplay()) +
				"<gray>. Right click for more info.");
	}
}
