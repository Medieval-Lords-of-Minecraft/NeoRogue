package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class EmeraldCluster extends Artifact {
	private int max;
	private double regen;

	public EmeraldCluster() {
		super("emeraldCluster", "Emerald Cluster", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 25;
		regen = 0.5;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxStamina(max);
		data.addStaminaRegen(regen);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EMERALD, "<gray>Increases max stamina by <white>" + max + "</white> and stamina regen by <white>" + regen +"</white>.");
	}
}
