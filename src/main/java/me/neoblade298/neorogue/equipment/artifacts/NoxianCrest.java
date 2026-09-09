package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class NoxianCrest extends Artifact {
	private static final String ID = "NoxianCrest";
	private final int mana, stamina;

	public NoxianCrest() {
		super(ID, "Noxian Crest", Rarity.COMMON, EquipmentClass.CLASSLESS);
		mana = 5;
		stamina = 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			data.addMana(mana);
			data.addStamina(stamina);
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_BRICK, "Killing an enemy grants " + DescUtil.val(mana)
				+ " mana and " + DescUtil.val(stamina) + " stamina.");
	}
}