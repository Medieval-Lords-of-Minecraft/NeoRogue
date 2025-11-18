package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.event.RewardFightEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class TarotCard extends Artifact {
	private static final String ID = "TarotCard";

	public TarotCard() {
		super(ID, "Tarot Card", Rarity.RARE, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAP, "Increases the number of equipment options dropped in a fight by <white>1</white>.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		data.addTrigger(id, SessionTrigger.REWARD_FIGHT, (pdata, in) -> {
			RewardFightEvent ev = (RewardFightEvent) in;
			ev.addBonusDrops(1);
		});
	}
}
