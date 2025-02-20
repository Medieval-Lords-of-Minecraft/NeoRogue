package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class EmeraldShard extends Artifact {
	private static final String ID = "emeraldShard";
	private int max;
	private double regen;

	public EmeraldShard() {
		super(ID, "Emerald Shard", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 5;
		regen = 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.addMaxStamina(max);
		data.addStaminaRegen(regen);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EMERALD, "Increases max stamina by <white>" + max + "</white> and stamina regen by <white>" + regen + "</white>.");
	}
}
