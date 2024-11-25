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

public class OmniGem extends Artifact {
	private static final String ID = "omniGem";
	private int flat;
	private double shields, regen;

	public OmniGem() {
		super(ID, "Omni-Gem", Rarity.RARE, EquipmentClass.CLASSLESS);
		canStack = true;
		flat = 5;
		shields = 2;
		regen = 0.2;
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
		data.addMaxHealth(flat);
		data.addMaxStamina(flat);
		data.addStaminaRegen(regen);
		data.addMaxMana(flat);
		data.addManaRegen(regen);
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.DIAMOND,
				"Increases max health, stamina, and mana by <white>" + flat
						+ "</white>, increases stamina and mana regen by <white>" + regen + "</white>, and grants "
						+ GlossaryTag.SHIELDS.tag(this, shields, false) + " at the start of a fight."
		);
	}
}
