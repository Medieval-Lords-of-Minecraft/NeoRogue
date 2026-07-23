package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
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
		canDrop = false;
		removable = false;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND, "Increases your armor slots by " + DescUtil.val(1) + ", up to a max of " + DescUtil.val(4) + ".");
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.addArmorSlots(1);
		PlayerSessionInventory.setupInventory(data.getPlayer().getInventory(),data);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}
}
