package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class RainbowVial extends Artifact {
	private static final String ID = "RainbowVial";
	private final int resourcePercent;

	public RainbowVial() {
		super(ID, "Rainbow Vial", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		resourcePercent = 25;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.setStamina(data.getMaxStamina() * resourcePercent * 0.01);
		data.setMana(data.getMaxMana() * resourcePercent * 0.01);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EXPERIENCE_BOTTLE, "Start fights with "
				+ DescUtil.val(resourcePercent + "%") + " of your max stamina and mana.");
	}
}