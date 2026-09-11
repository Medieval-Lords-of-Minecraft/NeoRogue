package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class WarChest extends Artifact {
	private static final String ID = "WarChest";

	public WarChest() {
		super(ID, "War Chest", Rarity.COMMON, EquipmentClass.CLASSLESS);
		markAsStartingBonus();
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		for (int i = 0; i < amount; i++) {
			Equipment weapon = Equipment.getWeapon(data.getData().getEquipmentDroptable(), Rarity.UNCOMMON,
					data.getPlayerClass(), EquipmentClass.CLASSLESS);
			if (weapon != null) data.giveEquipment(weapon);
		}
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHEST, "Obtain a random " + DescUtil.val("uncommon") + " weapon.");
	}
}