package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.PlayerSessionInventory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class ArmorStand extends Artifact {
	private static final String ID = "ArmorStand";

	public ArmorStand() {
		super(ID, "Armor Stand", Rarity.UNCOMMON, EquipmentClass.SHOP);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND, "Increases your armor slots by <white>1</white>, up to a max of <white>3</white>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.addArmorSlots(1);
		PlayerSessionInventory.setupInventory(data);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}
}
