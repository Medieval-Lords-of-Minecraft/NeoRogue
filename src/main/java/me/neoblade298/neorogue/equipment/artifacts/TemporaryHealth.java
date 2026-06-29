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

public class TemporaryHealth extends Artifact {
	private static final String ID = "TemporaryHealth";
	private static final int WINS = 2; // fight wins before healing

	public TemporaryHealth() {
		super(ID, "Temporary Health", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			PlayerSessionData sdata = data.getSessionData();
			sdata.removeArtifact(this);
			if (ai.getAmount() <= 0) {
				sdata.healPercent(0.30);
			}
			return TriggerResult.remove();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.damagePercent(0.10);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPLASH_POTION,
				"On acquire, take " + DescUtil.white("10%") + " of your max health as damage. After winning "
						+ DescUtil.white(WINS) + " fights, heal " + DescUtil.white("30%") + " of your max health, then crumble away.");
	}
}
