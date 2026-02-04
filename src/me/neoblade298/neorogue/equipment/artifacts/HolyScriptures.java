package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class HolyScriptures extends Artifact {
	private static final String ID = "HolyScriptures";
	
	public HolyScriptures() {
		super(ID, "Holy Scriptures", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		canDrop = false;
		canStack = true;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.LIGHT), Buff.multiplier(data, 0.2, StatTracker.damageBuffAlly(UUID.randomUUID().toString(), this)));
	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.ENCHANTED_BOOK, "Increases " + GlossaryTag.LIGHT.tag(this) + " damage by <white>20%</white>.");
	}
}
