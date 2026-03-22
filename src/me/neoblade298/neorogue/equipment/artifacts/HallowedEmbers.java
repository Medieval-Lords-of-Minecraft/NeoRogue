package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HallowedEmbers extends Artifact {
	private static final String ID = "HallowedEmbers";
	private double healthThreshold = 0.5;
	private double healPercent = 0.1;

	public HallowedEmbers() {
		super(ID, "Hallowed Embers", Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			Player p = data.getPlayer();
			if (p.getHealth() <= data.getMaxHealth() * healthThreshold) {
				data.addHealth(data.getMaxHealth() * healPercent);
			}
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
		item = createItem(Material.BLAZE_POWDER, "If you're below <white>" + (int) (healthThreshold * 100)
				+ "%</white> health after winning a fight, heal for <white>" + (int) (healPercent * 100) + "%</white> of your max health.");
	}
}
