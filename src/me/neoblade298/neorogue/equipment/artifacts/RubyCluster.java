package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class RubyCluster extends Artifact {
	private static final String ID = "rubyCluster";
	private int max, shields;

	public RubyCluster() {
		super(ID, "Ruby Cluster", Rarity.RARE, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 10;
		shields = 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addPermanentShield(p.getUniqueId(), ai.getAmount() * shields);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		data.addMaxHealth(max);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE, "Increases max health by <white>" + max + "</white> and grants " +
				GlossaryTag.SHIELDS.tag(this, shields, false) + " at the start of a fight.");
	}
}
