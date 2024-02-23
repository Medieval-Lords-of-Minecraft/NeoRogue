package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.event.RewardGoldEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class GoldIngot extends Artifact {

	public GoldIngot() {
		super("goldIngot", "Gold Ingot", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}
	
	@Override
	public void onInitializeSession(PlayerSessionData data) {
		data.addTrigger(id, SessionTrigger.REWARD_GOLD, (pdata, in) -> {
			RewardGoldEvent ev = (RewardGoldEvent) in;
			ev.setAmount(ev.getAmount() + 5);
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_INGOT, 
				"Increases gold earned from fights by <white>5</white>.");
	}
}
