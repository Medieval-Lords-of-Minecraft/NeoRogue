package me.neoblade298.neorogue.equipment.artifacts;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
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

public class FaerieDust extends Artifact {
	private static final String ID = "FaerieDust";
	private static final int def = 8;

	public FaerieDust() {
		super(ID, "Faerie Dust", Rarity.COMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), 
			Buff.increase(data, def, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)), 300);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SKULL_BANNER_PATTERN, 
				"For the first <white>15s</white> of a fight, your " + GlossaryTag.MAGICAL.tag(this) + " defense is " +
				"increased by " + DescUtil.white(def) + ".");
	}
}
