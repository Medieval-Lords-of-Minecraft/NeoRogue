package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class GoldIngot extends Artifact {
	private static final String ID = "GoldIngot";
	
	public GoldIngot() {
		super(ID, "Gold Ingot", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {

	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		data.addTrigger(id, SessionTrigger.REWARD_FIGHT, (pdata, in) -> {
			if (!pdata.hasArtifact(id)) return TriggerResult.remove();
			RewardFightEvent ev = (RewardFightEvent) in;
			ev.addBonusGold(25);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_INGOT, "Increases gold earned from fights by " + DescUtil.white(25) + ".");
	}
}
