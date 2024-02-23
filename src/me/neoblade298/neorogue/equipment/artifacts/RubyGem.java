package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;

public class RubyGem extends Artifact {
	private int max, shields;

	public RubyGem() {
		super("rubyGem", "Ruby Gem", Rarity.EPIC, EquipmentClass.CLASSLESS);
		canDrop = false;
		max = 50;
		shields = 10;
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
