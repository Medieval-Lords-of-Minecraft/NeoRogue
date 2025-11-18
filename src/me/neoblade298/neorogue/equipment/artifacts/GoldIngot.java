package me.neoblade298.neorogue.equipment.artifacts;

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

public class GoldIngot extends Artifact {
	private static final String ID = "GoldIngot";
	
	public GoldIngot() {
		super(ID, "Gold Ingot", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
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
			ev.addBonusGold(25);
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_INGOT, "Increases gold earned from fights by <white>25</white>.");
	}
}
