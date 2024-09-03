package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class SapphireCluster extends Artifact {
	private static final String ID = "sapphireCluster";
	private int max;
	private double regen;

	public SapphireCluster() {
		super(ID, "Sapphire Cluster", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 10;
		regen = 0.4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		data.addMaxMana(max);
		data.addManaRegen(regen);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI, "Increases max mana by <white>" + max + "</white> and mana regen by <white>" + regen + "</white>.");
	}
}
