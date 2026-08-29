package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class ClearGoblet extends Artifact {
	private static final String ID = "ClearGoblet";
	private int mana, stamina;

	public ClearGoblet() {
		super(ID, "Clear Goblet", Rarity.COMMON, EquipmentClass.CLASSLESS);
		canStack = true;
		mana = 5;
		stamina = 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addMana(mana * ai.getAmount());
		data.addStamina(stamina * ai.getAmount());
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLASS_BOTTLE,
				"Start fights with " + DescUtil.val(mana) + " mana and " + DescUtil.val(stamina)
						+ " stamina per stack.");
	}
}