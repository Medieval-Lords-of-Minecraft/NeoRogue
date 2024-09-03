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

public class RubyShard extends Artifact {
	private static final String ID = "rubyShard";
	private int max, shields;

	public RubyShard() {
		super(ID, "Ruby Shard", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 5;
		shields = 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addPermanentShield(p.getUniqueId(), ai.getAmount() * shields);
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
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
